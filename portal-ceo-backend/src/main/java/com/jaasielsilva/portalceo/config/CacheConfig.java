package com.jaasielsilva.portalceo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Cache será configurado automaticamente pelo Spring Boot
}