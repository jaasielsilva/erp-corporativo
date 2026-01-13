package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.SistemaConfig;
import com.jaasielsilva.portalceo.repository.SistemaConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracaoService {

    private final SistemaConfigRepository repository;

    public ConfiguracaoService(SistemaConfigRepository repository) {
        this.repository = repository;
    }

    public String getValor(String key, String defaultValue) {
        return repository.findByKey(key)
                .map(SistemaConfig::getValue)
                .orElse(defaultValue);
    }

    public boolean isModoManutencaoAtivo() {
        return Boolean.parseBoolean(getValor("maintenance_mode", "false"));
    }

    public String getEmailRelatorio() {
        return getValor("report_email", "silvajasiel30@gmail.com");
    }

    @Transactional
    public void setValor(String key, String value) {
        SistemaConfig config = repository.findByKey(key)
                .orElse(new SistemaConfig(key, value));
        config.setValue(value);
        repository.save(config);
    }
}
