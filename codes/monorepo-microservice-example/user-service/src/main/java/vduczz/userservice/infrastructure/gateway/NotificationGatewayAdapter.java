package vduczz.userservice.infrastructure.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vduczz.userservice.application.port.out.gateway.NotificationGateway;
import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;
import vduczz.userservice.infrastructure.client.NotificationClient;

@RequiredArgsConstructor
@Component
public class NotificationGatewayAdapter implements NotificationGateway {

    // adapter giữa client và application/gateway
    private final NotificationClient client;

    @Override
    public void sendWelcomeMail(WelcomeMailRequest request) {

        // processing

        client.sendWelcomeMessage(request);

        // nếu có result
        // => result processing,
        // mapping application/port/out/gateway/dto/response
        // return result
    }
}
