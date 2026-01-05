package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.dto.ChamadoDTO;
import com.jaasielsilva.portalceo.dto.AtualizarStatusRequest;
import com.jaasielsilva.portalceo.dto.ChamadoStatusResponse;
import com.jaasielsilva.portalceo.service.ChamadoService;
import com.jaasielsilva.portalceo.service.ChamadoStateMachine;
import com.jaasielsilva.portalceo.service.ChamadoAuditoriaService;
import com.jaasielsilva.portalceo.security.PermissaoBusinessService;
import com.jaasielsilva.portalceo.security.PerfilUsuario;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller para APIs do módulo de suporte
 * Fornece endpoints específicos para a interface de status
 */
@RestController
@RequestMapping("/api/suporte")
public class SuporteApiController {

    private static final Logger logger = LoggerFactory.getLogger(SuporteApiController.class);

    @Autowired
    private ChamadoService chamadoService;

    @Autowired
    private ChamadoStateMachine stateMachine;

    @Autowired
    private ChamadoAuditoriaService auditoriaService;

    @Autowired
    private PermissaoBusinessService permissaoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * API para listar chamados - usado pela página de status
     */
    @GetMapping("/chamados")
    @PreAuthorize("hasAnyAuthority('CHAMADO_VISUALIZAR', 'TECNICO_ATENDER_CHAMADOS')")
    public ResponseEntity<List<ChamadoDTO>> listarChamados() {
        try {
            List<ChamadoDTO> chamados = chamadoService.listarTodosDTO();
            logger.info("Retornando {} chamados para a página de status", chamados.size());
            return ResponseEntity.ok(chamados);
        } catch (Exception e) {
            logger.error("Erro ao listar chamados: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API para atualizar status de chamado - NOVO PADRÃO com validações
     */
    @PutMapping("/chamados/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChamadoStatusResponse> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusRequest request) {
        
        try {
            // Verificar permissão para atualizar status
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            String acao = request.getAcao().toLowerCase();
            
            boolean temPermissao = false;
            
            switch (acao) {
                case "iniciar":
                    temPermissao = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CHAMADO_INICIAR"));
                    break;
                case "resolver":
                    temPermissao = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CHAMADO_RESOLVER"));
                    break;
                case "fechar":
                    temPermissao = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CHAMADO_FECHAR"));
                    break;
                case "reabrir":
                    temPermissao = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CHAMADO_REABRIR"));
                    break;
                default:
                    auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS_API", id, username, "Ação inválida: " + acao);
                    return ResponseEntity.badRequest()
                        .body(ChamadoStatusResponse.erro("Ação inválida: " + acao));
            }
            
            if (!temPermissao) {
                auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS_API", id, username, 
                    "Usuário não possui permissão para executar a ação: " + request.getAcao());
                return ResponseEntity.status(403)
                    .body(ChamadoStatusResponse.erro("Acesso negado: você não possui permissão para executar esta ação"));
            }

            // Buscar chamado
            Optional<Chamado> chamadoOpt = chamadoService.buscarPorId(id);
            if (chamadoOpt.isEmpty()) {
                auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS_API", id, null, "Chamado não encontrado");
                return ResponseEntity.badRequest()
                    .body(ChamadoStatusResponse.erro("Chamado não encontrado"));
            }

            Chamado chamado = chamadoOpt.get();
            Chamado.StatusChamado statusAtual = chamado.getStatus();
            Chamado.StatusChamado novoStatus = request.getStatusDestino();

            // Validar transição de status
            if (!stateMachine.isTransicaoValida(statusAtual, novoStatus)) {
                String erro = String.format("Transição inválida de %s para %s", 
                    statusAtual.getDescricao(), novoStatus.getDescricao());
                auditoriaService.registrarTentativaOperacaoNaoAutorizada("ATUALIZAR_STATUS_API", id, null, erro);
                return ResponseEntity.badRequest()
                    .body(ChamadoStatusResponse.erro(erro));
            }

            // Executar ação baseada no tipo
            Chamado chamadoAtualizado;
            
            switch (acao) {
                case "iniciar":
                    if (request.getTecnicoResponsavel() == null || request.getTecnicoResponsavel().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                            .body(ChamadoStatusResponse.erro("Técnico responsável é obrigatório para iniciar atendimento"));
                    }
                    chamadoAtualizado = chamadoService.iniciarAtendimento(id, request.getTecnicoResponsavel());
                    auditoriaService.registrarMudancaStatus(chamadoAtualizado, statusAtual, novoStatus, request.getTecnicoResponsavel(), request.getObservacoes());
                    break;
                    
                case "resolver":
                    chamadoAtualizado = chamadoService.resolverChamado(id);
                    auditoriaService.registrarMudancaStatus(chamadoAtualizado, statusAtual, novoStatus, chamado.getTecnicoResponsavel(), request.getObservacoes());
                    break;
                    
                case "fechar":
                    chamadoAtualizado = chamadoService.fecharChamado(id);
                    auditoriaService.registrarMudancaStatus(chamadoAtualizado, statusAtual, novoStatus, chamado.getTecnicoResponsavel(), request.getObservacoes());
                    break;
                    
                case "reabrir":
                    if (request.getMotivoReabertura() == null || request.getMotivoReabertura().trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                            .body(ChamadoStatusResponse.erro("Motivo da reabertura é obrigatório"));
                    }
                    chamadoAtualizado = chamadoService.reabrirChamado(id);
                    auditoriaService.registrarReaberturaChamado(chamadoAtualizado, null, request.getMotivoReabertura());
                    break;
                    
                default:
                    throw new IllegalArgumentException("Ação inválida: " + acao);
            }

            logger.info("Status do chamado {} atualizado de {} para {} via API", 
                id, statusAtual.getDescricao(), novoStatus.getDescricao());
            
            return ResponseEntity.ok(ChamadoStatusResponse.sucesso(chamadoAtualizado, "Status atualizado com sucesso via API"));

        } catch (Exception e) {
            logger.error("Erro ao atualizar status do chamado {} via API: {}", id, e.getMessage(), e);
            auditoriaService.registrarErroOperacao("ATUALIZAR_STATUS_API", id, null, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ChamadoStatusResponse.erro("Erro interno: " + e.getMessage()));
        }
    }
}