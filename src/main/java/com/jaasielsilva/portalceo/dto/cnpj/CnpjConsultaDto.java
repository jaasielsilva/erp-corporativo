package com.jaasielsilva.portalceo.dto.cnpj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CnpjConsultaDto {
    private String razaoSocial;
    private String nomeFantasia;
    private EnderecoDto endereco;
    private String situacaoCadastral;
    private CnaeDto cnaePrincipal;
    private List<CnaeDto> cnaesSecundarios;
}

