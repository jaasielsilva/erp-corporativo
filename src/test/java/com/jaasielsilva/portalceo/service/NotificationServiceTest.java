package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateNotification() {
        // Arrange
        Usuario user = new Usuario();
        user.setId(1L);
        user.setNome("Test User");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setType("test");
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test notification");
        notification.setPriority(Notification.Priority.MEDIUM);
        notification.setUser(user);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        Notification result = notificationService.createNotification("test", "Test Notification", 
            "This is a test notification", Notification.Priority.MEDIUM, user);

        // Assert
        assertNotNull(result);
        assertEquals("test", result.getType());
        assertEquals("Test Notification", result.getTitle());
        assertEquals("This is a test notification", result.getMessage());
        assertEquals(Notification.Priority.MEDIUM, result.getPriority());
        assertEquals(user, result.getUser());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead() {
        // Arrange
        Long notificationId = 1L;
        Usuario user = new Usuario();
        user.setId(1L);

        when(notificationRepository.markAsReadForUser(notificationId, user)).thenReturn(1);

        // Act
        boolean result = notificationService.markAsRead(notificationId, user);

        // Assert
        assertTrue(result);
        verify(notificationRepository, times(1)).markAsReadForUser(notificationId, user);
    }

    @Test
    void testMarkAsReadFailure() {
        // Arrange
        Long notificationId = 1L;
        Usuario user = new Usuario();
        user.setId(1L);

        when(notificationRepository.markAsReadForUser(notificationId, user)).thenReturn(0);

        // Act
        boolean result = notificationService.markAsRead(notificationId, user);

        // Assert
        assertFalse(result);
        verify(notificationRepository, times(1)).markAsReadForUser(notificationId, user);
    }

    @Test
    void testNotifyNewEmployeeAdmission() {
        // Arrange
        Usuario recipient = new Usuario();
        recipient.setId(1L);
        recipient.setNome("Recipient User");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setType("hr_admission");
        notification.setTitle("Novo Colaborador em Admissão");
        notification.setMessage("Novo colaborador John Doe (john.doe@example.com) está em processo de admissão e aguarda integração.");
        notification.setPriority(Notification.Priority.HIGH);
        notification.setUser(recipient);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        Notification result = notificationService.notifyNewEmployeeAdmission("John Doe", "john.doe@example.com", recipient);

        // Assert
        assertNotNull(result);
        assertEquals("hr_admission", result.getType());
        assertEquals("Novo Colaborador em Admissão", result.getTitle());
        assertEquals(Notification.Priority.HIGH, result.getPriority());
        assertEquals(recipient, result.getUser());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyEmployeeDocumentPending() {
        // Arrange
        Usuario recipient = new Usuario();
        recipient.setId(1L);
        recipient.setNome("Recipient User");

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setType("hr_document_pending");
        notification.setTitle("Pendência Documental - Colaborador");
        notification.setMessage("Colaborador John Doe (john.doe@example.com) possui documentação pendente no processo de admissão.");
        notification.setPriority(Notification.Priority.HIGH);
        notification.setUser(recipient);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        Notification result = notificationService.notifyEmployeeDocumentPending("John Doe", "john.doe@example.com", recipient);

        // Assert
        assertNotNull(result);
        assertEquals("hr_document_pending", result.getType());
        assertEquals("Pendência Documental - Colaborador", result.getTitle());
        assertEquals(Notification.Priority.HIGH, result.getPriority());
        assertEquals(recipient, result.getUser());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}