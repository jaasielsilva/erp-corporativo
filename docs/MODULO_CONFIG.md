# Documentação do Módulo `config`

O módulo `config` contém as classes de configuração e inicialização da aplicação. Ele é responsável por configurar aspectos importantes como segurança, auditoria, cache, tratamento de datas, e a inicialização de dados no banco de dados.

## Arquivos e suas Finalidades:

*   **`AsyncConfig.java`**: Configuração para execução assíncrona de métodos, permitindo que certas operações sejam executadas em segundo plano, sem bloquear o fluxo principal da aplicação.
*   **`AuditorAwareImpl.java`**: Implementação da interface `AuditorAware` do Spring Data JPA, que permite a auditoria automática de entidades, registrando o usuário que criou ou modificou um registro.
*   **`CacheConfig.java`**: Configuração do mecanismo de cache da aplicação, otimizando o desempenho ao armazenar em memória dados frequentemente acessados.
*   **`DataLoader.java`**: Classe responsável por carregar dados iniciais no banco de dados quando a aplicação é iniciada. Útil para popular o banco com dados de teste ou configurações padrão.
*   **`DatabaseInitializer.java`**: Componente que pode ser usado para inicializar ou migrar o esquema do banco de dados, garantindo que a estrutura esteja correta na inicialização da aplicação.
*   **`DateConfig.java`**: Configurações relacionadas ao formato e tratamento de datas na aplicação, garantindo consistência na serialização e desserialização de objetos de data.
*   **`GlobalControllerAdvice.java`**: Um `@ControllerAdvice` global que lida com exceções e pré-processamento de requisições para todos os controladores da aplicação, centralizando o tratamento de erros.
*   **`JpaAuditingConfig.java`**: Habilita a funcionalidade de auditoria do Spring Data JPA, que, em conjunto com `AuditorAwareImpl`, permite o registro automático de informações de criação e modificação em entidades.
*   **`MultipartConfig.java`**: Configuração para o tratamento de requisições `multipart/form-data`, geralmente usadas para upload de arquivos.
*   **`SecurityConfig.java`**: Configuração principal de segurança da aplicação, definindo regras de acesso, autenticação e autorização.
*   **`SecurityInterceptor.java`**: Um interceptador de segurança que pode ser usado para aplicar lógica de segurança adicional antes ou depois da execução de métodos de controladores.
*   **`UsuarioLogadoControllerAdvice.java`**: Um `@ControllerAdvice` específico para injetar informações do usuário logado em modelos de visualização ou para outras finalidades relacionadas ao usuário autenticado.
*   **`UsuarioLogadoInterceptor.java`**: Interceptador para lidar com informações do usuário logado, como adicionar o usuário logado ao contexto da requisição.
*   **`WebSecurityConfig.java`**: Configuração de segurança web, complementando `SecurityConfig` com configurações específicas para a camada web, como CORS, CSRF e gerenciamento de sessões.
*   **`WebSocketConfig.java`**: Configuração para o suporte a WebSockets, permitindo comunicação bidirecional em tempo real entre o cliente e o servidor.
*   **`WebSocketEventListener.java`**: Um listener para eventos de WebSocket, como conexão e desconexão de clientes, permitindo a execução de lógica personalizada nesses eventos.