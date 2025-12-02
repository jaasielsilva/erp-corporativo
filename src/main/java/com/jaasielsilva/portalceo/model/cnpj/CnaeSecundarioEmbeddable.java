package com.jaasielsilva.portalceo.model.cnpj;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CnaeSecundarioEmbeddable {
    private String codigo;
    private String descricao;
}

