package cloud.loify.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TrailingSlashHandlerFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestUri = exchange.getRequest().getURI().getPath();

        if (requestUri.endsWith("/")) {
            String newUrl = requestUri.substring(0, requestUri.length() - 1);
            exchange.getResponse().setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, newUrl);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
