package com.jaasielsilva.portalceo.controller.automation;

import com.jaasielsilva.portalceo.dto.AutomationDTO;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.automation.AutomationActionType;
import com.jaasielsilva.portalceo.model.automation.AutomationEventType;
import com.jaasielsilva.portalceo.model.automation.UserAutomation;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.automation.UserAutomationRepository;
import com.jaasielsilva.portalceo.model.automation.AutomationLog;
import com.jaasielsilva.portalceo.repository.automation.AutomationLogRepository;
import com.jaasielsilva.portalceo.service.EmailService;
import com.jaasielsilva.portalceo.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/minha-conta/automacoes")
public class UserAutomationController {

    private static final Logger logger = LoggerFactory.getLogger(UserAutomationController.class);

    private final UserAutomationRepository automationRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AutomationLogRepository logRepository;

    public UserAutomationController(UserAutomationRepository automationRepository,
                                    UsuarioRepository usuarioRepository,
                                    EmailService emailService,
                                    NotificationService notificationService,
                                    AutomationLogRepository logRepository) {
        this.automationRepository = automationRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.logRepository = logRepository;
    }

    @GetMapping
    public String listAutomations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            logger.info("Listando automações para usuário: {}", userDetails.getUsername());
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            
            List<UserAutomation> automationsEntities = automationRepository.findByUsuarioId(usuario.getId());
            if (automationsEntities == null) {
                automationsEntities = List.of();
            }
            logger.info("Encontradas {} automações.", automationsEntities.size());
            
            // Converter para DTO para evitar erros na View
            List<AutomationDTO> dtos = automationsEntities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
            
            model.addAttribute("automations", dtos);
            model.addAttribute("eventTypes", AutomationEventType.values());
            model.addAttribute("actionTypes", AutomationActionType.values());
            
            logger.info("DTOs criados e adicionados ao modelo.");
            return "minha-conta/automacoes";
        } catch (Exception e) {
            logger.error("Erro ao listar automações", e);
            model.addAttribute("errorMessage", "Erro ao carregar automações: " + e.getMessage());
            model.addAttribute("automations", List.of());
            return "minha-conta/automacoes";
        }
    }

    private AutomationDTO toDTO(UserAutomation entity) {
        if (entity == null) return null;
        
        try {
            String timeStr = entity.getExecutionTime() != null 
                ? entity.getExecutionTime().format(DateTimeFormatter.ofPattern("HH:mm")) 
                : null;
                
            return new AutomationDTO(
                entity.getId(),
                entity.getEventType() != null ? entity.getEventType().name() : null,
                entity.getActionType() != null ? entity.getActionType().name() : null,
                timeStr,
                entity.isActive()
            );
        } catch (Exception e) {
            logger.error("Erro ao converter automação ID {} para DTO", entity.getId(), e);
            return null; // Retorna null para ser filtrado se necessário, ou lança erro
        }
    }

    @PostMapping("/create")
    public String createAutomation(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam AutomationEventType eventType,
                                   @RequestParam AutomationActionType actionType,
                                   @RequestParam String executionTime,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();

            // Convert string "HH:mm" to LocalTime
            LocalTime time = LocalTime.parse(executionTime, DateTimeFormatter.ofPattern("HH:mm"));

            UserAutomation automation = new UserAutomation(usuario, eventType, actionType, time);
            automationRepository.save(automation);

            redirectAttributes.addFlashAttribute("successMessage", "Automação criada com sucesso!");
        } catch (Exception e) {
            logger.error("Erro ao criar automação", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar automação: " + e.getMessage());
        }
        return "redirect:/minha-conta/automacoes";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteAutomation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UserAutomation automation = automationRepository.findById(id).orElseThrow();

            if (!automation.getUsuario().getId().equals(usuario.getId())) {
                 redirectAttributes.addFlashAttribute("errorMessage", "Acesso negado.");
                 return "redirect:/minha-conta/automacoes";
            }

            automationRepository.delete(automation);
            redirectAttributes.addFlashAttribute("successMessage", "Automação excluída.");
        } catch (Exception e) {
            logger.error("Erro ao excluir automação", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir: " + e.getMessage());
        }
        return "redirect:/minha-conta/automacoes";
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleAutomation(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UserAutomation automation = automationRepository.findById(id).orElseThrow();

            if (!automation.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(403).body(Map.of(
                        "active", automation.isActive(),
                        "message", "Acesso negado."
                ));
            }

            boolean novoStatus = !automation.isActive();
            automation.setActive(novoStatus);
            automationRepository.save(automation);

            String mensagem = novoStatus ? "Automação ativada." : "Automação pausada.";
            return ResponseEntity.ok(Map.of(
                    "active", novoStatus,
                    "message", mensagem
            ));
        } catch (Exception e) {
            logger.error("Erro ao alterar status da automação {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                    "active", false,
                    "message", "Erro ao alterar status: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/logs/{id}")
    @ResponseBody
    public ResponseEntity<?> getAutomationLogs(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UserAutomation automation = automationRepository.findById(id).orElseThrow();

            if (!automation.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
            }

            List<AutomationLog> logs = logRepository.findTop10ByAutomationIdOrderByTimestampDesc(id);
            
            // DTO simplificado para o frontend
            List<Map<String, Object>> logDtos = logs.stream().map(log -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("timestamp", log.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                map.put("status", log.getStatus());
                map.put("message", log.getMessage());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(logDtos);
        } catch (Exception e) {
            logger.error("Erro ao buscar logs", e);
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao buscar logs: " + e.getMessage()));
        }
    }
}
