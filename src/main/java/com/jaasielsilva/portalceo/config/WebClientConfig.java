package com.jaasielsilva.portalceo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Configuration
public class WebClientConfig {

    @Value("${erp.receita.base-url:https://brasilapi.com.br/api/cnpj/v1}")
    private String baseUrl;

    @Value("${erp.receita.timeout-ms:8000}")
    private long timeoutMs;

    @Bean
    public WebClient receitaWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeoutMs));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorMappingFilter())
                .build();
    }

    private ExchangeFilterFunction errorMappingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            HttpStatusCode status = clientResponse.statusCode();
            if (status.is2xxSuccessful()) {
                return Mono.just(clientResponse);
            }
            return clientResponse.bodyToMono(String.class).defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new RuntimeException("Receita API error: " + status.value() + " " + body)));
        });
    }
}
