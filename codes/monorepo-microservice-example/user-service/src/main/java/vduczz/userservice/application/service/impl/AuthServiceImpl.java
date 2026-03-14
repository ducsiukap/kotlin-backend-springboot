package vduczz.userservice.application.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import vduczz.userservice.application.port.in.dto.request.AuthRequestDto;
import vduczz.userservice.application.port.in.dto.response.AuthResponseDto;
import vduczz.userservice.application.service.AuthService;
import vduczz.userservice.domain.model.Account;
import vduczz.userservice.domain.model.Email;
import vduczz.userservice.domain.model.Password;
import vduczz.userservice.domain.repository.AccountRepository;
import vduczz.userservice.domain.service.AccountDomainService;
import vduczz.userservice.infrastructure.client.NotificationClient;
import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;
import vduczz.userservice.infrastructure.config.messagequeue.rabbitmq.RabbitMQConstant;
import vduczz.userservice.infrastructure.config.messagequeue.rabbitmq.RabbitNotificationConfig;
import vduczz.userservice.infrastructure.config.messagequeue.kafka.KafkaProduceConstants;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final AccountDomainService accountDomainService;

    // injects interface to call to notification-service synchronous
    private final NotificationClient notiClient;

    // injects RabbitTemplate to send RabbitMQ message
    private final RabbitTemplate rabbitTemplate;

    // injects KafkaTemplate to send Kafka message
    private final KafkaTemplate<String, Object> objectKafkaTemplate;
//    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public AuthResponseDto register(
            AuthRequestDto.RegisterRequest request,
            boolean mq, // use MessageQueue?
            boolean kafka // use Kafka event stream
    ) {
        // log

        // tạo value object
        Email email = new Email(request.getEmail());
        Password password = new Password(request.getPassword());

        // tạo model
        // qua domain service
        Account account = accountDomainService.createAccount(request.getName(), email, password);

        // save to db
        //        Account newAccount = accountRepository.save(account);

        // message to communicate
        WelcomeMailRequest message =
                new WelcomeMailRequest(
                        email.email(),
                        account.getName()
                );

        if (mq || kafka) {
            // ------------------------------------------------------------
            // Asynchronous

            // Message Key
            String messageId = account.getId().toString();
            // nên dựa vào id object gửi đi, ...

            // CorrelationData -> message identify
            CorrelationData correlationData = new CorrelationData(messageId);

            if (mq) { // RabbitMQ
                rabbitTemplate.convertAndSend(
                        RabbitMQConstant.Exchanges.EXCHANGE, // Exchange
                        RabbitMQConstant.RoutingKeys.ROUTING_KEY_V1, // Key
                        // Exchange + Key = Queue(s)

                        // message
                        message,

                        // correlation
                        correlationData
                );
                System.out.printf("[Async Communicate - RabbitMQ]: send message with id: %s\n", messageId);
            }

            if (kafka) { // Kafka
                try {
                    // CompletableFuture => chạy async
                    CompletableFuture<SendResult<String, Object>> failure = objectKafkaTemplate.send(
                            KafkaProduceConstants.UserEvents.USER_EVENTS_TOPIC, // topic
                            messageId,// key, với multi-types message per topic, key should be entity.id
                            message// value
                    );
                    // note: kafka.Template.send(...).get() // get() block thread hiện tại -> sync

                    failure.whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.out.printf("[Async Communicate - Kafka]: FAILED send message with id: %s\n", messageId);
                        } else {
                            System.out.printf("[Async Communicate - Kafka]: send message with id: %s\n", messageId);
                            System.out.printf(
                                    "[Async Communicate - Kafka]: topic: %s, partition: %d, offset: %d\n",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });

                    // Send with custom header
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            // ------------------------------------------------------------
            // synchronous -> block thread hiện tại

            // use OpenFeign client
            notiClient.sendWelcomeMessage(
                    message
            );

            // nếu không lỗi -> code chạy xuống đây
            // nếu có lỗi -> thread hiện tại bị sập hoặc chờ quá lâu
            //      => nếu hết thead: server sập!
            System.out.println("[Sync Communicate]: Sent welcome-mail successfully!");
        }

        // return
        return AuthResponseDto.builder()
                .userId(account.getId())
                .accountId(account.getId())
                .build();
    }
}
