package vduczz.userservice.application.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import vduczz.userservice.application.port.in.dto.request.AuthRequestDto;
import vduczz.userservice.application.port.in.dto.response.AuthResponseDto;
import vduczz.userservice.application.port.out.gateway.NotificationGateway;
import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;
import vduczz.userservice.application.port.out.messaging.EventPublisher;
import vduczz.userservice.application.service.AuthService;
import vduczz.userservice.domain.model.Account;
import vduczz.userservice.domain.model.Email;
import vduczz.userservice.domain.model.Password;
import vduczz.userservice.domain.repository.AccountRepository;
import vduczz.userservice.domain.service.AccountDomainService;

import java.util.List;

@Service
@Primary
public class AuthServiceImpl2 implements AuthService {
    private final AccountDomainService accountDomainService;
    private final AccountRepository accountRepository;

    //
    private final NotificationGateway notificationGateway;
    private final EventPublisher kafkaPublisher;
    private final EventPublisher rabbitMQPublisher;

    public AuthServiceImpl2(
            AccountDomainService accountDomainService,
            AccountRepository accountRepository,
            NotificationGateway notificationGateway,
            @Qualifier("kafkaEventPublisher") EventPublisher kafkaPublisher,
            @Qualifier("rabbitMQPublisher") EventPublisher rabbitMQPublisher
    ) {
        this.accountDomainService = accountDomainService;
        this.accountRepository = accountRepository;
        this.notificationGateway = notificationGateway;
        this.kafkaPublisher = kafkaPublisher;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public AuthResponseDto register(
            AuthRequestDto.RegisterRequest request,
            boolean mq, boolean kafka
    ) {

        Account account = accountDomainService.createAccount(
                request.getName(),
                new Email(request.getEmail()),
                new Password(request.getPassword())
        );

        // save to db
        // accountRepository.save(account);

        // gửi request
        boolean useBroker = mq || kafka;

        if (useBroker) { // messaging
            // events
            List<Object> events = account.pullEvents(); // get & delete
            System.out.println("events.size: " + events.size());
            if (kafka) {
                events.forEach(event ->
                        // với những loại event quan trọng, bắt buộc phải gửi thành công
                        // => nếu lưu event vào DB thất bại -> rollback toàn bộ!
                        kafkaPublisher.publishReliable(account, event)
                );
            }
            if (mq) {
                // với những event không quan trọng việc gửi thành công hay không
                // => fire & forget
                events.forEach(rabbitMQPublisher::publishDirect);
            }
        } else {
            // via Feign client
            // không thành công -> lập tức hủy transaction
            syncSend(account);
        }

        // trả về controller
        return new AuthResponseDto(
                account.getId(),
                account.getId()
        );
    }


    private void syncSend(Account account) {
        // mapping domain -> request
        WelcomeMailRequest feignRequest =
                new WelcomeMailRequest(
                        account.getEmail().email(),
                        account.getPassword().password()
                );

        // send request
        notificationGateway.sendWelcomeMail(feignRequest);

        System.out.println("[Sync Communicate]: Sent welcome-mail successfully!");
    }

}
