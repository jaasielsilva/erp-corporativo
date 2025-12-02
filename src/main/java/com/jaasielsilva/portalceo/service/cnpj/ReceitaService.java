package com.jaasielsilva.portalceo.service.cnpj;

import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;
import com.jaasielsilva.portalceo.provider.cnpj.CnpjProvider;
import com.jaasielsilva.portalceo.util.CnpjUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ReceitaService {

    @Autowired
    private CnpjProvider cnpjProvider;

    @Cacheable(value = "cnpjCache", key = "#cnpj")
    public CnpjConsultaDto consultarCnpj(String cnpj) {
        String s = CnpjUtils.sanitize(cnpj);
        if (!CnpjUtils.isValid(s)) {
            throw new IllegalArgumentException("CNPJ inválido");
        }
        return cnpjProvider.consultar(s);
    }
}
