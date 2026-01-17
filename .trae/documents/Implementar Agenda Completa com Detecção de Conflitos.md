Vou implementar o módulo **Agenda** completo, integrando todos os eventos críticos do sistema.

### Plano de Implementação: Agenda Inteligente

1.  **Criar Nova View "Agenda"**:
    *   Substituir o link placeholder da Agenda por uma view real `#view-agenda`.
    *   Implementar um **Calendário Visual** (Grid Mensal) para Janeiro 2026.

2.  **Popular com "Todos os Eventos Possíveis"**:
    *   Vou criar dados fictícios (Mock) cobrindo diversos cenários para Janeiro/2026:
        *   **Audiências** (Civil/Trabalhista).
        *   **Perícias Médicas** (Dr. Roberto, Dra. Ana).
        *   **Prazos Fatais** (Recursos, Contestação).
        *   **Assinaturas de Contrato**.
        *   **Reuniões com Clientes**.

3.  **Sistema de Conflitos e Alertas**:
    *   **Simulação de Erro**: Vou criar propositalmente dois eventos no mesmo dia e horário (ex: 15/01 às 14:00).
    *   **Detecção Automática**: Ao carregar a agenda, o sistema identificará o conflito.
    *   **Alerta Visual**: Exibirei um alerta flutuante (Toast/Modal) avisando: *"Conflito detectado: 2 eventos às 14:00. Por favor, re-agende."*
    *   Esse alerta sumirá após 15 segundos (conforme pedido) ou terá um contador regressivo.

4.  **Funcionalidade**:
    *   Clicar num dia do calendário mostra a lista de eventos daquele dia.
    *   Destaque visual (cores) para cada tipo de evento (Audiência = Vermelho, Perícia = Azul, etc.).

Vou transformar o módulo Agenda em uma ferramenta central de controle de tempo. Podemos começar?