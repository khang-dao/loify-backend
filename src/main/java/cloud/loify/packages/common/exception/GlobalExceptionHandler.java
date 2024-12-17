package cloud.loify.packages.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<String>> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        // Convert HttpStatusCode to HttpStatus
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        if (status == HttpStatus.NOT_FOUND) {
            // Handle 404 Not Found
            String errorMessage = "Sorry, the page you are looking for does not exist.";
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage));
        }

        // Handle other status codes with a generic error message
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred."));
    }
}
