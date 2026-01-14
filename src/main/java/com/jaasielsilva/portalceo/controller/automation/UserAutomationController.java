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

@Controller
@RequestMapping("/minha-conta/automacoes")
public class UserAutomationController {

    private final UserAutomationRepository automationRepository;
    private final UsuarioRepository usuarioRepository;

    public UserAutomationController(UserAutomationRepository automationRepository, UsuarioRepository usuarioRepository) {
        this.automationRepository = automationRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String listAutomations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("automations", automationRepository.findByUsuarioId(usuario.getId()));
        model.addAttribute("eventTypes", AutomationEventType.values());
        model.addAttribute("actionTypes", AutomationActionType.values());
        return "minha-conta/automacoes";
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

    @PostMapping("/delete/{id}")
    public String deleteAutomation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        automationRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Automação removida.");
        return "redirect:/minha-conta/automacoes";
    }
}
