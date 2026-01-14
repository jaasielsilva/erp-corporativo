package com.jaasielsilva.portalceo.model.chat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false)
    private Long senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_fk")
    private com.jaasielsilva.portalceo.model.Usuario sender;

    @Column(columnDefinition = "text")
    private String content;

    public enum MessageType { TEXT, IMAGE, FILE }

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType type = MessageType.TEXT;

    private String fileUrl;
    private String fileName;
    private Long fileSize;
    @Column(name = "is_read")
    private Boolean read = false;

    @Lob
    @Column
    private byte[] ciphertext;

    @Column(length = 32)
    private byte[] iv;

    @Lob
    private byte[] aad;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }
    public com.jaasielsilva.portalceo.model.Usuario getSender() { return sender; }
    public void setSender(com.jaasielsilva.portalceo.model.Usuario sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public byte[] getCiphertext() { return ciphertext; }
    public void setCiphertext(byte[] ciphertext) { this.ciphertext = ciphertext; }
    public byte[] getIv() { return iv; }
    public void setIv(byte[] iv) { this.iv = iv; }
    public byte[] getAad() { return aad; }
    public void setAad(byte[] aad) { this.aad = aad; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
