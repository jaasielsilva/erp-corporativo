package com.jaasielsilva.portalceo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;       // remetente
    private String content;      // conteúdo da mensagem
    private LocalDateTime timestamp; // data e hora
    private Long destinatarioId; // null = mensagem pública

    public ChatMessage() {
    }

    public ChatMessage(String sender, String content, LocalDateTime timestamp, Long destinatarioId) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.destinatarioId = destinatarioId;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getDestinatarioId() { return destinatarioId; }
    public void setDestinatarioId(Long destinatarioId) { this.destinatarioId = destinatarioId; }
}
