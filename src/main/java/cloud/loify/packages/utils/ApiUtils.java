package cloud.loify.packages.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.function.Supplier;

public class ApiUtils {

    /**
     * Utility method to retry a Mono operation on 429 error using the Retry-After header.
     *
     * @param operation The Mono operation to execute with retry support.
     * @param <T>       The type of response the Mono will emit.
     * @return A Mono that retries on 429 Too Many Requests error based on Retry-After header.
     */
    public static <T> Mono<T> retryWithDelay(Supplier<Mono<T>> operation) {
        return Mono.defer(operation)
                .retryWhen(Retry.from(companion -> companion.handle((retrySignal, sink) -> {
                    Throwable failure = retrySignal.failure();
                    if (failure instanceof WebClientResponseException e) {
                        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            String retryAfterHeader = e.getHeaders().getFirst("Retry-After");
                            long retryAfterSeconds = retryAfterHeader != null ? Long.parseLong(retryAfterHeader) : 1;

                            System.out.printf("Received 429 Too Many Requests. Retrying after %d seconds.%n", retryAfterSeconds);
                            sink.next(Duration.ofSeconds(retryAfterSeconds));
                        } else {
                            sink.error(failure); // Stop retrying for other HTTP errors
                        }
                    } else {
                        sink.error(failure); // Stop retrying for non-HTTP errors
                    }
                })));
    }
}
