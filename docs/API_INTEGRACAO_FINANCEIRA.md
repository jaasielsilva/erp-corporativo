# Documentação da API de Integração Financeira

## Visão Geral
Esta API permite a integração segura entre o módulo de RH (Folha de Pagamento) e o módulo Financeiro (Contas a Pagar).

## Autenticação
Requer autenticação via Session/Spring Security. O usuário deve ter role `ADMIN`, `MASTER` ou `RH_GESTOR`.

## Endpoints

### 1. Enviar Folha para Financeiro
Transfere uma folha processada para o contas a pagar, realizando validações de integridade e assinatura digital (Hash).

**URL**: `/api/v1/integracao/financeiro/folha/{folhaId}/enviar`
**Método**: `POST`
**Content-Type**: `application/json`

#### Parâmetros de URL
| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| folhaId | Long | ID da Folha de Pagamento |

#### Resposta de Sucesso (200 OK)
```json
{
  "folhaId": 123,
  "valorTotal": 150000.00,
  "dataVencimento": "2025-12-25",
  "hashIntegridade": "a1b2c3d4...",
  "usuarioResponsavel": "gestor@empresa.com",
  "tipoFolha": "MENSAL"
}
```

#### Resposta de Erro (400 Bad Request)
```text
Erro na integração: A folha precisa estar PROCESSADA antes de enviar para o financeiro.
```

## Segurança e Auditoria
- **Hash SHA-256**: Todo envio gera um hash baseado no ID, Valor e Competência para garantir que os dados não foram alterados pós-envio.
- **Auditoria**: Todas as transações são logadas na tabela `rh_auditoria_logs` com categoria `INTEGRACAO`.
- **Tolerância Zero**: O sistema recalcula `Bruto - Descontos` e compara com `Liquido`. Se houver diferença de 0.01 centavo, a operação é abortada.

## Fluxo de Dados
1. **RH**: Processa a folha -> Status `PROCESSADA`.
2. **RH**: Envia via API -> Status `ENVIADA_FINANCEIRO`.
3. **Financeiro**: Recebe `ContaPagar` vinculada.
4. **Financeiro**: Paga -> Status `PAGA`.
