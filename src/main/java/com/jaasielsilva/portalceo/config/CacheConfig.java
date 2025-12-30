package com.jaasielsilva.portalceo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${erp.cnpj-cache.ttl-hours:24}")
    private long ttlHours;

    @Value("${erp.cnpj-cache.max-size:100000}")
    private long maxSize;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Primary
    public CacheManager cacheManager() {
        SimpleCacheManager specific = new SimpleCacheManager();
        Caffeine<Object, Object> recrutamentoSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(10000);
        CaffeineCache candidatosPage = new CaffeineCache("recrutamentoCandidatosPage", recrutamentoSpec.build());

        Caffeine<Object, Object> headcountSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(120))
                .maximumSize(5000);
        CaffeineCache headcountTipos = new CaffeineCache("rhHeadcountTiposContrato", headcountSpec.build());

        Caffeine<Object, Object> colaboradoresListSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(10);
        CaffeineCache colaboradoresAtivosList = new CaffeineCache("colaboradoresAtivosList", colaboradoresListSpec.build());

        Caffeine<Object, Object> colaboradoresCountSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(60))
                .maximumSize(100);
        CaffeineCache colaboradoresAtivosCount = new CaffeineCache("colaboradoresAtivosCount", colaboradoresCountSpec.build());
        Caffeine<Object, Object> vtConfigSpec = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(12))
                .maximumSize(5);
        CaffeineCache vtConfigCache = new CaffeineCache("vtConfig", vtConfigSpec.build());

        java.util.List<org.springframework.cache.Cache> caches = new java.util.ArrayList<>();
        caches.add(candidatosPage);
        caches.add(headcountTipos);
        caches.add(colaboradoresAtivosList);
        caches.add(colaboradoresAtivosCount);
        caches.add(vtConfigCache);

        try {
            if (applicationContext.containsBean("redisConnectionFactory")) {
                Object redisConnectionFactory = applicationContext.getBean("redisConnectionFactory");
                Class<?> redisCacheConfigurationClass = Class.forName("org.springframework.data.redis.cache.RedisCacheConfiguration");
                Object config = redisCacheConfigurationClass.getMethod("defaultCacheConfig").invoke(null);
                config = redisCacheConfigurationClass.getMethod("entryTtl", Duration.class).invoke(config, Duration.ofSeconds(60));

                Class<?> serializationPairClass = Class.forName("org.springframework.data.redis.serializer.RedisSerializationContext$SerializationPair");
                Class<?> redisSerializerClass = Class.forName("org.springframework.data.redis.serializer.RedisSerializer");
                Class<?> stringSerializerClass = Class.forName("org.springframework.data.redis.serializer.StringRedisSerializer");
                Class<?> jsonSerializerClass = Class.forName("org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer");

                Object stringSerializer = stringSerializerClass.getConstructor().newInstance();
                Object jsonSerializer = jsonSerializerClass.getConstructor().newInstance();
                Object keyPair = serializationPairClass.getMethod("fromSerializer", redisSerializerClass).invoke(null, stringSerializer);
                Object valPair = serializationPairClass.getMethod("fromSerializer", redisSerializerClass).invoke(null, jsonSerializer);

                config = redisCacheConfigurationClass.getMethod("serializeKeysWith", serializationPairClass).invoke(config, keyPair);
                config = redisCacheConfigurationClass.getMethod("serializeValuesWith", serializationPairClass).invoke(config, valPair);

                Class<?> managerClass = Class.forName("org.springframework.data.redis.cache.RedisCacheManager");
                Class<?> builderClass = Class.forName("org.springframework.data.redis.cache.RedisCacheManager$RedisCacheManagerBuilder");
                Class<?> rcFactoryClass = Class.forName("org.springframework.data.redis.connection.RedisConnectionFactory");

                Object builder = managerClass.getMethod("builder", rcFactoryClass).invoke(null, redisConnectionFactory);
                builder = builderClass.getMethod("cacheDefaults", redisCacheConfigurationClass).invoke(builder, config);
                Object redisManager = builderClass.getMethod("build").invoke(builder);

                org.springframework.cache.Cache dashboardMetrics =
                        (org.springframework.cache.Cache) managerClass.getMethod("getCache", String.class)
                                .invoke(redisManager, "dashboardMetrics");
                if (dashboardMetrics != null) {
                    caches.add(dashboardMetrics);
                }
            }
        } catch (Throwable ignored) {}

        specific.setCaches(caches);

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
