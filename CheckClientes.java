import java.sql.*;

public class CheckClientes {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/painelceo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "12345";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conectado ao banco de dados com sucesso!");

            // Verificar tabela de clientes
            String sql = "SELECT id, nome, email, tipo_cliente FROM clientes LIMIT 5";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                System.out.println("\n--- Clientes (Top 5) ---");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long id = rs.getLong("id");
                    String nome = rs.getString("nome");
                    String email = rs.getString("email");
                    String tipo = rs.getString("tipo_cliente");
                    System.out.printf("ID: %d | Nome: %s | Email: %s | Tipo: %s%n", id, nome, email, tipo);
                }
                if (!found) {
                    System.out.println("Nenhum cliente encontrado na tabela 'clientes'.");
                }
            } catch (SQLException e) {
                System.out.println("Erro ao consultar clientes: " + e.getMessage());
            }

            // Verificar permissões do usuário master
            String sqlPermissoes = "SELECT p.nome FROM permissoes p " +
                                 "JOIN perfil_permissoes pp ON p.id = pp.permissao_id " +
                                 "JOIN perfis perf ON pp.perfil_id = perf.id " +
                                 "JOIN usuarios u ON u.perfil_id = perf.id " +
                                 "WHERE u.email = 'master@sistema.com' " +
                                 "AND p.nome = 'MENU_CLIENTES_LISTAR'";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlPermissoes)) {
                
                System.out.println("\n--- Verificacao de Permissao (master@sistema.com) ---");
                if (rs.next()) {
                    System.out.println("Permissao MENU_CLIENTES_LISTAR encontrada para o usuario.");
                } else {
                    System.out.println("Permissao MENU_CLIENTES_LISTAR NAO encontrada para o usuario.");
                }
            } catch (SQLException e) {
                 System.out.println("Erro ao consultar permissoes: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexao: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
