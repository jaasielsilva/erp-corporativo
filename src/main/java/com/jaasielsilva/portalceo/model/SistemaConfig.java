package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sistema_configs")
public class SistemaConfig {

    @Id
    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @Column(name = "config_value")
    private String value;

    public SistemaConfig() {}

    public SistemaConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
