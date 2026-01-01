package com.jaasielsilva.portalceo.juridico.previdenciario.processo.service;

import com.jaasielsilva.portalceo.juridico.previdenciario.historico.service.HistoricoProcessoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciarioStatus;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository.ProcessoPrevidenciarioRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.model.Cliente;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoDecisaoResultado;
import com.jaasielsilva.portalceo.service.ContaReceberService;
import com.jaasielsilva.portalceo.model.ContaReceber;
import org.springframework.dao.DataIntegrityViolationException;

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
        conta.setCategoria(ContaReceber.CategoriaContaReceber.SERVICO);
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
}
