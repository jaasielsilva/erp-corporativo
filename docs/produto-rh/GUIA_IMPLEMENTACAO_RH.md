# Guia de Implementação — Produto RH

## Preparação
- Infraestrutura: provisionar ambientes `dev`, `homolog`, `prod` com Java 21 e MySQL 8.
- Segurança: configurar perfis e grupos (`ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL`).
- E-mail/Agenda: parametrizar SMTP e integração de agenda corporativa.

## Instalação
- Banco: criar schema MySQL e executar migrações (DDL conforme entidades JPA).
- Aplicação: configurar `application.properties` com credenciais do banco e SMTP.
- Inicialização: executar a aplicação e validar acesso ao módulo RH (`/rh`).

## Parametrização Inicial
- Cargos e Departamentos: cadastrar estrutura organizacional.
- Políticas de Férias e Ponto: ajustar em `RH > Configurações`.
- Benefícios: configurar catálogos e regras de adesão.
- Perfis de Usuário: vincular colaboradores a usuários com permissões adequadas.

## Dados e Migração
- Importação de colaboradores via API (`POST /rh/colaboradores/novo`) ou CSV (customização).
- Benefícios e históricos: migrar tabelas auxiliares conforme necessidade.
- Folha: validar regras de cálculo e bases (INSS, IRRF, FGTS) e ajustar serviço.

## Testes e Validação
- Cenários: cadastro, benefícios, ponto, folha, férias, recrutamento, treinamentos.
- Relatórios: validar KPIs de turnover, admissões/demissões, benefícios e indicadores.
- Segurança: verificar permissões por perfil e auditoria de acessos/alterações.

## Go-Live
- Treinar equipes de RH e gestores.
- Monitorar logs e auditoria no primeiro ciclo de folha e admissões.
- Ajustar integrações e exportações conforme operação.

