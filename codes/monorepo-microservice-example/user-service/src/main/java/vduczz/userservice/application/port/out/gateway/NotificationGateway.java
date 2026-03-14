package vduczz.userservice.application.port.out.gateway;

import vduczz.userservice.application.port.out.gateway.dto.request.WelcomeMailRequest;

// Synchronous communication
public interface NotificationGateway {

    // hàm gọi client
    // thay void bằng kiểu trả về phù hợp nếu có
    public void sendWelcomeMail(
            // package
            WelcomeMailRequest request
    );
    
}
