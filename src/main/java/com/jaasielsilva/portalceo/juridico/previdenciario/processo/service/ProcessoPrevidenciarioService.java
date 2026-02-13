package com.jaasielsilva.portalceo.juridico.previdenciario.processo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaasielsilva.portalceo.juridico.previdenciario.historico.service.HistoricoProcessoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoDecisaoResultado;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciarioStatus;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository.ProcessoPrevidenciarioRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.ContaReceber;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.ClienteService;
import com.jaasielsilva.portalceo.service.ContaReceberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessoPrevidenciarioService {

    private final ProcessoPrevidenciarioRepository processoRepository;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoProcessoService historicoProcessoService;
    private final ContaReceberService contaReceberService;

    @Transactional(readOnly = true)
    public List<ProcessoPrevidenciario> listar() {
        return processoRepository.findAllByOrderByDataAberturaDesc();
    }

    @Transactional(readOnly = true)
    public Page<ProcessoPrevidenciario> buscarComFiltros(String status, String search, Pageable pageable) {
        ProcessoPrevidenciarioStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = ProcessoPrevidenciarioStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                // ignore invalid status
            }
        }
        return processoRepository.buscarComFiltros(statusEnum, search, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProcessoPrevidenciario> listarPorCliente(Long clienteId) {
        return processoRepository.findByCliente_IdOrderByDataAberturaDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public ProcessoPrevidenciario buscarPorId(Long id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
    }

    @Transactional
    public ProcessoPrevidenciario criar(Long clienteId, Long responsavelId, Usuario usuarioExecutor) {
        Cliente cliente = clienteService.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        Usuario responsavel = usuarioRepository.findById(responsavelId)
                .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado"));

        ProcessoPrevidenciario p = new ProcessoPrevidenciario();
        p.setCliente(cliente);
        p.setResponsavel(responsavel);
        p.setStatusAtual(ProcessoPrevidenciarioStatus.ABERTO);
        p.setEtapaAtual(EtapaWorkflowCodigo.CADASTRO);
        p.setDataAbertura(LocalDateTime.now());

        ProcessoPrevidenciario salvo = processoRepository.save(p);
        historicoProcessoService.registrar(salvo, "ABERTURA_PROCESSO", usuarioExecutor,
                "Processo previdenciário criado");
        return salvo;
    }

    @Transactional
    public ProcessoPrevidenciario atualizarDadosBasicos(Long processoId, Long clienteId, Long responsavelId,
            Usuario usuarioExecutor) {
        ProcessoPrevidenciario existente = buscarPorId(processoId);
        if (existente.getStatusAtual() == ProcessoPrevidenciarioStatus.ENCERRADO) {
            throw new IllegalStateException("Processo encerrado não permite alterações");
        }
        Cliente cliente = clienteService.buscarPorId(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        Usuario responsavel = usuarioRepository.findById(responsavelId)
                .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado"));

        boolean mudouResponsavel = existente.getResponsavel() != null && responsavelId != null
                && !existente.getResponsavel().getId().equals(responsavelId);

        existente.setCliente(cliente);
        existente.setResponsavel(responsavel);

        ProcessoPrevidenciario atualizado = processoRepository.save(existente);
        if (mudouResponsavel) {
            historicoProcessoService.registrar(atualizado, "TROCA_RESPONSAVEL", usuarioExecutor,
                    "Responsável atualizado");
        }
        return atualizado;
    }

    @Transactional
    public ProcessoPrevidenciario atualizarProtocoloInss(Long processoId,
            String numeroProtocolo,
            LocalDate dataProtocolo,
            String urlMeuInss,
            Usuario usuarioExecutor) {
        ProcessoPrevidenciario existente = buscarPorId(processoId);
        if (existente.getStatusAtual() == ProcessoPrevidenciarioStatus.ENCERRADO) {
            throw new IllegalStateException("Processo encerrado não permite alterações");
        }
        if (existente.getEtapaAtual() != EtapaWorkflowCodigo.PROTOCOLO_INSS) {
            throw new IllegalStateException("Dados de protocolo só podem ser editados na etapa PROTOCOLO_INSS");
        }

        String numero = numeroProtocolo != null ? numeroProtocolo.trim() : null;
        String url = urlMeuInss != null ? urlMeuInss.trim() : null;

        existente.setNumeroProtocolo((numero != null && !numero.isBlank()) ? numero : null);
        existente.setDataProtocolo(dataProtocolo);
        existente.setUrlMeuInss((url != null && !url.isBlank()) ? url : null);

        ProcessoPrevidenciario atualizado = processoRepository.save(existente);
        historicoProcessoService.registrar(atualizado, "ATUALIZACAO_PROTOCOLO_INSS", usuarioExecutor,
                "Dados do protocolo atualizados");
        return atualizado;
    }

    @Transactional
    public ProcessoPrevidenciario atualizarDecisao(Long processoId,
            ProcessoDecisaoResultado resultado,
            Boolean ganhouCausa,
            BigDecimal valorCausa,
            BigDecimal valorConcedido,
            LocalDate dataDecisao,
            String observacao,
            Usuario usuarioExecutor) {
        ProcessoPrevidenciario existente = buscarPorId(processoId);
        if (existente.getStatusAtual() == ProcessoPrevidenciarioStatus.ENCERRADO) {
            throw new IllegalStateException("Processo encerrado não permite alterações");
        }
        if (existente.getEtapaAtual() != EtapaWorkflowCodigo.DECISAO) {
            throw new IllegalStateException("Decisão só pode ser registrada na etapa DECISAO");
        }
        existente.setResultadoDecisao(resultado);
        existente.setGanhouCausa(ganhouCausa != null ? ganhouCausa : null);
        existente.setValorCausa(valorCausa);
        existente.setValorConcedido(valorConcedido);
        existente.setDataDecisao(dataDecisao);
        existente.setObservacaoDecisao((observacao != null && !observacao.isBlank()) ? observacao.trim() : null);
        ProcessoPrevidenciario atualizado = processoRepository.save(existente);
        historicoProcessoService.registrar(atualizado, "DECISAO_PROCESSO", usuarioExecutor,
                resultado != null ? resultado.name() : "SEM_RESULTADO");

        if ((resultado == ProcessoDecisaoResultado.DEFERIDO || Boolean.TRUE.equals(ganhouCausa))
                && valorConcedido != null && valorConcedido.compareTo(BigDecimal.ZERO) > 0) {
            ContaReceber conta = new ContaReceber();
            conta.setDescricao("Previdenciário #" + atualizado.getId() + " - decisão");
            conta.setCliente(atualizado.getCliente());
            conta.setValorOriginal(valorConcedido);
            conta.setDataEmissao(LocalDate.now());
            conta.setDataVencimento(LocalDate.now().plusDays(30));
            conta.setTipo(ContaReceber.TipoContaReceber.SERVICO);
            conta.setCategoria(ContaReceber.CategoriaContaReceber.SERVICO);
            conta.setNumeroDocumento("PREV-" + atualizado.getId() + "-" + LocalDate.now());
            contaReceberService.save(conta, usuarioExecutor);
        }
        return atualizado;
    }

    @Transactional
    public ProcessoPrevidenciario reabrir(Long processoId,
            ProcessoPrevidenciarioStatus statusDestino,
            String justificativa,
            Usuario usuarioExecutor) {
        ProcessoPrevidenciario existente = buscarPorId(processoId);
        if (existente.getStatusAtual() != ProcessoPrevidenciarioStatus.ENCERRADO) {
            throw new IllegalStateException("Somente processos ENCERRADOS podem ser reabertos");
        }
        ProcessoPrevidenciarioStatus destino = statusDestino != null ? statusDestino : ProcessoPrevidenciarioStatus.EM_ANDAMENTO;
        existente.setStatusAtual(destino);
        existente.setDataEncerramento(null);
        ProcessoPrevidenciario atualizado = processoRepository.save(existente);
        historicoProcessoService.registrar(atualizado, "REABERTURA_PROCESSO", usuarioExecutor,
                (justificativa != null ? justificativa : "Sem justificativa") + " -> " + destino.name());
        return atualizado;
    }

    @Transactional
    public ProcessoPrevidenciario registrarGanho(Long processoId,
            BigDecimal valor,
            LocalDate vencimento,
            String numeroDocumento,
            String observacoes,
            Usuario usuarioExecutor) {
        ProcessoPrevidenciario existente = buscarPorId(processoId);
        ContaReceber conta = new ContaReceber();
        conta.setDescricao("Previdenciário #" + existente.getId() + " - ganho de causa");
        conta.setCliente(existente.getCliente());
        conta.setValorOriginal(valor);
        conta.setDataEmissao(LocalDate.now());
        conta.setDataVencimento(vencimento != null ? vencimento : LocalDate.now().plusDays(30));
        conta.setTipo(ContaReceber.TipoContaReceber.SERVICO);
        conta.setCategoria(ContaReceber.CategoriaContaReceber.JURIDICO);
        conta.setNumeroDocumento(numeroDocumento != null && !numeroDocumento.isBlank()
                ? numeroDocumento
                : ("PREV-" + existente.getId() + "-" + LocalDate.now()));
        contaReceberService.save(conta, usuarioExecutor);

        existente.setStatusAtual(ProcessoPrevidenciarioStatus.ENCERRADO);
        existente.setDataEncerramento(LocalDateTime.now());
        ProcessoPrevidenciario atualizado = processoRepository.save(existente);
        historicoProcessoService.registrar(atualizado, "GANHO_CAUSA", usuarioExecutor,
                observacoes != null ? observacoes : "Valor: " + (valor != null ? valor : "—"));
        return atualizado;
    }

    @Transactional
    public ProcessoPrevidenciario atualizarEnvioDocumentacao(Long processoId, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setDataEnvioDocumentacao(LocalDateTime.now());
        // p.setEtapaAtual(EtapaWorkflowCodigo.DOCUMENTACAO); // Assume workflow handles transition or already there
        historicoProcessoService.registrar(p, "ENVIO_DOCS", usuarioExecutor, "Documentação enviada");
        return processoRepository.save(p);
    }

    @Transactional
    public ProcessoPrevidenciario atualizarAnalise(Long processoId, boolean aprovado, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        if (aprovado) {
            p.setDataAnalise(LocalDateTime.now());
            p.setPendenciaAnalise(false);
            p.setEtapaAtual(EtapaWorkflowCodigo.CONTRATO); 
            historicoProcessoService.registrar(p, "ANALISE_OK", usuarioExecutor, "Análise validada - Avançou para Contrato");
        } else {
            p.setPendenciaAnalise(true);
            p.setDataPendenciaAnalise(LocalDate.now());
            historicoProcessoService.registrar(p, "ANALISE_PENDENTE", usuarioExecutor, "Pendência identificada na análise");
        }
        return processoRepository.save(p);
    }

    @Transactional
    public ProcessoPrevidenciario atualizarEnvioContrato(Long processoId, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setStatusContrato("ENVIADO");
        p.setDataEnvioContrato(LocalDateTime.now());
        historicoProcessoService.registrar(p, "CONTRATO_ENVIADO", usuarioExecutor, "Contrato enviado");
        return processoRepository.save(p);
    }

    @Transactional
    public ProcessoPrevidenciario atualizarAnalisePendente(Long processoId, LocalDate data, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setPendenciaAnalise(true);
        p.setDataPendenciaAnalise(data != null ? data : LocalDate.now());
        historicoProcessoService.registrar(p, "ANALISE_PENDENTE", usuarioExecutor, "Pendência identificada na análise");
        return processoRepository.save(p);
    }

    @Transactional
    public ProcessoPrevidenciario atualizarAssinaturaContrato(Long processoId, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setStatusContrato("ASSINADO");
        p.setDataAssinaturaContrato(LocalDateTime.now());
        p.setEtapaAtual(EtapaWorkflowCodigo.MEDICO);
        historicoProcessoService.registrar(p, "CONTRATO_ASSINADO", usuarioExecutor, "Contrato assinado - Avançou para Médico");
        return processoRepository.save(p);
    }

    @Transactional
    public ProcessoPrevidenciario atualizarPagamentoMedico(Long processoId, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setStatusMedico("PAGO");
        p.setDataPagamentoMedico(LocalDateTime.now());
        historicoProcessoService.registrar(p, "MEDICO_PAGO", usuarioExecutor, "Médico pago");
        return processoRepository.save(p);
    }

    @Transactional
    public ProcessoPrevidenciario atualizarLaudoMedico(Long processoId, Usuario usuarioExecutor) {
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setStatusMedico("LAUDO_EMITIDO");
        p.setDataLaudoMedico(LocalDateTime.now());
        p.setEtapaAtual(EtapaWorkflowCodigo.PROTOCOLO_INSS);
        historicoProcessoService.registrar(p, "LAUDO_MEDICO", usuarioExecutor, "Laudo emitido - Avançou para Protocolo INSS");
        return processoRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<ProcessoPrevidenciario> listarEncerradosDeferidos() {
        return processoRepository.findEncerradosDeferidos();
    }

    @Transactional(readOnly = true)
    public List<ProcessoPrevidenciario> listarMedicosAPagar() {
        return processoRepository.findMedicosAPagar();
    }

    @Transactional(readOnly = true)
    public List<ProcessoPrevidenciario> listarContratosPendentesAssinatura() {
        return processoRepository.findContratosPendentesAssinatura();
    }

    @Transactional
    public ProcessoPrevidenciario registrarValorMedicoPrevisto(Long processoId, BigDecimal valorPrevisto, Usuario usuarioExecutor, String observacao) {
        if (valorPrevisto == null || valorPrevisto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor previsto do médico deve ser maior que zero");
        }
        ProcessoPrevidenciario p = buscarPorId(processoId);
        p.setValorMedicoPrevisto(valorPrevisto);
        p.setStatusMedico("PENDENTE");
        historicoProcessoService.registrar(p, "VALOR_MEDICO_PREVISTO", usuarioExecutor,
                observacao != null ? observacao : ("Valor previsto: " + valorPrevisto));
        return processoRepository.save(p);
    }
}
