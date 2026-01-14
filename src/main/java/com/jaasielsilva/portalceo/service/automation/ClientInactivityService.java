package com.jaasielsilva.portalceo.service.automation;

import com.jaasielsilva.portalceo.event.ClientInactivityEvent;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientInactivityService {

    private final ClienteRepository clienteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ClientInactivityService(ClienteRepository clienteRepository, ApplicationEventPublisher eventPublisher) {
        this.clienteRepository = clienteRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Roda a cada hora cheia (Ex: 08:00, 09:00, 10:00)
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void verificarInatividade() {
        // A lógica de verificação foi movida para o AutomationListener.
        // O ClientInactivityService agora apenas dispara o "pulso" (evento) informando que uma verificação está disponível.
        // O Listener vai decidir QUEM deve receber com base no horário.
        
        // Dispara o evento com a hora atual para o Listener filtrar
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(30);
        List<Cliente> inativos = clienteRepository.findByUltimaCompraAntesDe(dataLimite);

        if (!inativos.isEmpty()) {
            ClientInactivityEvent event = new ClientInactivityEvent(this, inativos.size());
            eventPublisher.publishEvent(event);
        }
    }
    
    // Método para trigger manual via Job Controller
    public void forcarVerificacao() {
        verificarInatividade();
    }
}
