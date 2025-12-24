package com.jaasielsilva.portalceo.juridico.previdenciario.workflow.service;

import com.jaasielsilva.portalceo.juridico.previdenciario.historico.service.HistoricoProcessoService;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciario;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.entity.ProcessoPrevidenciarioStatus;
import com.jaasielsilva.portalceo.juridico.previdenciario.processo.repository.ProcessoPrevidenciarioRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflow;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.TransicaoWorkflow;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.repository.EtapaWorkflowRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.repository.TransicaoWorkflowRepository;
import com.jaasielsilva.portalceo.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final ProcessoPrevidenciarioRepository processoRepository;
    private final EtapaWorkflowRepository etapaWorkflowRepository;
    private final TransicaoWorkflowRepository transicaoWorkflowRepository;
    private final HistoricoProcessoService historicoProcessoService;

    @Transactional(readOnly = true)
    public List<EtapaWorkflow> listarEtapasOrdenadas() {
        return etapaWorkflowRepository.findAllByOrderByOrdemAsc();
    }

    @Transactional(readOnly = true)
    public boolean permiteAnexo(EtapaWorkflowCodigo codigo) {
        return etapaWorkflowRepository.findByCodigo(codigo)
                .map(EtapaWorkflow::getPermiteAnexo)
                .orElse(Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    public List<EtapaWorkflow> listarDestinosPermitidos(EtapaWorkflowCodigo origem, Usuario usuarioExecutor,
            Authentication authentication) {
        if (origem == null) {
            return List.of();
        }
        List<TransicaoWorkflow> transicoes = transicaoWorkflowRepository.findByEtapaOrigem_Codigo(origem);
        if (transicoes.isEmpty()) {
            return List.of();
        }
        Set<String> authorities = authorities(authentication);
        return transicoes.stream()
                .filter(t -> authorityMatches(authorities, t.getRolePermitida(), usuarioExecutor))
                .map(TransicaoWorkflow::getEtapaDestino)
                .distinct()
                .sorted(Comparator.comparingInt(EtapaWorkflow::getOrdem))
                .toList();
    }

    @Transactional
    public ProcessoPrevidenciario avancarEtapa(Long processoId, EtapaWorkflowCodigo etapaDestino,
            Usuario usuarioExecutor, Authentication authentication) {
        ProcessoPrevidenciario processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        EtapaWorkflowCodigo origem = processo.getEtapaAtual();
        EtapaWorkflowCodigo destino = (etapaDestino != null) ? etapaDestino
                : proximaEtapaPermitida(origem, usuarioExecutor, authentication);

        validarTransicao(origem, destino, usuarioExecutor, authentication);

        processo.setEtapaAtual(destino);
        if (destino == EtapaWorkflowCodigo.FINALIZADO) {
            processo.setStatusAtual(ProcessoPrevidenciarioStatus.ENCERRADO);
            processo.setDataEncerramento(LocalDateTime.now());
        } else if (processo.getStatusAtual() == ProcessoPrevidenciarioStatus.ABERTO) {
            processo.setStatusAtual(ProcessoPrevidenciarioStatus.EM_ANDAMENTO);
        }

        ProcessoPrevidenciario atualizado = processoRepository.save(processo);
        historicoProcessoService.registrar(atualizado, "MUDANCA_ETAPA", usuarioExecutor,
                "De " + origem + " para " + destino);
        return atualizado;
    }

    private EtapaWorkflowCodigo proximaEtapaPermitida(EtapaWorkflowCodigo origem, Usuario usuarioExecutor,
            Authentication authentication) {
        List<TransicaoWorkflow> transicoes = transicaoWorkflowRepository.findByEtapaOrigem_Codigo(origem);
        if (transicoes.isEmpty()) {
            throw new IllegalStateException("Não há transições configuradas para a etapa atual");
        }

        Set<String> authorities = authorities(authentication);
        return transicoes.stream()
                .filter(t -> authorityMatches(authorities, t.getRolePermitida(), usuarioExecutor))
                .sorted(Comparator.comparingInt(t -> t.getEtapaDestino().getOrdem()))
                .map(t -> t.getEtapaDestino().getCodigo())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário não possui permissão para avançar a etapa"));
    }

    private void validarTransicao(EtapaWorkflowCodigo origem, EtapaWorkflowCodigo destino, Usuario usuarioExecutor,
            Authentication authentication) {
        if (origem == null || destino == null) {
            throw new IllegalArgumentException("Etapa inválida");
        }
        if (origem == destino) {
            throw new IllegalArgumentException("A etapa de destino deve ser diferente da etapa atual");
        }

        List<TransicaoWorkflow> transicoes = transicaoWorkflowRepository
                .findByEtapaOrigem_CodigoAndEtapaDestino_Codigo(origem, destino);
        if (transicoes.isEmpty()) {
            throw new IllegalStateException("Transição não permitida para este workflow");
        }

        Set<String> authorities = authorities(authentication);
        boolean permitido = transicoes.stream()
                .anyMatch(t -> authorityMatches(authorities, t.getRolePermitida(), usuarioExecutor));
        if (!permitido) {
            throw new IllegalStateException("Usuário não possui role para executar esta transição");
        }
    }

    private Set<String> authorities(Authentication authentication) {
        if (authentication == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    private boolean authorityMatches(Set<String> authorities, String rolePermitida, Usuario usuarioExecutor) {
        if (rolePermitida == null || rolePermitida.isBlank()) {
            return false;
        }
        String normalized = rolePermitida.trim();
        String withPrefix = normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
        String withoutPrefix = normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
        if (authorities.contains(normalized) || authorities.contains(withPrefix)
                || authorities.contains(withoutPrefix)) {
            return true;
        }
        if ("JURIDICO".equalsIgnoreCase(withoutPrefix)) {
            return isJuridico(usuarioExecutor);
        }
        return false;
    }

    private boolean isJuridico(Usuario usuario) {
        if (usuario == null || usuario.getCargo() == null || usuario.getCargo().getNome() == null) {
            return false;
        }
        String cargoNome = usuario.getCargo().getNome().toLowerCase();
        return cargoNome.contains("juridico") || cargoNome.contains("advogado") || cargoNome.contains("legal")
                || cargoNome.contains("compliance");
    }
}
