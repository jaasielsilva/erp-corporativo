# Arquitetura do Módulo RH/Treinamentos

## Estrutura

- Modelos: Curso, Instrutor, Turma, Matricula, Frequencia
- Repositórios: por entidade
- Serviço: `TreinamentoService` com regras de negócio
- Controladores: API (`/api/rh/treinamentos/*`) e MVC (`/rh/treinamentos/relatorios`)
- Integração: Agenda corporativa via `Evento`
- Compatibilidade: uso de `Colaborador` e RBAC padrão RH

## Endpoints

- `POST /api/rh/treinamentos/cursos`
- `POST /api/rh/treinamentos/instrutores`
- `POST /api/rh/treinamentos/turmas`
- `POST /api/rh/treinamentos/turmas/{id}/status`
- `GET  /api/rh/treinamentos/turmas`
- `POST /api/rh/treinamentos/turmas/{turmaId}/matriculas`
- `POST /api/rh/treinamentos/matriculas/{matriculaId}/frequencia`
- `POST /api/rh/treinamentos/matriculas/{matriculaId}/avaliacao`
- `GET  /api/rh/treinamentos/relatorios/turmas`
- `GET  /api/rh/treinamentos/relatorios/export.csv`
- `GET  /api/rh/treinamentos/relatorios/export.pdf`
- `POST /api/rh/treinamentos/turmas/{id}/enviar-lembretes`
- `GET  /api/rh/treinamentos/certificados/{matriculaId}.pdf`

## Fluxo de Dados

- Criação de turma gera evento na agenda
- Matrículas vinculam colaboradores a turmas
- Frequência registra presença por matrícula
- Avaliação pós-treinamento registra nota/feedback por matrícula
- Relatórios agregam participação, presença e média de avaliação

## Diagrama de Sequência (mermaid)

```mermaid
sequenceDiagram
participant RH
participant API
participant Service
participant DB
participant Agenda
RH->>API: POST /turmas
API->>Service: criarTurma
Service->>DB: salvar turma
Service->>Agenda: criar Evento
Service->>DB: vincular agendaEventoId
API-->>RH: turma criada
RH->>API: POST /turmas/{id}/matriculas
API->>Service: matricular
Service->>DB: salvar matrícula
API-->>RH: matrícula criada
RH->>API: POST /matriculas/{id}/avaliacao
API->>Service: avaliar
Service->>DB: salvar avaliação
API-->>RH: avaliação registrada

## Segurança

- `@PreAuthorize` com perfis `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL`

## Casos de Uso

1. Treinamento obrigatório de Compliance (LGPD)
- Objetivo: garantir conformidade legal de todos os colaboradores
- Fluxo: criação de curso → abertura de turmas por área → inscrições em massa → controle de presença → avaliação pós-treinamento → relatório para diretoria
- Benefícios: redução de risco regulatório, evidências auditáveis

2. Capacitação técnica de equipes (ferramentas internas)
- Objetivo: elevar eficiência operacional com novos recursos
- Fluxo: projeto de curso → seleção de instrutor → cronograma integrado à Agenda → presença acompanhada por sessão → coleta de feedback → indicadores por time
- Benefícios: ganho de produtividade e menores erros operacionais

3. Integração de novos colaboradores (onboarding)
- Objetivo: acelerar ramp-up com trilhas de treinamento
- Fluxo: cursos base (cultura, segurança, ferramentas) → turmas quinzenais → inscrições automáticas por RH → presença e avaliações → relatórios por lotação
- Benefícios: menor tempo de adaptação e melhor retenção

## Exemplos e Resolução de Problemas

- Superlotação de turmas: sistema bloqueia novas matrículas; abrir nova turma ou ampliar capacidade
- Faltas recorrentes: usar taxa de presença por turma para identificar gargalos de horário/local; replanejar agenda
- Baixa avaliação média: analisar feedbacks, ajustar conteúdo ou instrutor, criar material de apoio

## Exportação e Integrações

- CSV: dados tabulares para BI/planilhas (`/relatorios/export.csv`)
- PDF: relatório pronto para compartilhamento (`/relatorios/export.pdf`)
- Agenda: criação de `Evento` com lembrete ao abrir turma
- E-mail: confirmação de matrícula e lembretes de sessão

## Requisitos do Sistema

- Java 17, Spring Boot
- Banco relacional compatível com JPA
- Serviço de e-mail configurado (`EmailService`)
- Dependência `openhtmltopdf` para renderização de PDFs

## Configurações Necessárias

- Permissões RBAC (`ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL`)
- Propriedades de e-mail SMTP
- Templates Thymeleaf ativos e recursos estáticos

## Fluxogramas

```mermaid
flowchart TD
A[Criar Curso] --> B[Criar Instrutor]
B --> C[Abrir Turma]
C --> D[Matricular]
D --> E[Registrar Frequência]
E --> F[Avaliar]
F --> G[Relatórios / Exportar]
```

## Perguntas Frequentes e Troubleshooting

- Erro de 404 em páginas: verificar caminhos `/rh/treinamentos/*` e sufixos com ponto
- Falha em exportação PDF: conferir dependência `openhtmltopdf` e fontes
- E-mails não enviados: revisar configuração SMTP e permissões do serviço

## Histórico de Versões

- v1.0 – Implementação inicial (cadastro, turmas, matrículas, frequência, relatórios)
- v1.1 – Adição de avaliações, exportação CSV/PDF, lembretes por e-mail e Agenda
- v1.2 – Páginas de cadastro, inscrição e certificado
