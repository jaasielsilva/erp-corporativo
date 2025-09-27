package com.jaasielsilva.portalceo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket para o Sistema de Chat Interno
 * Habilita comunicação em tempo real via STOMP sobre WebSocket
 * 
 * @author Sistema ERP
 * @version 2.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura o message broker para roteamento de mensagens
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita um message broker simples em memória
        // Prefixos para destinos que o broker irá gerenciar
        config.enableSimpleBroker(
            "/topic",    // Para mensagens broadcast (públicas)
            "/queue",    // Para mensagens diretas (privadas)
            "/user"      // Para mensagens específicas do usuário
        );
        
        // Prefixo para mensagens enviadas do cliente para o servidor
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefixo para mensagens direcionadas a usuários específicos
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Registra endpoints STOMP para conexão WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal para conexão WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:8080", "https://localhost:8080", "http://127.0.0.1:8080") // Origens específicas e seguras
                .withSockJS(); // Habilitar fallback SockJS para navegadores sem suporte WebSocket
        
        // Endpoint alternativo sem SockJS (para clientes que suportam WebSocket nativo)
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("http://localhost:8080", "https://localhost:8080", "http://127.0.0.1:8080");
    }
    
    /**
     * Configurações adicionais do message broker (opcional)
     * Pode ser usado para configurar brokers externos como RabbitMQ ou ActiveMQ
     */
    /*
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Para usar RabbitMQ como message broker (produção)
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
        
        config.setApplicationDestinationPrefixes("/app");
    }
    */
}