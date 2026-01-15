package com.jaasielsilva.portalceo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SetupTestUsers {

    // Configurações de banco (ajuste conforme seu application.properties se necessário)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/painelceo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345";

    public static void main(String[] args) {
        System.out.println("Iniciando criacao de usuarios de teste...");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String senhaHash = encoder.encode("senha123");

            // 1. Criar Gerente Comercial (Pode excluir)
            criarUsuario(conn, "Gerente Teste", "gerente.teste@empresa.com", senhaHash, "GERENTE", "M99998", "GERENTE_COMERCIAL");

            // 2. Criar Vendedor (Não pode excluir)
            criarUsuario(conn, "Vendedor Teste", "vendedor.teste@empresa.com", senhaHash, "USER", "M99999", "VENDEDOR");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void criarUsuario(Connection conn, String nome, String email, String senha, String nivel, String matricula, String nomePerfil) throws Exception {
        // Verificar se usuário existe
        String sqlCheck = "SELECT id FROM usuarios WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Usuario ja existe: " + email);
                return;
            }
        }

        // Inserir usuário
        String sqlInsert = "INSERT INTO usuarios (nome, email, senha, status, nivel_acesso, matricula, online, notificacoes_sonoras_ativadas, mutar_toasts, preferir_banner_seguranca, volume_notificacao, nao_perturbe_ativo) VALUES (?, ?, ?, 'ATIVO', ?, ?, false, true, false, false, 80, false)";
        long userId = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, senha);
            stmt.setString(4, nivel);
            stmt.setString(5, matricula);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getLong(1);
                System.out.println("Usuario criado: " + email + " (ID: " + userId + ")");
            }
        }

        // Buscar ID do perfil
        long perfilId = 0;
        String sqlPerfil = "SELECT id FROM perfis WHERE nome = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPerfil)) {
            stmt.setString(1, nomePerfil);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                perfilId = rs.getLong(1);
            } else {
                System.out.println("ERRO: Perfil nao encontrado: " + nomePerfil);
                return;
            }
        }

        // Vincular perfil
        String sqlVinculo = "INSERT INTO usuario_perfil (usuario_id, perfil_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sqlVinculo)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, perfilId);
            stmt.executeUpdate();
            System.out.println("Perfil " + nomePerfil + " vinculado ao usuario " + email);
        }
    }
}
