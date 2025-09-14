# Guia Rápido - Sistema de Chamados

## Como Abrir um Chamado

### Via Interface Web

1. **Acesse o Sistema**
   - URL: `http://localhost:8080/suporte`
   - Faça login com suas credenciais

2. **Criar Novo Chamado**
   - Clique em "Novo Chamado" ou acesse `/suporte/novo`
   - Preencha os campos obrigatórios:
     - **Assunto**: Título resumido do problema
     - **Descrição**: Detalhes completos da solicitação
     - **Prioridade**: Selecione conforme urgência
   - Clique em "Criar Chamado"

3. **Acompanhar Chamado**
   - Anote o número gerado (formato: CHM-YYYYMMDDHHMMSS)
   - Acesse `/suporte/chamados/{id}` para visualizar

### Via API REST

```bash
# Criar chamado
curl -X POST http://localhost:8080/api/chamados \
  -H "Content-Type: application/json" \
  -d '{
    "assunto": "Problema no sistema",
    "descricao": "Descrição detalhada do problema",
    "prioridade": "ALTA",
    "solicitante": "João Silva",
    "email": "joao@empresa.com"
  }'

# Consultar chamado
curl http://localhost:8080/api/chamados/1

# Listar todos os chamados
curl http://localhost:8080/api/chamados
```

## Prioridades e SLA

| Prioridade | Tempo SLA | Quando Usar |
|------------|-----------|-------------|
| **CRITICA** | 2 horas | Sistema fora do ar, perda de dados |
| **ALTA** | 4 horas | Funcionalidade importante indisponível |
| **MEDIA** | 8 horas | Problemas que afetam produtividade |
| **BAIXA** | 24 horas | Melhorias, dúvidas, problemas menores |

## Status do Chamado

- 🔴 **ABERTO**: Aguardando atendimento
- 🟡 **EM_ANDAMENTO**: Técnico trabalhando na solução
- 🟢 **RESOLVIDO**: Solução implementada
- ⚫ **FECHADO**: Chamado finalizado

## Dicas para um Bom Chamado

### ✅ Faça
- Seja específico no assunto
- Descreva o problema detalhadamente
- Inclua passos para reproduzir o erro
- Mencione quando o problema começou
- Informe seu email para contato

### ❌ Evite
- Assuntos vagos como "Sistema não funciona"
- Descrições muito curtas
- Abrir múltiplos chamados para o mesmo problema
- Usar prioridade CRITICA desnecessariamente

## Exemplo de Chamado Bem Estruturado

**Assunto**: `Erro ao gerar relatório de vendas - Módulo Financeiro`

**Descrição**:
```
Problema: Ao tentar gerar o relatório mensal de vendas, o sistema retorna erro 500.

Passos para reproduzir:
1. Acessar Módulo Financeiro > Relatórios
2. Selecionar "Relatório de Vendas"
3. Escolher período: 01/12/2024 a 31/12/2024
4. Clicar em "Gerar Relatório"
5. Sistema exibe mensagem de erro

Mensagem de erro: "Erro interno do servidor - Contate o administrador"

Impacto: Não conseguimos fechar o mês sem este relatório
Urgência: Precisamos resolver até amanhã para apresentação

Informações adicionais:
- Usuário: João Silva (Financeiro)
- Navegador: Chrome 120.0
- Horário do erro: 14:30 de hoje
- Outros relatórios funcionam normalmente
```

**Prioridade**: `ALTA`

## URLs Importantes

- **Dashboard**: `/suporte`
- **Novo Chamado**: `/suporte/novo`
- **Lista de Chamados**: `/suporte/chamados`
- **API Base**: `/api/chamados`

## Contatos

- **Suporte Técnico**: suporte@empresa.com
- **Emergências**: (11) 9999-9999
- **Chat Interno**: Disponível no sistema ERP

---

*Última atualização: Janeiro 2025*