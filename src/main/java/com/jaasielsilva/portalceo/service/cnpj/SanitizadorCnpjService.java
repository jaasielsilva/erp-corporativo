package com.jaasielsilva.portalceo.service.cnpj;

import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import com.jaasielsilva.portalceo.util.CnpjUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class SanitizadorCnpjService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional
    public Map<String, Object> sanitizar(boolean apply) {
        List<Cliente> clientes = clienteRepository.findByTipoClienteIgnoreCaseAndCpfCnpjIsNotNull("PJ");
        int normalized = 0;
        int invalid = 0;
        List<String> invalidSamples = new ArrayList<>();
        for (Cliente c : clientes) {
            String orig = c.getCpfCnpj();
            String s = CnpjUtils.sanitize(orig);
            if (s != null && !s.equals(orig)) {
                if (apply) c.setCpfCnpj(s);
                normalized++;
            }
            if (!CnpjUtils.isValid(s)) {
                invalid++;
                if (invalidSamples.size() < 50) invalidSamples.add(s);
            }
        }
        if (apply) {
            clienteRepository.saveAll(clientes);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("total", clientes.size());
        res.put("normalized", normalized);
        res.put("invalid", invalid);
        res.put("invalidSamples", invalidSamples);
        res.put("applied", apply);
        return res;
    }
}

