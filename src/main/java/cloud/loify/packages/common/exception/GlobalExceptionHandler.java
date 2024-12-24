package cloud.loify.packages.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<Void> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        if (exchange.getRequest().getURI().getPath().equals("/error/404.html")) {
            return Mono.empty();
        }

        if (status == HttpStatus.NOT_FOUND) {
            exchange.getResponse().setStatusCode(HttpStatus.MOVED_PERMANENTLY);  // Permanent redirect (301)
            exchange.getResponse().getHeaders().setLocation(exchange.getRequest().getURI().resolve("/error/404.html"));

            return exchange.getResponse().setComplete();  // Complete the response with the redirect
        }

        // For other status codes, you can return a generic response or do nothing
        return Mono.empty();
    }
}
