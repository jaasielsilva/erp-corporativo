package com.jaasielsilva.portalceo.service.cnpj;

import com.jaasielsilva.portalceo.repository.cnpj.CnpjFonteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProcessamentoCnpjService {

    @Autowired
    private CnpjFonteRepository cnpjFonteRepository;

    @Autowired
    private ReceitaService receitaService;

    private final AtomicBoolean emExecucao = new AtomicBoolean(false);
    private final AtomicInteger processados = new AtomicInteger(0);

    @Async("taskExecutor")
    public void processarAsync() {
        if (emExecucao.get()) {
            return;
        }
        emExecucao.set(true);
        processados.set(0);
        try {
            int limit = 200;
            int offset = 0;
            while (true) {
                List<String> lote = cnpjFonteRepository.buscarLote(limit, offset);
                if (lote == null || lote.isEmpty()) {
                    break;
                }
                for (String cnpj : lote) {
                    try {
                        receitaService.consultarCnpj(cnpj);
                        processados.incrementAndGet();
                    } catch (Exception ignored) {
                    }
                }
                offset += limit;
            }
        } finally {
            emExecucao.set(false);
        }
    }

    public String status() {
        if (emExecucao.get()) {
            return "EM_EXECUCAO:" + processados.get();
        }
        return "CONCLUIDO:" + processados.get();
    }
}

