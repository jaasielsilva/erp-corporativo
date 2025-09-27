package com.jaasielsilva.portalceo.model;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ParteEnvolvida {

    public abstract String getNome();

}
