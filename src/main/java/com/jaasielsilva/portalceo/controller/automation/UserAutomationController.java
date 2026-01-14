package com.jaasielsilva.portalceo.controller.automation;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.automation.AutomationActionType;
import com.jaasielsilva.portalceo.model.automation.AutomationEventType;
import com.jaasielsilva.portalceo.model.automation.UserAutomation;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.automation.UserAutomationRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.jaasielsilva.portalceo.service.EmailService;
import com.jaasielsilva.portalceo.service.NotificationService;
import org.springframework.http.ResponseEntity;
import java.util.Map;

import com.jaasielsilva.portalceo.model.automation.AutomationLog;
import com.jaasielsilva.portalceo.repository.automation.AutomationLogRepository;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/minha-conta/automacoes")
public class UserAutomationController {

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
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        
        List<UserAutomation> automationsEntities = automationRepository.findByUsuarioId(usuario.getId());
        if (automationsEntities == null) {
            automationsEntities = List.of();
        }
        
        // Converter para DTO para evitar erros na View
        List<AutomationDTO> dtos = automationsEntities.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        model.addAttribute("automations", dtos);
        model.addAttribute("eventTypes", AutomationEventType.values());
        model.addAttribute("actionTypes", AutomationActionType.values());
        return "minha-conta/automacoes";
    }

    private AutomationDTO toDTO(UserAutomation entity) {
        if (entity == null) return null;
        
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
    }

    // Classe DTO Interna
    public static class AutomationDTO {
        private Long id;
        private String eventType;
        private String actionType;
        private String executionTime;
        private boolean active;

        public AutomationDTO(Long id, String eventType, String actionType, String executionTime, boolean active) {
            this.id = id;
            this.eventType = eventType;
            this.actionType = actionType;
            this.executionTime = executionTime;
            this.active = active;
        }

        public Long getId() { return id; }
        public String getEventType() { return eventType; }
        public String getActionType() { return actionType; }
        public String getExecutionTime() { return executionTime; }
        public boolean isActive() { return active; }
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
            e.printStackTrace(); // Log for debugging
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar automação: " + e.getMessage());
        }
        return "redirect:/minha-conta/automacoes";
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
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao buscar logs: " + e.getMessage()));
        }
    }

    /* Endpoint de teste comentado
    @PostMapping("/test/{id}")
    @ResponseBody
    public ResponseEntity<?> testAutomation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UserAutomation automation = automationRepository.findById(id).orElseThrow();

            if (!automation.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
            }

            // Log Debug
            System.out.println("=== TESTE DE AUTOMAÇÃO ===");
            System.out.println("Usuário: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
            System.out.println("E-mail destino: " + usuario.getEmail());
            System.out.println("Ação: " + automation.getActionType());

            if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuário sem e-mail cadastrado."));
            }

            String msg = "Teste de Automação: O evento '" + automation.getEventType() + "' foi disparado manualmente para teste.";

            if (automation.getActionType() == AutomationActionType.EMAIL_ALERT) {
                System.out.println("Tentando enviar e-mail via EmailService (Síncrono)...");
                emailService.enviarEmailTeste(usuario.getEmail(), "Teste de Automação", msg);
                System.out.println("Chamada ao EmailService concluída com sucesso.");
            } else if (automation.getActionType() == AutomationActionType.SYSTEM_NOTIFICATION) {
                notificationService.enviarNotificacao(usuario.getEmail(), "Automação", msg);
            }

            return ResponseEntity.ok(Map.of("message", "Teste enviado com sucesso para: " + usuario.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao testar: " + e.getMessage()));
        }
    }
    */

    @PostMapping("/update/{id}")
    public String updateAutomation(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam AutomationEventType eventType,
                                   @RequestParam AutomationActionType actionType,
                                   @RequestParam String executionTime,
                                   RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UserAutomation automation = automationRepository.findById(id).orElseThrow();

            if (!automation.getUsuario().getId().equals(usuario.getId())) {
                return "redirect:/minha-conta/automacoes?error=AcessoNegado";
            }

            // Convert string "HH:mm" to LocalTime
            LocalTime time = LocalTime.parse(executionTime, DateTimeFormatter.ofPattern("HH:mm"));

            automation.setEventType(eventType);
            automation.setActionType(actionType);
            automation.setExecutionTime(time);
            
            automationRepository.save(automation);

            redirectAttributes.addFlashAttribute("successMessage", "Automação atualizada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar: " + e.getMessage());
        }
        return "redirect:/minha-conta/automacoes";
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleAutomation(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UserAutomation automation = automationRepository.findById(id).orElseThrow();

            if (!automation.getUsuario().getId().equals(usuario.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Acesso negado"));
            }

            automation.setActive(!automation.isActive());
            automationRepository.save(automation);

            return ResponseEntity.ok(Map.of(
                "active", automation.isActive(),
                "message", automation.isActive() ? "Automação ativada." : "Automação pausada."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro ao alterar status: " + e.getMessage()));
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAutomation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        automationRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Automação removida.");
        return "redirect:/minha-conta/automacoes";
    }
}
