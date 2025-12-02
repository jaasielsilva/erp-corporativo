package com.jaasielsilva.portalceo.service.cnpj;

import com.jaasielsilva.portalceo.repository.cnpj.CnpjFonteRepository;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.service.NotificationService;
import com.jaasielsilva.portalceo.dto.cnpj.ProcessamentoStatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.jaasielsilva.portalceo.repository.NotificationRepository;

@Service
public class ProcessamentoCnpjService {

    @Autowired
    private CnpjFonteRepository cnpjFonteRepository;

    @Autowired
    private ReceitaService receitaService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private final AtomicBoolean emExecucao = new AtomicBoolean(false);
    private final AtomicInteger processados = new AtomicInteger(0);
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private final AtomicBoolean pauseRequested = new AtomicBoolean(false);
    private volatile long startedAt = 0L;
    private volatile long finishedAt = 0L;
    private final AtomicInteger invalidCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final java.util.concurrent.CopyOnWriteArrayList<String> invalidSamples = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final java.util.concurrent.CopyOnWriteArrayList<String> errorSamples = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Async("taskExecutor")
    public void processarAsync(String emailUsuario, String protocolo) {
        if (emExecucao.get()) {
            return;
        }
        emExecucao.set(true);
        processados.set(0);
        cancelRequested.set(false);
        pauseRequested.set(false);
        startedAt = System.currentTimeMillis();
        invalidCount.set(0);
        errorCount.set(0);
        invalidSamples.clear();
        errorSamples.clear();
        Usuario usuario = null;
        if (emailUsuario != null) {
            usuario = usuarioRepository.findByEmail(emailUsuario).orElse(null);
        }
        try {
            if (usuario != null) {
                Notification nStart = notificationService.createNotification(
                        "cnpj_processing_start",
                        "Processamento de CNPJs",
                        "Iniciado — Protocolo: " + protocolo,
                        Notification.Priority.LOW,
                        usuario
                );
                nStart.setActionUrl("/utilidades/processar-cnpj");
                nStart.setMetadata("{\"protocol\":\"" + protocolo + "\"}");
                notificationRepository.save(nStart);
            }
            int limit = 200;
            int offset = 0;
            int concurrency = currentConcurrency;
            ThreadPoolTaskExecutor executor = taskExecutor;
            Semaphore semaphore = new Semaphore(Math.max(1, concurrency));
            while (true) {
                List<String> lote = cnpjFonteRepository.buscarLote(limit, offset);
                if (lote == null || lote.isEmpty()) {
                    break;
                }
                List<CompletableFuture<Void>> futures = new CopyOnWriteArrayList<>();
                for (String cnpj : lote) {
                    if (cancelRequested.get()) break;
                    while (pauseRequested.get() && !cancelRequested.get()) {
                        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                    }
                    try {
                        semaphore.acquire();
                        CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                            try {
                                String s = com.jaasielsilva.portalceo.util.CnpjUtils.sanitize(cnpj);
                                if (!com.jaasielsilva.portalceo.util.CnpjUtils.isValid(s)) {
                                    invalidCount.incrementAndGet();
                                    if (invalidSamples.size() < 20) invalidSamples.add(s);
                                    return;
                                }
                                receitaService.consultarCnpj(s);
                                processados.incrementAndGet();
                            } catch (Exception ignored) {
                                errorCount.incrementAndGet();
                                String s2 = com.jaasielsilva.portalceo.util.CnpjUtils.sanitize(cnpj);
                                if (errorSamples.size() < 20) errorSamples.add(s2);
                            } finally {
                                semaphore.release();
                            }
                        }, executor);
                        futures.add(f);
                    } catch (InterruptedException ignored) {}
                }
                for (CompletableFuture<Void> f : futures) {
                    try { f.join(); } catch (Exception ignored) {}
                }
                offset += limit;
                if (cancelRequested.get()) break;
            }
        } finally {
            emExecucao.set(false);
            finishedAt = System.currentTimeMillis();
            long duration = finishedAt - startedAt;
            if (usuario != null) {
                Notification nDone = notificationService.createNotification(
                        "cnpj_processing_complete",
                        "Processamento de CNPJs",
                        "Concluído (" + processados.get() + ") — Invalidos: " + invalidCount.get() + " — Erros: " + errorCount.get() + " — Protocolo: " + protocolo,
                        Notification.Priority.MEDIUM,
                        usuario
                );
                nDone.setActionUrl("/utilidades/processar-cnpj");
                nDone.setMetadata("{\"protocol\":\"" + protocolo + "\",\"processedCount\":" + processados.get() + ",\"invalidCount\":" + invalidCount.get() + ",\"errorCount\":" + errorCount.get() + ",\"durationMs\":" + duration + "}");
                notificationRepository.save(nDone);
            }

            // Redundância: enviar notificação global para garantir exibição do toast em qualquer página
            notificationService.createGlobalNotification(
                    "cnpj_processing_complete",
                    "Processamento de CNPJs",
                    "Concluído (" + processados.get() + ") — Invalidos: " + invalidCount.get() + " — Erros: " + errorCount.get() + " — Protocolo: " + protocolo,
                    Notification.Priority.MEDIUM
            );
        }
    }

    public ProcessamentoStatusDto status() {
        boolean running = emExecucao.get();
        Long st = startedAt == 0L ? null : startedAt;
        Long fin = finishedAt == 0L ? null : finishedAt;
        Long dur = (st != null && fin != null) ? (fin - st) : null;
        return new ProcessamentoStatusDto(running, processados.get(), st, fin, dur, invalidCount.get(), errorCount.get(), new java.util.ArrayList<>(invalidSamples), new java.util.ArrayList<>(errorSamples));
    }

    public void pause() { pauseRequested.set(true); }
    public void resume() { pauseRequested.set(false); }
    public void cancel() { cancelRequested.set(true); }

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private volatile int currentConcurrency = 4;
    public void setConcurrency(int c) { currentConcurrency = Math.max(1, c); }
}
