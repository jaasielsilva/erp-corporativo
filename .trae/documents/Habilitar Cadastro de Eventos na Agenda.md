Atualmente não (é apenas visual), mas **vou implementar agora**.

### Plano de Implementação: Cadastro de Eventos

1.  **Botão "Novo Evento"**:
    *   Adicionar um botão `+ Novo Agendamento` no cabeçalho da Agenda.

2.  **Modal de Cadastro**:
    *   Criar um formulário Modal (`modal-novo-evento`) com:
        *   **Título** (ex: Audiência).
        *   **Tipo** (Select: Audiência, Perícia, Prazo, Reunião).
        *   **Data e Hora**.
        *   **Cliente** (Texto livre ou Select).
        *   Botão **Salvar**.

3.  **Lógica JS**:
    *   Ao salvar, o evento será adicionado ao array `agendaEvents`.
    *   A função `renderCalendar()` será chamada para atualizar a tela instantaneamente.
    *   O sistema de **Conflitos** rodará automaticamente: se você tentar agendar no mesmo horário de outro evento, o alerta de erro aparecerá na hora.

Vou tornar a agenda funcional para você testar a criação de eventos e conflitos. Posso prosseguir?