package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.dto.ChamadoDTO;
import com.jaasielsilva.portalceo.service.ChamadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * API para listar chamados - usado pela página de status
     */
    @GetMapping("/chamados")
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
     * API para atualizar status de chamado - usado pela página de status
     */
    @PutMapping("/chamados/{id}/status")
    public ResponseEntity<ChamadoDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam Chamado.StatusChamado status,
            @RequestParam(required = false) String tecnicoResponsavel) {
        try {
            Chamado chamado = chamadoService.buscarPorId(id).orElse(null);
            if (chamado == null) {
                return ResponseEntity.notFound().build();
            }

            chamado.setStatus(status);
            if (tecnicoResponsavel != null && !tecnicoResponsavel.trim().isEmpty()) {
                chamado.setTecnicoResponsavel(tecnicoResponsavel);
            }

            Chamado chamadoAtualizado = chamadoService.atualizarChamado(chamado);
            ChamadoDTO chamadoDTO = new ChamadoDTO(chamadoAtualizado);
            logger.info("Status do chamado {} atualizado para {}", id, status);
            return ResponseEntity.ok(chamadoDTO);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status do chamado {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}