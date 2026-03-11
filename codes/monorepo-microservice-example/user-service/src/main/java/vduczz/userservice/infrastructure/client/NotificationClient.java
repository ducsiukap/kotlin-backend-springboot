package vduczz.userservice.infrastructure.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vduczz.userservice.infrastructure.client.dto.WelcomeMailRequest;
import vduczz.userservice.infrastructure.config.feign.FeignConfig;

@FeignClient(
        name = "notification-service",
        url = "${app.client.notification-service.url}", // nếu cấu hình API Gateway / Load Balance thì không cần nữa

        // configuration
        configuration = FeignConfig.class

        // Fallback class
        // fallback =

        // Fallback Factory
        // fallbackFactory =
)
public interface NotificationClient {
    // chỉ cần khai báo interface
    // Spring tự sinh Proxy khi cần, tường tự JpaRepository

    // Sử dụng tương tự @RestController methods

    // Method Mapping -> Method mà sẽ gửi tới service bên kia
    // GetMapping, PostMapping, PutMapping, PatchMapping, DeleteMapping, ...
    @PostMapping(
            value = "/notifications/welcome"

            // upload multipart
            // consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    )
    void sendWelcomeMessage(
            // RequestBody, RequestParam,
            // PathVariable
            @RequestBody WelcomeMailRequest request
    );
}
