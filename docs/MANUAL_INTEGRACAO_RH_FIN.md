# Manual de Operação Integrada RH-Financeiro

## Introdução
Este manual descreve o novo fluxo automatizado de integração entre o processamento da Folha de Pagamento e o Contas a Pagar.

## Perfil RH (Gestor)

### 1. Processamento da Folha
1. Acesse **RH > Folha de Pagamento > Gerar Folha**.
2. Realize o processamento normal da competência.
3. Verifique os totais e holerites gerados.

### 2. Envio para Financeiro
1. Na tela de **Visualização da Folha**, após conferir todos os cálculos, clique em **"Fechar Folha"** (ícone de cadeado).
2. O status mudará para **FECHADA**, indicando que os valores estão consolidados.
3. Clique no botão **"Enviar Financeiro"** (ícone de avião de papel).
4. Confirme a operação.
   - O sistema validará automaticamente a consistência matemática (Tolerância Zero).
   - Será gerado um Hash de Integridade.
   - A folha mudará para status **ENVIADA_FINANCEIRO**.

## Perfil Financeiro

### 1. Recebimento
1. Acesse **Financeiro > Contas a Pagar**.
2. Localize o registro com Categoria **SALARIOS** e descrição "Folha de Pagamento - Mês/Ano".
3. O campo "Observações" conterá o Hash de Integridade para auditoria.

### 2. Pagamento
1. Realize a conferência bancária (CNAB).
2. Aprove o pagamento no sistema.
3. O status da Folha será atualizado automaticamente para **PAGA** (requer implementação futura de webhook bancário ou baixa manual).

## Auditoria e Segurança
- Todas as operações ficam registradas em **Configurações > Auditoria**.
- O Hash garante que os valores aprovados pelo RH são exatamente os mesmos que chegaram ao Financeiro.
