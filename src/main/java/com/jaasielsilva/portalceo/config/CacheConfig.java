package com.jaasielsilva.portalceo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
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
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(ttlHours))
                .maximumSize(maxSize);

        CaffeineCacheManager manager = new CaffeineCacheManager("cnpjCache");
        manager.setCaffeine(builder);
        return manager;
    }
}
