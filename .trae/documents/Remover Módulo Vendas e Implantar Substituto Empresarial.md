## Objetivo
Excluir integralmente o módulo **Vendas** (código, telas e integrações) e propor/implantar um novo módulo empresarial que faça sentido para seu ERP, preservando integrações com Financeiro, Estoque, Clientes, Jurídico e Suporte.

## Diagnóstico do Módulo Vendas
- **Back-end**:
  - Controller: `src/main/java/com/jaasielsilva/portalceo/controller/VendasController.java`
  - Service: `src/main/java/com/jaasielsilva/portalceo/service/VendaService.java`
  - Mapper: `src/main/java/com/jaasielsilva/portalceo/mapper/VendaMapper.java`
  - Repositórios: `src/main/java/com/jaasielsilva/portalceo/repository/VendaRepository.java`, `VendaItemRepository.java`
  - Modelos JPA: `src/main/java/com/jaasielsilva/portalceo/model/Venda.java`, `VendaItem.java`
- **Front-end**:
  - Templates: `src/main/resources/templates/vendas/*` (dashboard, lista, pdv, caixa, novo, clientes, produtos, relatorios, configuracoes)
  - Sidebar: itens e permissões sob `podeAcessarVendas` em `templates/components/sidebar.html:105–120`
- **Integrações**:
  - Financeiro: geração de `ContaReceber` dentro de `VendaService` (método `gerarContasReceberParaVenda`)
  - Estoque: baixa de estoque de `Produto` em `VendaService.salvar`
  - Caixa: `CaixaService.registrarVenda`

## Impactos e Cuidados
- Remover Vendas **afetará**: geração automática de contas a receber, baixa de estoque via PDV e métricas de faturamento/volume.
- Permanece: módulo Financeiro (contas a pagar/receber), Estoque, Clientes, Jurídico, Suporte, Projetos, Marketing.
- **Estratégia**: primeiro neutralizar referências (menu/permissões), depois remover código, e por fim ajustar fluxos para manter Financeiro/Estoque funcionais.

## Plano de Remoção Segura
1. **Desativar Navegação**
   - Remover o menu "Vendas" e seus subitens do `sidebar.html` (blocos `podeAcessarVendas`).
   - Opcional: remover/ajustar flags `podeAcessarVendas` nas políticas.
2. **Remover Páginas**
   - Apagar `templates/vendas/*`.
3. **Remover Back-end**
   - Apagar `VendasController.java`, `VendaService.java`, `VendaMapper.java`.
   - Apagar repositórios `VendaRepository.java`, `VendaItemRepository.java`.
   - Apagar entidades `Venda.java`, `VendaItem.java`.
4. **Revisar Integrações**
   - `ContaReceberService`: garantir que outros módulos (ex.: Contratos) gerem faturamento.
   - `CaixaService`: manter funcional sem registrar vendas.
   - `ProdutoService/Estoque`: desativar baixa automática por vendas; manter entradas/saídas existentes.
5. **Banco de Dados**
   - Manter tabelas `venda` e `venda_item` inicialmente (backup lógico) ou criar migration de drop **apenas após** validação.
6. **Build/Smoke Test**
   - Compilar, subir e validar navegação, Financeiro, Estoque e Jurídico.

## Propostas de Substituição Empresarial
1. **PSA – Professional Services Automation (Serviços/OS)** [Recomendado]
   - Foco em serviços recorrentes/projetos, integrando: Contratos (Jurídico), Timesheets, Ordens de Serviço, faturamento (ContaReceber), SLAs (Suporte).
   - Benefícios: encaixa com módulos atuais (Projetos, RH, Suporte), mantém receita recorrente sem PDV.
2. **Compras & Suprimentos (Procurement)**
   - Se seu negócio exige controle de aquisições e fornecedores; integra Estoque, Financeiro e Fornecedores.
3. **CRM & Propostas Comerciais**
   - Foco em pré-vendas/propostas sem faturamento via PDV; integra Marketing/Leads e Contratos.

## Módulo Proposto (PSA) – Escopo
- **Páginas**: `servicos/dashboard`, `servicos/ordens`, `servicos/timesheets`, `servicos/faturamento`, `servicos/slas`.
- **Entidades**: `OrdemServico`, `Atividade`, `Timesheet`, `FaturaServico` (ou integração direta com `ContaReceber`), `SlaContrato`.
- **Serviços**: 
  - Geração de `ContaReceber` por OS concluída/por período.
  - Integração com `ContratoLegalService` para recorrência e SLAs.
  - Integração com Suporte para tickets vinculados a contratos/OS.
- **Integrações**:
  - Financeiro: faturamento recorrente e por OS.
  - RH: alocação de colaboradores e contabilização de horas.
  - Projetos: planejamento/cronograma e custo-hora.

## Passos de Implantação (PSA)
1. **Modelagem**: criar entidades e repositórios (OrdemServico, Timesheet, etc.).
2. **Serviços**: faturamento por OS/recorrência, geração de contas a receber, relatórios executivos.
3. **Controllers e Templates**: telas de dashboard, ordens, timesheets e faturamento.
4. **Sidebar**: substituir o grupo "Vendas" por "Serviços".
5. **Migração de Fluxos**: redirecionar processos que dependiam de vendas para contratos/OS.
6. **Testes**: unitários (serviços, repositórios) e integração (faturamento, cálculo de horas).

## Cronograma
- Semana 1: remoção do módulo Vendas + neutralização de impactos.
- Semana 2–3: implementação PSA (modelos, serviços, telas principais).
- Semana 4: integração com Financeiro/Jurídico/Suporte + testes e ajustes.

## Riscos e Mitigações
- Perda de baixa automática de estoque: se necessário, implementar consumo de materiais por OS (opcional).
- Faturamento: garantir geração via contratos/OS para substituir vendas.
- Dados de vendas históricos: manter tabelas até validar migração para relatórios executivos.

## Aprovação
Ao aprovar, executo: remoção completa do módulo Vendas, atualização da navegação, ajustes de integrações e inicio a implantação do módulo PSA (Serviços), com entregas incrementais validadas em build/tests.