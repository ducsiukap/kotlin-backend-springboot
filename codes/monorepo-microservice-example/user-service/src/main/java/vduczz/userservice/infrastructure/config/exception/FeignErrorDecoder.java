package vduczz.userservice.infrastructure.config.exception;

import feign.Response;
import feign.codec.ErrorDecoder;

// Mọi lỗi 4xx và 5xx được trả về
// mặc định, OpenFeign ném ra FeignException => cần custom để nó trở nên hữu ích hơn
public class FeignErrorDecoder implements ErrorDecoder {
    // implements ErrorDecoder

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // http status
        int status = response.status();

        //
        if (status == 404) {
            // custom
            // ResourceNotFoundException
            //
            return new RuntimeException("Not Found");
        }
        if (status >= 400 && status < 500) {
            return new RuntimeException("Bad Request");
        }
        if (status >= 500 && status < 600) {
            return new RuntimeException("Internal Remote-Service Error");
        }

        // nếu lỗi khác -> trả về mặc định của feign
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
