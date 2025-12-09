package com.jaasielsilva.portalceo.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class HibernateBatchConfig implements HibernatePropertiesCustomizer {
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.jdbc.batch_size", 200);
        hibernateProperties.put("hibernate.order_inserts", true);
        hibernateProperties.put("hibernate.order_updates", true);
        hibernateProperties.put("hibernate.jdbc.fetch_size", 100);
        hibernateProperties.put("hibernate.jdbc.batch_versioned_data", true);
        hibernateProperties.put("hibernate.generate_statistics", true);
    }
}
