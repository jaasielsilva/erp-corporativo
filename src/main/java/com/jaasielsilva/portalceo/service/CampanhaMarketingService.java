package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.model.CampanhaMarketing.StatusCampanha;
import com.jaasielsilva.portalceo.repository.CampanhaMarketingRepository;
import com.jaasielsilva.portalceo.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CampanhaMarketingService {

    private final CampanhaMarketingRepository campanhaRepository;
    private final ClienteRepository clienteRepository;
    private final EmailService emailService;

    // CRUD Operations
    public CampanhaMarketing save(CampanhaMarketing campanha) {
        validarCampanha(campanha);

        if (campanha.getId() == null) {
            campanha.setDataCriacao(LocalDateTime.now());
            campanha.setStatus(CampanhaMarketing.StatusCampanha.RASCUNHO);
        }

        return campanhaRepository.save(campanha);
    }

    @Transactional(readOnly = true)
    public Optional<CampanhaMarketing> findById(Long id) {
        return campanhaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<CampanhaMarketing> findAll() {
        return campanhaRepository.findAll();
    }

    public void deleteById(Long id) {
        campanhaRepository.findById(id).ifPresent(campanha -> {
            if (campanha.isEmAndamento()) {
                throw new IllegalStateException("Não é possível excluir uma campanha em andamento");
            }
            campanhaRepository.deleteById(id);
        });
    }

    // Campaign Management Operations
    @Transactional
    public CampanhaMarketing agendarCampanha(Long id, LocalDate dataInicio, LocalDate dataFim) {
        CampanhaMarketing campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (!campanha.podeSerEnviada()) {
            throw new IllegalStateException("Campanha não pode ser agendada no status atual: " + campanha.getStatus());
        }

        campanha.setDataInicio(dataInicio);
        campanha.setDataFim(dataFim);
        campanha.setStatus(CampanhaMarketing.StatusCampanha.AGENDADA);

        return campanhaRepository.save(campanha);
    }

    @Transactional
    public CampanhaMarketing iniciarCampanha(Long id, Usuario usuarioResponsavel) {
        CampanhaMarketing campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (!campanha.podeSerEnviada()) {
            throw new IllegalStateException("Campanha não pode ser iniciada no status atual: " + campanha.getStatus());
        }

        campanha.setStatus(CampanhaMarketing.StatusCampanha.EM_ANDAMENTO);
        campanha.setUsuarioResponsavel(usuarioResponsavel);
        campanha.setDataEnvio(LocalDateTime.now());

        // Start sending to target audience
        enviarParaPublicoAlvo(campanha);

        return campanhaRepository.save(campanha);
    }

    @Transactional
    public CampanhaMarketing pausarCampanha(Long id, String motivo) {
        CampanhaMarketing campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (!campanha.podeSerPausada()) {
            throw new IllegalStateException("Campanha não pode ser pausada no status atual: " + campanha.getStatus());
        }

        campanha.setStatus(CampanhaMarketing.StatusCampanha.PAUSADA);
        campanha.setObservacoes(motivo);

        return campanhaRepository.save(campanha);
    }

    @Transactional
    public CampanhaMarketing retomarCampanha(Long id) {
        CampanhaMarketing campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (!campanha.podeSerRetomada()) {
            throw new IllegalStateException("Campanha não pode ser retomada no status atual: " + campanha.getStatus());
        }

        campanha.setStatus(CampanhaMarketing.StatusCampanha.EM_ANDAMENTO);

        return campanhaRepository.save(campanha);
    }

    @Transactional
    public CampanhaMarketing finalizarCampanha(Long id, String observacoes) {
        CampanhaMarketing campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (campanha.isFinalizada() || campanha.isCancelada()) {
            throw new IllegalStateException("Campanha já está finalizada ou cancelada");
        }

        campanha.setStatus(CampanhaMarketing.StatusCampanha.FINALIZADA);
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            campanha.setObservacoes(observacoes);
        }

        // Calculate final metrics
        calcularMetricasFinais(campanha);

        return campanhaRepository.save(campanha);
    }

    @Transactional
    public CampanhaMarketing cancelarCampanha(Long id, String motivo, Usuario usuario) {
        CampanhaMarketing campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (!campanha.podeSerCancelada()) {
            throw new IllegalStateException("Campanha não pode ser cancelada no status atual: " + campanha.getStatus());
        }

        campanha.setStatus(CampanhaMarketing.StatusCampanha.CANCELADA);
        campanha.setObservacoes(motivo);

        return campanhaRepository.save(campanha);
    }

    // Target Audience Management
    @Transactional
    public CampanhaMarketing adicionarClientesPublicoAlvo(Long campanhaId, List<Long> clienteIds) {
        CampanhaMarketing campanha = campanhaRepository.findById(campanhaId)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        if (!campanha.podeSerEditada()) {
            throw new IllegalStateException("Não é possível modificar o público-alvo de uma campanha em andamento");
        }

        List<Cliente> clientes = clienteRepository.findAllById(clienteIds);

        for (Cliente cliente : clientes) {
            CampanhaCliente campanhaCliente = new CampanhaCliente();
            campanhaCliente.setCampanha(campanha);
            campanhaCliente.setCliente(cliente);
            campanhaCliente.setStatusEnvio(CampanhaCliente.StatusEnvio.PENDENTE);

            if (campanha.getClientes() == null) {
                campanha.setClientes(new java.util.ArrayList<>());
            }
            campanha.getClientes().add(campanhaCliente);
        }

        campanha.setPublicoAlvo(campanha.getClientes().size());

        return campanhaRepository.save(campanha);
    }

    @Transactional
    public CampanhaMarketing definirPublicoAlvoPorSegmento(Long campanhaId, String segmento) {
        CampanhaMarketing campanha = campanhaRepository.findById(campanhaId)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));

        List<Cliente> clientes;
        switch (segmento.toUpperCase()) {
            case "TODOS":
                clientes = clienteRepository.findByAtivoTrue();
                break;
            case "VIP":
                clientes = clienteRepository.findClientesVIP();
                break;
            case "NOVOS":
                clientes = clienteRepository.findClientesNovos(LocalDate.now().minusMonths(3));
                break;
            case "INATIVOS":
                clientes = clienteRepository.findClientesInativos(LocalDate.now().minusMonths(6));
                break;
            default:
                throw new IllegalArgumentException("Segmento não reconhecido: " + segmento);
        }

        List<Long> clienteIds = clientes.stream().map(Cliente::getId).collect(Collectors.toList());
        return adicionarClientesPublicoAlvo(campanhaId, clienteIds);
    }

    // Analytics and Reporting
    @Transactional(readOnly = true)
    public List<CampanhaMarketing> findByStatus(CampanhaMarketing.StatusCampanha status) {
        return campanhaRepository.findByStatusOrderByDataCriacaoDesc(status);
    }

    @Transactional(readOnly = true)
    public List<CampanhaMarketing> findCampanhasAtivas() {
        return campanhaRepository.findCampanhasAtivas(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<CampanhaMarketing> findCampanhasExpiradas() {
        return campanhaRepository.findCampanhasExpiradas(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<CampanhaMarketing> findCampanhasMaisLucrativas() {
        return campanhaRepository.findCampanhasMaisLucrativas();
    }

    @Transactional(readOnly = true)
    public Map<CampanhaMarketing.StatusCampanha, Long> getEstatisticasPorStatus() {
        return List.of(CampanhaMarketing.StatusCampanha.values())
                .stream()
                .collect(Collectors.toMap(
                        status -> status,
                        status -> campanhaRepository.countByStatus(status)));
    }

    @Transactional(readOnly = true)
    public Map<CampanhaMarketing.TipoCampanha, Long> getEstatisticasPorTipo() {
        List<Object[]> results = campanhaRepository.countByTipo();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (CampanhaMarketing.TipoCampanha) row[0],
                        row -> (Long) row[1]));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularROITotal() {
        BigDecimal custoTotal = campanhaRepository.sumOrcamentoGasto();
        BigDecimal receitaTotal = campanhaRepository.sumReceitaGeradaByPeriodo(
                LocalDate.now().minusYears(1), LocalDate.now());

        if (custoTotal == null || custoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return receitaTotal.subtract(custoTotal)
                .divide(custoTotal, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPerformancePorTipo(LocalDate inicio, LocalDate fim) {
        return campanhaRepository.getPerformancePorTipo(inicio, fim);
    }

    // Automated Campaign Management
    @Transactional
    public void processarCampanhasAgendadas() {
        LocalDate hoje = LocalDate.now();
        List<CampanhaMarketing> campanhasParaIniciar = campanhaRepository.findCampanhasParaIniciarAmanha(hoje);

        for (CampanhaMarketing campanha : campanhasParaIniciar) {
            try {
                iniciarCampanha(campanha.getId(), campanha.getUsuarioResponsavel());
            } catch (Exception e) {
                // Log error but continue processing other campaigns
                System.err.println("Erro ao iniciar campanha " + campanha.getId() + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    public void finalizarCampanhasExpiradas() {
        List<CampanhaMarketing> campanhasExpiradas = findCampanhasExpiradas();

        for (CampanhaMarketing campanha : campanhasExpiradas) {
            try {
                finalizarCampanha(campanha.getId(), "Finalizada automaticamente por expiração");
            } catch (Exception e) {
                // Log error but continue processing other campaigns
                System.err.println("Erro ao finalizar campanha " + campanha.getId() + ": " + e.getMessage());
            }
        }
    }

    // paginação
    @Transactional(readOnly = true)
    public Page<CampanhaMarketing> findAll(Pageable pageable) {
        return campanhaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<CampanhaMarketing> findByStatus(StatusCampanha status, Pageable pageable) {
        return campanhaRepository.findByStatus(status, pageable);
    }

    // Private helper methods
    private void validarCampanha(CampanhaMarketing campanha) {
        if (campanha.getNome() == null || campanha.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da campanha é obrigatório");
        }

        if (campanha.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início é obrigatória");
        }

        if (campanha.getDataFim() != null && campanha.getDataFim().isBefore(campanha.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }

        if (campanha.getId() == null && campanhaRepository.existsByNome(campanha.getNome())) {
            throw new IllegalArgumentException("Já existe uma campanha com este nome");
        }
    }

    private void enviarParaPublicoAlvo(CampanhaMarketing campanha) {
        if (campanha.getClientes() == null || campanha.getClientes().isEmpty()) {
            return;
        }

        for (CampanhaCliente campanhaCliente : campanha.getClientes()) {
            if (campanhaCliente.isPendente()) {
                try {
                    enviarCampanhaParaCliente(campanha, campanhaCliente);
                    campanhaCliente.marcarComoEnviado();
                } catch (Exception e) {
                    campanhaCliente.marcarComoFalha("Erro no envio: " + e.getMessage());
                }
            }
        }
    }

    private void enviarCampanhaParaCliente(CampanhaMarketing campanha, CampanhaCliente campanhaCliente) {
        switch (campanha.getTipo()) {
            case EMAIL:
                enviarEmail(campanha, campanhaCliente.getCliente());
                break;
            case SMS:
                enviarSMS(campanha, campanhaCliente.getCliente());
                break;
            case WHATSAPP:
                enviarWhatsApp(campanha, campanhaCliente.getCliente());
                break;
            default:
                throw new UnsupportedOperationException(
                        "Tipo de campanha não suportado para envio automático: " + campanha.getTipo());
        }
    }

    private void enviarEmail(CampanhaMarketing campanha, Cliente cliente) {
        try {
            emailService.enviarEmailMarketing(
                    cliente.getEmail(),
                    campanha.getAssunto(),
                    campanha.getConteudo(),
                    campanha.getId());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email: " + e.getMessage(), e);
        }
    }

    private void enviarSMS(CampanhaMarketing campanha, Cliente cliente) {
        // Implementation for SMS sending would go here
        // For now, just log the action
        System.out.println("SMS enviado para " + cliente.getTelefone() + ": " + campanha.getConteudo());
    }

    private void enviarWhatsApp(CampanhaMarketing campanha, Cliente cliente) {
        // Implementation for WhatsApp sending would go here
        // For now, just log the action
        System.out.println("WhatsApp enviado para " + cliente.getTelefone() + ": " + campanha.getConteudo());
    }

    private void calcularMetricasFinais(CampanhaMarketing campanha) {
        if (campanha.getClientes() != null) {
            int enviados = (int) campanha.getClientes().stream().mapToLong(c -> c.isEnviado() ? 1 : 0).sum();
            int cliques = (int) campanha.getClientes().stream().mapToLong(c -> c.isClicou() ? 1 : 0).sum();
            int conversoes = (int) campanha.getClientes().stream().mapToLong(c -> c.isConverteu() ? 1 : 0).sum();
            int vendas = (int) campanha.getClientes().stream().mapToLong(c -> c.isComprou() ? 1 : 0).sum();

            campanha.setAlcanceEfetivo(enviados);
            campanha.setCliques(cliques);
            campanha.setConversoes(conversoes);
            campanha.setVendas(vendas);
        }
    }
}