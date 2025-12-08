package com.jaasielsilva.portalceo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${erp.cnpj-cache.ttl-hours:24}")
    private long ttlHours;

    @Value("${erp.cnpj-cache.max-size:100000}")
    private long maxSize;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager specific = new SimpleCacheManager();
        Caffeine<Object, Object> recrutamentoSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(10000);
        CaffeineCache candidatosPage = new CaffeineCache("recrutamentoCandidatosPage", recrutamentoSpec.build());
        specific.setCaches(java.util.List.of(candidatosPage));

        Caffeine<Object, Object> defaultSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(ttlHours))
                .maximumSize(maxSize);
        CaffeineCacheManager dynamic = new CaffeineCacheManager();
        dynamic.setCaffeine(defaultSpec);

        CompositeCacheManager composite = new CompositeCacheManager(specific, dynamic);
        composite.setFallbackToNoOpCache(false);
        return composite;
    }
}
