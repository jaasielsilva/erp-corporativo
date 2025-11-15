package com.jaasielsilva.portalceo.controller.chat;

import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.chat.ChatNewService;
import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {
    private final ChatNewService chatService;
    private final UsuarioService usuarioService;
    public ChatApiController(ChatNewService chatService, UsuarioService usuarioService) { this.chatService = chatService; this.usuarioService = usuarioService; }

    @GetMapping("/rooms")
    public ResponseEntity<List<Map<String,Object>>> rooms(Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(chatService.listRooms(u.getId()));
    }

    @PostMapping("/rooms/direct")
    public ResponseEntity<Map<String,Object>> createDirect(@RequestParam Long userId, Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        var r = chatService.createDirectRoom(u.getId(), userId);
        return ResponseEntity.ok(Map.of("id", r.getId(), "name", r.getName()));
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<Map<String,Object>> createGroup(@RequestBody Map<String,Object> body, Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        var name = (String) body.get("name");
        @SuppressWarnings("unchecked") var participants = (List<Integer>) body.get("participants");
        var ids = participants != null ? participants.stream().map(Long::valueOf).toList() : java.util.List.<Long>of();
        var r = chatService.createGroupRoom(u.getId(), name, ids);
        return ResponseEntity.ok(Map.of("id", r.getId(), "name", r.getName()));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<Map<String,Object>>> messages(@PathVariable Long roomId, Authentication auth) {
        usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(chatService.listMessages(roomId));
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Map<String,Object>> send(@PathVariable Long roomId, @RequestParam String content, Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        var m = chatService.sendText(roomId, u.getId(), content);
        return ResponseEntity.ok(Map.of("id", m.getId()));
    }

    @PostMapping("/rooms/{roomId}/messages/upload")
    public ResponseEntity<Map<String,Object>> upload(@PathVariable Long roomId, @RequestParam("file") MultipartFile file, @RequestParam(value="content", required=false) String content, Authentication auth) throws Exception {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        var type = content != null && content.toLowerCase().endsWith(".png") ? ChatMessage.MessageType.IMAGE : ChatMessage.MessageType.FILE;
        var m = chatService.sendFile(roomId, u.getId(), file, content, type);
        return ResponseEntity.ok(Map.of("id", m.getId()));
    }

    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long roomId, Authentication auth) {
        var u = usuarioService.buscarPorEmail(auth.getName()).orElseThrow();
        chatService.markRead(roomId, u.getId());
        return ResponseEntity.ok().build();
    }
}