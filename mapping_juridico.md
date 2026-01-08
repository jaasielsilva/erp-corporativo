# Mapeamento do Módulo Jurídico - ERP Corporativo

Este documento serve como base de conhecimento para as permissões e funcionalidades do módulo Jurídico.

## 1. Permissões de Acesso (MAPA_PERMISSOES)

As permissões abaixo estão registradas na tabela `mapa_permissoes` e são utilizadas para controle de visibilidade em tela e acesso a APIs.

| ID  | Descrição | Módulo | Chave (Permissão) | Recurso | Tipo |
|:---:|:---|:---:|:---|:---|:---:|
| 381 | Menu Jurídico | Jurídico | `MENU_JURIDICO` | Menu Principal | MENU |
| 382 | Dashboard Jurídico | Jurídico | `MENU_JURIDICO_DASHBOARD` | Dashboard | PÁGINA |
| 199 | Exportação de Contratos | Jurídico | `JURIDICO_CONTRATOS_EXPORTAR` | Ações Jurídico | AÇÃO |
| 200 | Exportação de Processos | Jurídico | `JURIDICO_PROCESSOS_EXPORTAR` | Ações Jurídico | AÇÃO |
| 201 | Exportação Previdenciária | Jurídico | `JURIDICO_PREVIDENCIARIO_EXPORTAR` | Ações Jurídico | AÇÃO |
| 396 | Novo Processo | Jurídico | `MENU_JURIDICO_PROCESSOS_NOVO` | Gestão de Processos | BOTAO |
| 388 | Novo Previdenciário | Jurídico | `MENU_JURIDICO_PREVIDENCIARIO_NOVO` | Previdenciário | BOTAO |

## 2. Segregação de Funções (Roles)

| Perfil | Permissões de Exportação | Observações |
|:---|:---:|:---|
| **ADMIN** | Todas | Acesso total ao sistema. |
| **GERENTE_JURIDICO** | Todas | Gestão total do departamento jurídico. |
| **ADVOGADO** | Processos e Contratos | Foco na operação judicial e contratual. |
| **ESTAGIARIO_JURIDICO** | Previdenciário | Acesso restrito ao fluxo previdenciário. |

## 3. Endpoints de Exportação

| Funcionalidade | Endpoint | Permissão Necessária |
|:---|:---|:---|
| Exportar Contratos (XLSX) | `/api/juridico/relatorios/contratos/export` | `JURIDICO_CONTRATOS_EXPORTAR` |
| Exportar Processos (XLSX) | `/api/juridico/relatorios/processos/export` | `JURIDICO_PROCESSOS_EXPORTAR` |
| Exportar Previdenciário (XLSX) | `/api/juridico/relatorios/previdenciario/export` | `JURIDICO_PREVIDENCIARIO_EXPORTAR` |

## 4. Auditoria

Todas as exportações são registradas no log de auditoria com os seguintes detalhes:
- **Evento:** `DOCUMENTO_ENVIADO` (Utilizado para rastreabilidade de extração de dados)
- **Descrição:** "Exportação de relatórios: [Nome do Relatório]"
- **Dados:** IP do solicitante e Session ID.
