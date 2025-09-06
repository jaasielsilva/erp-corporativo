package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Notification;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.NotificationRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdesaoNotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void testNotifyNewEmployeeAdmission() {
        // Create a test user with RH permissions
        Usuario rhUser = new Usuario();
        rhUser.setNome("RH User");
        rhUser.setEmail("rh@example.com");
        rhUser.setNivelAcesso(com.jaasielsilva.portalceo.model.NivelAcesso.GERENTE);
        rhUser.setSenha(passwordEncoder.encode("password")); // Set a password
        
        // Save the user
        rhUser = usuarioRepository.save(rhUser);
        
        // Create notification for new employee admission
        Notification notification = notificationService.notifyNewEmployeeAdmission(
            "John Doe", 
            "john.doe@example.com", 
            rhUser
        );
        
        // Verify the notification was created correctly
        assertNotNull(notification.getId());
        assertEquals("hr_admission", notification.getType());
        assertEquals("Novo Colaborador em Admissão", notification.getTitle());
        assertEquals("Novo colaborador John Doe (john.doe@example.com) está em processo de admissão e aguarda integração.", 
            notification.getMessage());
        assertEquals(Notification.Priority.HIGH, notification.getPriority());
        assertEquals(rhUser, notification.getUser());
        assertTrue(notification.getActive());
        assertFalse(notification.getIsRead());
    }

    @Test
    void testNotifyEmployeeDocumentPending() {
        // Create a test user with RH permissions
        Usuario rhUser = new Usuario();
        rhUser.setNome("RH User");
        rhUser.setEmail("rh2@example.com");
        rhUser.setNivelAcesso(com.jaasielsilva.portalceo.model.NivelAcesso.GERENTE);
        rhUser.setSenha(passwordEncoder.encode("password")); // Set a password
        
        // Save the user
        rhUser = usuarioRepository.save(rhUser);
        
        // Create notification for employee document pending
        Notification notification = notificationService.notifyEmployeeDocumentPending(
            "Jane Smith", 
            "jane.smith@example.com", 
            rhUser
        );
        
        // Verify the notification was created correctly
        assertNotNull(notification.getId());
        assertEquals("hr_document_pending", notification.getType());
        assertEquals("Pendência Documental - Colaborador", notification.getTitle());
        assertEquals("Colaborador Jane Smith (jane.smith@example.com) possui documentação pendente no processo de admissão.", 
            notification.getMessage());
        assertEquals(Notification.Priority.HIGH, notification.getPriority());
        assertEquals(rhUser, notification.getUser());
        assertTrue(notification.getActive());
        assertFalse(notification.getIsRead());
    }

    @Test
    void testBuscarUsuariosComPermissaoGerenciarRH() {
        // Get users with RH management permissions before adding new ones
        List<Usuario> rhUsersBefore = usuarioService.buscarUsuariosComPermissaoGerenciarRH();
        int initialCount = rhUsersBefore.size();
        
        // Create a test user with MASTER permissions
        Usuario masterUser = new Usuario();
        masterUser.setNome("Master User");
        masterUser.setEmail("master@example.com");
        masterUser.setNivelAcesso(com.jaasielsilva.portalceo.model.NivelAcesso.MASTER);
        masterUser.setSenha(passwordEncoder.encode("password")); // Set a password
        masterUser = usuarioRepository.save(masterUser);
        
        // Create a test user with ADMIN permissions
        Usuario adminUser = new Usuario();
        adminUser.setNome("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setNivelAcesso(com.jaasielsilva.portalceo.model.NivelAcesso.ADMIN);
        adminUser.setSenha(passwordEncoder.encode("password")); // Set a password
        adminUser = usuarioRepository.save(adminUser);
        
        // Create a test user with GERENTE permissions
        Usuario gerenteUser = new Usuario();
        gerenteUser.setNome("Gerente User");
        gerenteUser.setEmail("gerente@example.com");
        gerenteUser.setNivelAcesso(com.jaasielsilva.portalceo.model.NivelAcesso.GERENTE);
        gerenteUser.setSenha(passwordEncoder.encode("password")); // Set a password
        gerenteUser = usuarioRepository.save(gerenteUser);
        
        // Get users with RH management permissions after adding new ones
        List<Usuario> rhUsersAfter = usuarioService.buscarUsuariosComPermissaoGerenciarRH();
        
        // Verify that we have 3 more users than before
        assertEquals(initialCount + 3, rhUsersAfter.size());
        
        // Verify the new users are included
        assertTrue(rhUsersAfter.stream().anyMatch(u -> u.getEmail().equals("master@example.com")));
        assertTrue(rhUsersAfter.stream().anyMatch(u -> u.getEmail().equals("admin@example.com")));
        assertTrue(rhUsersAfter.stream().anyMatch(u -> u.getEmail().equals("gerente@example.com")));
    }
}