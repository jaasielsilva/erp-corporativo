# Documentação do Módulo `security`

O módulo `security` é responsável por implementar as funcionalidades de segurança da aplicação, incluindo autenticação, autorização e gerenciamento de permissões. Ele define como os usuários são autenticados, quais recursos eles podem acessar e como as sessões de segurança são gerenciadas.

## Arquivos e suas Finalidades:

*   **`CustomAuthenticationFailureHandler.java`**: Manipulador personalizado para falhas de autenticação. Define a lógica a ser executada quando um usuário não consegue se autenticar, como redirecionar para uma página de erro ou registrar a tentativa falha.
*   **`CustomAuthenticationSuccessHandler.java`**: Manipulador personalizado para autenticações bem-sucedidas. Define a lógica a ser executada quando um usuário se autentica com sucesso, como redirecionar para uma página inicial específica ou registrar o login.
*   **`PerfilUsuario.java`**: Entidade que representa o perfil de um usuário na aplicação. Geralmente contém informações sobre os papéis e permissões associados a um usuário.
*   **`Permissao.java`**: Entidade que representa uma permissão específica dentro do sistema. Define quais ações ou recursos podem ser acessados por um determinado perfil ou usuário.
*   **`PermissaoBusinessService.java`**: Camada de serviço que lida com a lógica de negócios relacionada às permissões. Pode incluir métodos para verificar permissões, atribuir permissões a perfis, etc.
*   **`UsuarioDetailsService.java`**: Implementação da interface `UserDetailsService` do Spring Security. É responsável por carregar os detalhes do usuário (como nome de usuário, senha e autoridades/permissões) a partir de uma fonte de dados (geralmente o banco de dados) durante o processo de autenticação.