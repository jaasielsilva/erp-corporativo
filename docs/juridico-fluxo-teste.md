# Manual de Teste do Módulo Jurídico

Este guia orienta o preenchimento, passo a passo, das páginas do módulo Jurídico, com dados de teste e o ciclo de vida completo (Contratos → Processos → Documentos → Compliance → Dashboard).

## Pré-requisitos
- Usuário autenticado com permissão para acessar o módulo Jurídico.
- Aplicação rodando em `http://localhost:8080/`.
- Acesso às páginas:
  - Dashboard Jurídico: `/juridico`
  - Contratos: `/juridico/contratos`
  - Processos: `/juridico/processos`
  - Documentos: `/juridico/documentos`
  - Compliance: `/juridico/compliance`

## Visão Geral do Fluxo
1. Criar e movimentar um contrato (análise, aprovação, ativação, suspensão).
2. Registrar um processo, adicionar audiência e prazo, concluir o prazo, encerrar o processo.
3. Subir documentos e verificar agrupamento por categorias e recentes.
4. Criar auditoria de compliance e acompanhar indicadores.
5. Validar métricas no Dashboard Jurídico.

---

## 1) Contratos
Página: `/juridico/contratos`

### 1.1 Criar contrato
Preencha o formulário (se disponível) ou utilize os seguintes dados de teste:
- Título: `Contrato de Prestação de Serviços TI`
- Tipo: `PRESTACAO_SERVICOS`
- Descrição: `Suporte e manutenção de infraestrutura`
- Data de Início: `2025-11-01`
- Duração (meses): `12`
- Valor Mensal: `7.500,00`
- Valor Total (opcional): `90.000,00`
- Renovação Automática: `Sim`
- Prazo de Notificação: `30`
- Número do Contrato: `CT-2025-001`

Após salvar, o contrato aparece na lista. Use filtros por `status`, `tipo` e ordenação por datas/valores conforme necessidade.

### 1.2 Ciclo de vida
Executar, na ordem:
1. Enviar para análise
2. Aprovar contrato (com observações)
3. Assinar contrato
4. Ativar contrato
5. Suspender (ex.: motivo: `Inadimplência`)

Resultados esperados:
- Estatísticas e agregados (valor total ativos, receita mensal recorrente) atualizam na própria página.
- Contrato aparece com o `status` correspondente após cada ação.

---

## 2) Processos
Página: `/juridico/processos`

### 2.1 Criar processo
Dados de teste:
- Número: `1234567-89.2025.8.26.0100`
- Tipo: `TRABALHISTA`
- Tribunal: `TRT-2`
- Parte: `Fulano da Silva vs. Empresa`
- Assunto: `Horas extras`
- Status: `EM_ANDAMENTO`

Após salvar, o processo aparece nas listas separadas por status.

### 2.2 Adicionar audiência
Dados de teste:
- Data/Hora: `2025-12-05 10:30`
- Tipo: `INSTRUCAO`
- Observações: `Testemunhas convocadas`

Verifique a sessão de Próximas Audiências.

### 2.3 Adicionar prazo
Dados de teste:
- Data Limite: `2025-11-30`
- Descrição: `Apresentar contestação`
- Responsável: `juridico`

Após criado, o prazo aparece na seção de prazos do processo.

### 2.4 Concluir prazo e encerrar processo
- Conclua o prazo criado e valide que o campo `cumprido` muda para verdadeiro.
- Atualize o status do processo para `ENCERRADO`.

Resultados esperados:
- “Em andamento”, “Suspensos” e “Encerrados” refletem as mudanças.
- “Prazos críticos” e “Próximas audiências” atualizam conforme datas.

---

## 3) Documentos
Página: `/juridico/documentos`

### 3.1 Upload simples (sem arquivo físico)
Dados de teste:
- Título: `Contrato de Locação - Filial Norte`
- Categoria: `Contratos`
- Descrição: `Assinado digitalmente`
- Caminho do Arquivo: `uploads/juridico/documentos/contrato_filial_norte.pdf`

### 3.2 Upload multipart (com arquivo)
Selecione um arquivo PDF qualquer e preencha:
- Título: `Aditivo Contratual 01`
- Categoria: `Aditivos`
- Descrição: `Reajuste anual`

Resultados esperados:
- “Categorias de documentos” atualiza a contagem por categoria.
- “Documentos recentes” mostra os últimos 5 por data de upload.

---

## 4) Compliance
Página: `/juridico/compliance`

### 4.1 Criar auditoria de compliance
Dados de teste:
- Tipo: `INTERNA`
- Escopo: `LGPD`
- Data Início: `2025-11-25`
- Auditor: `Equipe Compliance`

Observação: Use a opção real de criação de auditoria para refletir nos indicadores. Se utilizar uma ação de demonstração, ela pode não persistir.

### 4.2 Não conformidades e normas
- Liste as não conformidades e altere o status (resolver/reabrir) para ver o impacto no indicador de conformidade.
- Consulte normas e verifique códigos/status.

Resultados esperados:
- “Status de Compliance” exibe: % de conformidade, total de NCs, auditorias pendentes e data da última auditoria, todos calculados do banco.

---

## 5) Dashboard Jurídico
Página: `/juridico`

Elementos principais:
- Contratos próximos ao vencimento (30 dias)
- Processos urgentes e últimas atividades
- Indicadores gerais do módulo

Após concluir os passos anteriores, o dashboard refletirá os dados reais criados.

---

## Dicas e Validações
- Utilize filtros e paginação nas listas para explorar os dados.
- Garanta que datas (audiências e prazos) estejam no futuro para aparecerem nas seções de destaque.
- Em caso de erro de validação, revise campos obrigatórios (ex.: número do contrato único, status compatível com ação).

## Referências Técnicas (para suporte)
- Contratos: `src/main/java/com/jaasielsilva/portalceo/controller/JuridicoController.java:92–150`
- Processos (APIs): `JuridicoController.java:653, 670, 694, 719, 730`
- Documentos (APIs): `JuridicoController.java:855, 916, 930, 962, 977`
- Compliance (views e APIs): `JuridicoController.java:274, 744, 757, 792, 827, 844, 859`
- Dashboard Jurídico: `JuridicoController.java:1–90`