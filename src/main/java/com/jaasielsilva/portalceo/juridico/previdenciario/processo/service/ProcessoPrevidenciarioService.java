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

@Service
@RequiredArgsConstructor
public class ProcessoPrevidenciarioService {

    private final ProcessoPrevidenciarioRepository processoRepository;
    private final ClienteService clienteService;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoProcessoService historicoProcessoService;

    @Transactional(readOnly = true)
    public List<ProcessoPrevidenciario> listar() {
        return processoRepository.findAllByOrderByDataAberturaDesc();
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
}
