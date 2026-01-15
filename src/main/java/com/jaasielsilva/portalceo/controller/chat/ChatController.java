package com.jaasielsilva.portalceo.controller.chat;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.service.chat.ChatMessageService;
import com.jaasielsilva.portalceo.service.chat.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ChatSessionService chatSessionService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/contacts")
    public ResponseEntity<List<Map<String, Object>>> getContacts(Authentication auth) {
        String myEmail = auth.getName();
        List<Usuario> users = usuarioRepository.findAll();
        
        List<Map<String, Object>> contacts = users.stream()
            .filter(u -> u.getEmail() != null && !u.getEmail().equals(myEmail))
            .map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                
                String name = u.getNome() != null ? u.getNome() : "Usuário sem nome";
                map.put("name", name);
                
                String avatarName = name.replace(" ", "+");
                map.put("avatar", "https://ui-avatars.com/api/?name=" + avatarName + "&background=random");
                
                boolean isOnline = chatSessionService.isUserOnline(u.getId());
                map.put("online", isOnline);
                
                if (u.getUltimoAcesso() != null) {
                    map.put("lastSeen", u.getUltimoAcesso());
                }
                
                return map;
            })
            .collect(Collectors.<Map<String, Object>>toList());
            
        return ResponseEntity.ok(contacts);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/history")
    public ResponseEntity<Page<ChatMessage>> history(@RequestParam Long conversationId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(chatMessageService.history(conversationId, page, size));
    }
}
