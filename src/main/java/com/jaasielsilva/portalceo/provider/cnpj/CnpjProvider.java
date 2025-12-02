package com.jaasielsilva.portalceo.provider.cnpj;

import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;

public interface CnpjProvider {
    CnpjConsultaDto consultar(String cnpj);
}

