package com.jaasielsilva.portalceo.service.chat;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.chat.ChatMembership;
import com.jaasielsilva.portalceo.model.chat.ChatMessage;
import com.jaasielsilva.portalceo.model.chat.ChatRoom;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import com.jaasielsilva.portalceo.repository.chat.ChatMembershipRepository;
import com.jaasielsilva.portalceo.repository.chat.ChatMessageRepository;
import com.jaasielsilva.portalceo.repository.chat.ChatRoomRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Transactional
public class ChatNewService {
    private final ChatRoomRepository roomRepo;
    private final ChatMembershipRepository membershipRepo;
    private final ChatMessageRepository messageRepo;
    private final UsuarioRepository usuarioRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final String uploadDir = "uploads/chat";

    public ChatNewService(ChatRoomRepository roomRepo, ChatMembershipRepository membershipRepo, ChatMessageRepository messageRepo, UsuarioRepository usuarioRepo, SimpMessagingTemplate messagingTemplate) {
        this.roomRepo = roomRepo;
        this.membershipRepo = membershipRepo;
        this.messageRepo = messageRepo;
        this.usuarioRepo = usuarioRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<Map<String,Object>> listRooms(Long userId) {
        List<ChatRoom> rooms = roomRepo.findRoomsByUser(userId);
        List<Map<String,Object>> out = new ArrayList<>();
        for (ChatRoom r : rooms) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("type", r.getType().name());
            m.put("active", r.getActive());
            out.add(m);
        }
        return out;
    }

    public ChatRoom createDirectRoom(Long creatorId, Long targetUserId) {
        Usuario creator = usuarioRepo.findById(creatorId).orElseThrow();
        Usuario target = usuarioRepo.findById(targetUserId).orElseThrow();
        ChatRoom room = new ChatRoom();
        room.setName(creator.getNome()+" & "+target.getNome());
        room.setType(ChatRoom.RoomType.DIRECT);
        room.setCreatedBy(creatorId);
        room = roomRepo.save(room);
        addMember(room, creator);
        addMember(room, target);
        return room;
    }

    public ChatRoom createGroupRoom(Long creatorId, String name, List<Long> participants) {
        ChatRoom room = new ChatRoom();
        room.setName(name);
        room.setType(ChatRoom.RoomType.GROUP);
        room.setCreatedBy(creatorId);
        room = roomRepo.save(room);
        Usuario creator = usuarioRepo.findById(creatorId).orElseThrow();
        addMember(room, creator);
        if (participants != null) for (Long uid : participants) {
            if (!Objects.equals(uid, creatorId)) addMember(room, usuarioRepo.findById(uid).orElseThrow());
        }
        return room;
    }

    private void addMember(ChatRoom room, Usuario usuario) {
        ChatMembership ms = new ChatMembership();
        ms.setRoom(room);
        ms.setUsuario(usuario);
        membershipRepo.save(ms);
    }

    @Transactional(readOnly = true)
    public List<Map<String,Object>> listMessages(Long roomId) {
        List<ChatMessage> msgs = messageRepo.findByRoomOrdered(roomId);
        List<Map<String,Object>> out = new ArrayList<>();
        for (ChatMessage m : msgs) {
            Map<String,Object> x = new LinkedHashMap<>();
            x.put("id", m.getId());
            x.put("roomId", m.getRoom().getId());
            x.put("senderId", m.getSender().getId());
            x.put("senderName", m.getSender().getNome());
            x.put("content", m.getContent());
            x.put("type", m.getType().name());
            x.put("sentAt", m.getSentAt().toString());
            x.put("fileUrl", m.getFileUrl());
            x.put("fileName", m.getFileName());
            x.put("fileSize", m.getFileSize());
            out.add(x);
        }
        return out;
    }

    public ChatMessage sendText(Long roomId, Long senderId, String content) {
        ChatRoom room = roomRepo.findById(roomId).orElseThrow();
        Usuario sender = usuarioRepo.findById(senderId).orElseThrow();
        ChatMessage msg = new ChatMessage();
        msg.setRoom(room);
        msg.setSender(sender);
        msg.setContent(content);
        msg = messageRepo.save(msg);
        broadcast(room.getId(), msg);
        return msg;
    }

    public ChatMessage sendFile(Long roomId, Long senderId, MultipartFile file, String content, ChatMessage.MessageType type) throws Exception {
        ChatRoom room = roomRepo.findById(roomId).orElseThrow();
        Usuario sender = usuarioRepo.findById(senderId).orElseThrow();
        Path base = Paths.get(uploadDir);
        if (!Files.exists(base)) Files.createDirectories(base);
        String name = System.currentTimeMillis()+"_"+file.getOriginalFilename();
        Path p = base.resolve(name);
        Files.copy(file.getInputStream(), p);
        ChatMessage msg = new ChatMessage();
        msg.setRoom(room);
        msg.setSender(sender);
        msg.setContent(content == null ? "" : content);
        msg.setType(type);
        msg.setFileUrl(uploadDir+"/"+name);
        msg.setFileName(file.getOriginalFilename());
        msg.setFileSize(file.getSize());
        msg = messageRepo.save(msg);
        broadcast(room.getId(), msg);
        return msg;
    }

    public void markRead(Long roomId, Long userId) {
        List<ChatMessage> msgs = messageRepo.findByRoomOrdered(roomId);
        for (ChatMessage m : msgs) if (!Objects.equals(m.getSender().getId(), userId)) { m.setRead(true); }
        messageRepo.saveAll(msgs);
        membershipRepo.findByUsuarioIdAndActiveTrue(userId).forEach(ms -> { if (Objects.equals(ms.getRoom().getId(), roomId)) { ms.setLastSeen(java.time.LocalDateTime.now()); membershipRepo.save(ms); } });
    }

    private void broadcast(Long roomId, ChatMessage msg) {
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("roomId", roomId);
        payload.put("id", msg.getId());
        payload.put("senderId", msg.getSender().getId());
        payload.put("senderName", msg.getSender().getNome());
        payload.put("content", msg.getContent());
        payload.put("type", msg.getType().name());
        payload.put("sentAt", msg.getSentAt().toString());
        payload.put("fileUrl", msg.getFileUrl());
        payload.put("fileName", msg.getFileName());
        payload.put("fileSize", msg.getFileSize());
        messagingTemplate.convertAndSend("/topic/chat.room."+roomId, payload);
        membershipRepo.findByRoomIdAndActiveTrue(roomId).forEach(ms -> messagingTemplate.convertAndSendToUser(ms.getUsuario().getEmail(), "/queue/mensagens", payload));
    }
}