package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.*;
import com.jaasielsilva.portalceo.repository.ContratoLegalRepository;
import com.jaasielsilva.portalceo.repository.ContratoAditivoRepository;
import com.jaasielsilva.portalceo.repository.ContratoAlertaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContratoLegalService {

    @Autowired
    public ContratoLegalRepository contratoRepository;
    @Autowired
    public ContratoAditivoRepository aditivoRepository;
    @Autowired
    public ContratoAlertaRepository alertaRepository;
    @Autowired
    public NotificationService notificationService;
    @Autowired
    private AutentiqueService autentiqueService;

    // CRUD Operations
    public ContratoLegal save(ContratoLegal contrato) {
        validarContrato(contrato);

        if (contrato.getId() == null) {
            contrato.setDataCriacao(LocalDate.now());
            contrato.setStatus(ContratoLegal.StatusContrato.RASCUNHO);

            // Generate contract number if not provided
            if (contrato.getNumeroContrato() == null || contrato.getNumeroContrato().trim().isEmpty()) {
                contrato.setNumeroContrato(gerarNumeroContrato());
            }
        }

        ContratoLegal contratoSalvo = contratoRepository.save(contrato);

        // Create automatic alerts for important dates
        criarAlertasAutomaticos(contratoSalvo);

        return contratoSalvo;
    }

    @Transactional(readOnly = true)
    public Optional<ContratoLegal> findById(Long id) {
        return contratoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ContratoLegal> findByNumeroContrato(String numeroContrato) {
        return contratoRepository.findByNumeroContrato(numeroContrato);
    }

    @Transactional(readOnly = true)
    public List<ContratoLegal> findAll() {
        return contratoRepository.findAll();
    }

    public void deleteById(Long id) {
        contratoRepository.findById(id).ifPresent(contrato -> {
            if (contrato.isAtivo() || contrato.isAssinado()) {
                throw new IllegalStateException("Não é possível excluir um contrato ativo ou assinado");
            }
            contratoRepository.deleteById(id);
        });
    }

    // Contract Lifecycle Management
    @Transactional
    public ContratoLegal enviarParaAnalise(Long id, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.isRascunho()) {
            throw new IllegalStateException("Apenas contratos em rascunho podem ser enviados para análise");
        }

        contrato.setStatus(ContratoLegal.StatusContrato.EM_ANALISE);
        contrato.setUsuarioResponsavel(usuario);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoLegal aprovarContrato(Long id, String observacoes, Usuario usuarioAprovacao) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerAprovado()) {
            throw new IllegalStateException("Contrato não pode ser aprovado no status atual: " + contrato.getStatus());
        }

        contrato.setStatus(ContratoLegal.StatusContrato.APROVADO);
        contrato.setDescricao(contrato.getDescricao() + "\nObservações da aprovação: " + observacoes);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoLegal assinarContrato(Long id, LocalDate dataAssinatura, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerAssinado()) {
            throw new IllegalStateException("Contrato não pode ser assinado no status atual: " + contrato.getStatus());
        }

        contrato.setStatus(ContratoLegal.StatusContrato.ASSINADO);
        contrato.setDataAssinatura(dataAssinatura);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoLegal enviarParaAssinatura(Long id) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerAssinado()) {
            throw new IllegalStateException(
                    "Contrato não pode ser enviado para assinatura no status atual: " + contrato.getStatus());
        }

        if (contrato.getCliente() == null || contrato.getCliente().getEmail() == null) {
            throw new IllegalArgumentException("Cliente do contrato não possui e-mail cadastrado para assinatura");
        }

        if (contrato.getCaminhoArquivo() == null) {
            throw new IllegalArgumentException("Documento PDF do contrato não encontrado para envio");
        }

        Map<String, String> resultado = autentiqueService.enviarDocumento(
                contrato.getCaminhoArquivo(),
                contrato.getTitulo(),
                contrato.getCliente().getEmail());

        if (resultado != null) {
            contrato.setAutentiqueId(resultado.get("id"));
            contrato.setLinkAssinatura(resultado.get("link"));
            contrato.setLinkAssinaturaEmpresa(resultado.get("linkEmpresa"));
            return contratoRepository.save(contrato);
        } else {
            throw new RuntimeException("Falha ao enviar documento para a Autentique");
        }
    }

    @Transactional
    public ContratoLegal sincronizarStatusAutentique(Long id) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (contrato.getAutentiqueId() == null) {
            throw new IllegalStateException("Contrato não possui vínculo com a Autentique");
        }

        boolean assinado = autentiqueService.verificarSeEstaAssinado(contrato.getAutentiqueId());

        if (assinado) {
            contrato.setStatus(ContratoLegal.StatusContrato.ASSINADO);
            contrato.setDataAssinatura(LocalDate.now());
            return contratoRepository.save(contrato);
        }

        return contrato;
    }

    @Transactional
    public ContratoLegal ativarContrato(Long id, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerAtivado()) {
            throw new IllegalStateException("Contrato não pode ser ativado no status atual: " + contrato.getStatus());
        }

        contrato.setStatus(ContratoLegal.StatusContrato.ATIVO);

        // Calculate end date and renewal date
        if (contrato.getDuracaoMeses() != null) {
            contrato.setDataFim(contrato.getDataInicio().plusMonths(contrato.getDuracaoMeses()));
            contrato.setDataVencimento(contrato.getDataFim());

            if (contrato.getRenovacaoAutomatica()) {
                contrato.setDataRenovacao(contrato.getDataVencimento().minusDays(contrato.getPrazoNotificacao()));
            }
        }

        ContratoLegal contratoAtivado = contratoRepository.save(contrato);

        // Create renewal alerts if applicable
        if (contrato.getRenovacaoAutomatica()) {
            criarAlertaRenovacao(contratoAtivado);
        }

        return contratoAtivado;
    }

    @Transactional
    public ContratoLegal suspenderContrato(Long id, String motivo, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerSuspenso()) {
            throw new IllegalStateException("Contrato não pode ser suspenso no status atual: " + contrato.getStatus());
        }

        contrato.setStatus(ContratoLegal.StatusContrato.SUSPENSO);
        contrato.setDescricao(contrato.getDescricao() + "\nMotivo da suspensão: " + motivo);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoLegal reativarContrato(Long id, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerReativado()) {
            throw new IllegalStateException("Contrato não pode ser reativado no status atual: " + contrato.getStatus());
        }

        contrato.setStatus(ContratoLegal.StatusContrato.ATIVO);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoLegal rescindirContrato(Long id, String motivo, LocalDate dataRescisao, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.podeSerRescindido()) {
            throw new IllegalStateException(
                    "Contrato não pode ser rescindido no status atual: " + contrato.getStatus());
        }

        contrato.setStatus(ContratoLegal.StatusContrato.RESCINDIDO);
        contrato.setDataFim(dataRescisao);
        contrato.setCondicoesRescisao(motivo);

        return contratoRepository.save(contrato);
    }

    @Transactional
    public ContratoLegal renovarContrato(Long id, Integer novasDuracaoMeses, BigDecimal novoValor, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        if (!contrato.isAtivo()) {
            throw new IllegalStateException("Apenas contratos ativos podem ser renovados");
        }

        // Create renewal addendum
        ContratoAditivo aditivo = new ContratoAditivo();
        aditivo.setContrato(contrato);
        aditivo.setNumeroAditivo("REN-" + System.currentTimeMillis());
        aditivo.setTipo(ContratoAditivo.TipoAditivo.RENOVACAO);
        aditivo.setTitulo("Renovação Automática do Contrato");
        aditivo.setDescricao("Renovação automática conforme cláusula contratual");
        aditivo.setDataCriacao(LocalDate.now());
        aditivo.setDataVigencia(contrato.getDataVencimento().plusDays(1));
        aditivo.setStatus(ContratoAditivo.StatusAditivo.VIGENTE);
        aditivo.setDuracaoAnterior(contrato.getDuracaoMeses());
        aditivo.setDuracaoNova(novasDuracaoMeses);
        aditivo.setValorAnterior(contrato.getValorMensal());
        aditivo.setValorNovo(novoValor);
        aditivo.setUsuarioCriacao(usuario);

        aditivoRepository.save(aditivo);

        // Update contract
        contrato.setDuracaoMeses(novasDuracaoMeses);
        contrato.setValorMensal(novoValor);
        contrato.setDataInicio(contrato.getDataVencimento().plusDays(1));
        contrato.setDataFim(contrato.getDataInicio().plusMonths(novasDuracaoMeses));
        contrato.setDataVencimento(contrato.getDataFim());
        contrato.setDataRenovacao(contrato.getDataVencimento().minusDays(contrato.getPrazoNotificacao()));

        return contratoRepository.save(contrato);
    }

    // Addendum Management
    @Transactional
    public ContratoAditivo criarAditivo(Long contratoId, ContratoAditivo aditivo, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        aditivo.setContrato(contrato);
        aditivo.setUsuarioCriacao(usuario);
        aditivo.setDataCriacao(LocalDate.now());

        return aditivoRepository.save(aditivo);
    }

    // Alert Management
    @Transactional
    public ContratoAlerta criarAlerta(Long contratoId, ContratoAlerta alerta, Usuario usuario) {
        ContratoLegal contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        alerta.setContrato(contrato);
        alerta.setUsuarioResponsavel(usuario);
        alerta.setDataAlerta(LocalDate.now());

        return alertaRepository.save(alerta);
    }

    // Query Operations
    @Transactional(readOnly = true)
    public List<ContratoLegal> findByStatus(ContratoLegal.StatusContrato status) {
        return contratoRepository.findByStatusOrderByDataCriacaoDesc(status);
    }

    @Transactional(readOnly = true)
    public List<ContratoLegal> findContratosVencidos() {
        return contratoRepository.findContratosVencidos(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<ContratoLegal> findContratosVencendoEm(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate futuro = hoje.plusDays(dias);
        return contratoRepository.findContratosVencendoEm(hoje, futuro);
    }

    @Transactional(readOnly = true)
    public List<ContratoLegal> findContratosParaRenovacao(int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate futuro = hoje.plusDays(dias);
        return contratoRepository.findContratosParaRenovacao(hoje, futuro);
    }

    @Transactional(readOnly = true)
    public List<ContratoLegal> findContratosComAlertas() {
        return contratoRepository.findContratosComAlertas();
    }

    // Analytics and Reporting
    @Transactional(readOnly = true)
    public Map<ContratoLegal.StatusContrato, Long> getEstatisticasPorStatus() {
        return List.of(ContratoLegal.StatusContrato.values())
                .stream()
                .collect(Collectors.toMap(
                        status -> status,
                        status -> contratoRepository.countByStatus(status)));
    }

    // Métodos adicionais necessários para o controller
    @Transactional(readOnly = true)
    public Page<ContratoLegal> buscarContratosComFiltros(String numero, ContratoLegal.StatusContrato status,
            ContratoLegal.TipoContrato tipo, Pageable pageable) {
        if (numero != null && !numero.trim().isEmpty()) {
            return contratoRepository.findByNumeroContratoContainingIgnoreCase(numero, pageable);
        }
        if (status != null && tipo != null) {
            return contratoRepository.findByStatusAndTipo(status, tipo, pageable);
        }
        if (status != null) {
            return contratoRepository.findByStatus(status, pageable);
        }
        if (tipo != null) {
            return contratoRepository.findByTipo(tipo, pageable);
        }
        return contratoRepository.findAll(pageable);
    }

    @Transactional
    public ContratoLegal salvarContrato(ContratoLegal contrato) {
        return save(contrato);
    }

    @Transactional(readOnly = true)
    public ContratoLegal buscarPorId(Long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> gerarRelatorioVencimentos(int proximosDias) {
        List<ContratoLegal> contratosVencendo = findContratosVencendoEm(proximosDias);
        Map<String, Object> relatorio = new java.util.HashMap<>();
        relatorio.put("contratos", contratosVencendo);
        relatorio.put("total", contratosVencendo.size());
        relatorio.put("proximosDias", proximosDias);
        return relatorio;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticasGerais(LocalDate dataInicio, LocalDate dataFim) {
        Map<String, Object> estatisticas = new java.util.HashMap<>();
        estatisticas.put("totalContratos", contratoRepository.count());
        estatisticas.put("contratosAtivos", contratoRepository.countByStatus(ContratoLegal.StatusContrato.ATIVO));
        estatisticas.put("contratosVencidos", contratoRepository.countByStatus(ContratoLegal.StatusContrato.VENCIDO));
        estatisticas.put("valorTotalAtivos", calcularValorTotalAtivos());
        estatisticas.put("receitaMensal", calcularReceitaMensalRecorrente());
        return estatisticas;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterTimelineContrato(Long contratoId) {
        ContratoLegal contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        List<Map<String, Object>> timeline = new java.util.ArrayList<>();

        // Adicionar eventos do contrato
        Map<String, Object> criacao = new java.util.HashMap<>();
        criacao.put("data", contrato.getDataCriacao());
        criacao.put("evento", "Contrato criado");
        criacao.put("status", "RASCUNHO");
        timeline.add(criacao);

        if (contrato.getDataAssinatura() != null) {
            Map<String, Object> assinatura = new java.util.HashMap<>();
            assinatura.put("data", contrato.getDataAssinatura());
            assinatura.put("evento", "Contrato assinado");
            assinatura.put("status", "ASSINADO");
            timeline.add(assinatura);
        }

        Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("eventos", timeline);
        resultado.put("contratoId", contratoId);
        resultado.put("total", timeline.size());
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<ContratoLegal> buscarContratosVencendoEm(int proximosDias) {
        LocalDate hoje = LocalDate.now();
        LocalDate futuro = hoje.plusDays(proximosDias);
        return contratoRepository.findContratosVencendoEm(hoje, futuro);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> gerarRelatorioContratos(LocalDate dataInicio, LocalDate dataFim,
            ContratoLegal.TipoContrato tipo,
            ContratoLegal.StatusContrato status) {
        Map<String, Object> relatorio = new java.util.HashMap<>();

        List<ContratoLegal> contratos;
        if (tipo != null && status != null) {
            contratos = contratoRepository.findByTipoAndStatus(tipo, status);
        } else if (tipo != null) {
            contratos = contratoRepository.findByTipoOrderByDataCriacaoDesc(tipo);
        } else if (status != null) {
            contratos = contratoRepository.findByStatusOrderByDataCriacaoDesc(status);
        } else {
            contratos = contratoRepository.findAll();
        }

        // Filtrar por data se especificado
        if (dataInicio != null && dataFim != null) {
            contratos = contratos.stream()
                    .filter(c -> c.getDataCriacao().isAfter(dataInicio.minusDays(1)) &&
                            c.getDataCriacao().isBefore(dataFim.plusDays(1)))
                    .collect(Collectors.toList());

        }

        relatorio.put("contratos", contratos);
        relatorio.put("total", contratos.size());
        relatorio.put("dataInicio", dataInicio);
        relatorio.put("dataFim", dataFim);
        relatorio.put("tipo", tipo);
        relatorio.put("status", status);

        return relatorio;
    }

    @Transactional(readOnly = true)
    public Map<ContratoLegal.TipoContrato, Long> getEstatisticasPorTipo() {
        List<Object[]> results = contratoRepository.getContratosPorTipoGrouped();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (ContratoLegal.TipoContrato) row[0],
                        row -> (Long) row[1]));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularValorTotalAtivos() {
        BigDecimal valor = contratoRepository.sumValorContratoByStatus(ContratoLegal.StatusContrato.ATIVO);
        return valor != null ? valor : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularReceitaMensalRecorrente() {
        BigDecimal valor = contratoRepository.sumValorMensalAtivos();
        return valor != null ? valor : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getEstatisticasPorTipoDetalhadas() {
        return contratoRepository.getEstatisticasPorTipo();
    }

    // Automated Contract Management
    @Transactional
    public void processarContratosVencidos() {
        List<ContratoLegal> contratosVencidos = findContratosVencidos();

        for (ContratoLegal contrato : contratosVencidos) {
            if (contrato.isAtivo()) {
                contrato.setStatus(ContratoLegal.StatusContrato.VENCIDO);
                contratoRepository.save(contrato);

                // Create alert for expired contract
                criarAlertaVencimento(contrato);
            }
        }
    }

    @Transactional
    public void processarRenovacoes() {
        List<ContratoLegal> contratosParaRenovacao = findContratosParaRenovacao(30);

        for (ContratoLegal contrato : contratosParaRenovacao) {
            if (contrato.getRenovacaoAutomatica() && contrato.precisaNotificacaoRenovacao()) {
                // Create renewal notification
                notificationService.enviarNotificacaoRenovacao(contrato);

                // Create renewal alert
                criarAlertaRenovacao(contrato);
            }
        }
    }

    // Private helper methods
    private void validarContrato(ContratoLegal contrato) {
        if (contrato.getTitulo() == null || contrato.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("Título do contrato é obrigatório");
        }

        if (contrato.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início é obrigatória");
        }

        if (contrato.getDataFim() != null && contrato.getDataFim().isBefore(contrato.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início");
        }

        if (contrato.getNumeroContrato() != null && contrato.getId() == null &&
                contratoRepository.existsByNumeroContrato(contrato.getNumeroContrato())) {
            throw new IllegalArgumentException("Já existe um contrato com este número");
        }
    }

    private String gerarNumeroContrato() {
        String ano = String.valueOf(LocalDate.now().getYear());
        Long count = contratoRepository.count() + 1;
        return String.format("CTR-%s-%04d", ano, count);
    }

    private void criarAlertasAutomaticos(ContratoLegal contrato) {
        if (contrato.getDataVencimento() != null) {
            // Create alert 30 days before expiration
            ContratoAlerta alerta = new ContratoAlerta();
            alerta.setContrato(contrato);
            alerta.setTipo(ContratoAlerta.TipoAlerta.VENCIMENTO_CONTRATO);
            alerta.setPrioridade(ContratoAlerta.PrioridadeAlerta.ALTA);
            alerta.setTitulo("Contrato vencendo em 30 dias");
            alerta.setDescricao("O contrato " + contrato.getNumeroContrato() + " vencerá em 30 dias");
            alerta.setDataAlerta(contrato.getDataVencimento().minusDays(30));
            alerta.setDataVencimento(contrato.getDataVencimento());
            alerta.setAcaoRecomendada("Verificar necessidade de renovação ou rescisão");
            alerta.setUsuarioResponsavel(contrato.getUsuarioResponsavel());

            alertaRepository.save(alerta);
        }
    }

    private void criarAlertaRenovacao(ContratoLegal contrato) {
        ContratoAlerta alerta = new ContratoAlerta();
        alerta.setContrato(contrato);
        alerta.setTipo(ContratoAlerta.TipoAlerta.RENOVACAO_CONTRATO);
        alerta.setPrioridade(ContratoAlerta.PrioridadeAlerta.MEDIA);
        alerta.setTitulo("Renovação de contrato necessária");
        alerta.setDescricao("O contrato " + contrato.getNumeroContrato() + " precisa ser renovado");
        alerta.setDataAlerta(LocalDate.now());
        alerta.setDataVencimento(contrato.getDataVencimento());
        alerta.setAcaoRecomendada("Preparar documentação para renovação");
        alerta.setUsuarioResponsavel(contrato.getUsuarioResponsavel());

        alertaRepository.save(alerta);
    }

    private void criarAlertaVencimento(ContratoLegal contrato) {
        ContratoAlerta alerta = new ContratoAlerta();
        alerta.setContrato(contrato);
        alerta.setTipo(ContratoAlerta.TipoAlerta.VENCIMENTO_CONTRATO);
        alerta.setPrioridade(ContratoAlerta.PrioridadeAlerta.CRITICA);
        alerta.setTitulo("Contrato vencido");
        alerta.setDescricao("O contrato " + contrato.getNumeroContrato() + " está vencido");
        alerta.setDataAlerta(LocalDate.now());
        alerta.setAcaoRecomendada("Tomar ação imediata: renovar ou rescindir");
        alerta.setUsuarioResponsavel(contrato.getUsuarioResponsavel());

        alertaRepository.save(alerta);
    }
}