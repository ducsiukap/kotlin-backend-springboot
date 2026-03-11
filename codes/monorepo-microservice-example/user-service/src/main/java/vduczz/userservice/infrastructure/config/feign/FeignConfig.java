package vduczz.userservice.infrastructure.config.feign;


import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    // Logging
    @Bean
    public Logger.Level feignLoggerLevel() {
        //  - NONE: không log
        //  - BASIC: URL + Method + status + time
        //  - HEADERS:
        //      + BASIC
        //      + Request/Response Header
        //  - FULL:
        //      + HEADERS
        //      + Request/Response Body
        return Logger.Level.HEADERS;
    }

    // Truyền Header
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            // -> tự động nhét mọi header bên dưới vào MỌI request
            //      được gửi đi từ FeignClient
            public void apply(RequestTemplate template) {

                // add headers

                // template.header("X-Service-Name", "user-service");
                // security
                // template.header("Authorization", "Bearer token_cua_he_thong_o_day");
                template.header("X-Header-XXX", "HelloWorld!");
            }
        };
    }

}
