# Manual do Usuário — RH/Recrutamento

## Acesso

- Candidatos: `http://localhost:8080/rh/recrutamento/candidatos`
- Vagas: `http://localhost:8080/rh/recrutamento/vagas`
- Pipeline: `http://localhost:8080/rh/recrutamento/pipeline`
- Relatórios: `http://localhost:8080/rh/recrutamento/relatorios`

## Casos de Uso

- Cadastrar candidato com histórico e habilidades
- Criar vaga e divulgar
- Cadastrar candidatura e agendar entrevista
- Avaliar candidato e promover etapa
 - Filtrar pipeline por etapa e por vaga
 - Visualizar conversão por etapa em gráficos

## Passo a Passo

- Criar candidato via página de candidatos
- Adicionar experiência, formação e habilidades via API ou UI
- Criar vaga e listar
- Cadastrar candidatura e agendar entrevista
- Avaliar e promover etapa até contratação ou reprovação
 - No Pipeline, use os filtros de etapa e vaga, altere a etapa pelo seletor e utilize os botões de agendar e avaliar
 - Em Relatórios, utilize o gráfico para acompanhar conversão por etapa

## Screenshots

- Capturar páginas de candidatos, vagas, pipeline e relatórios

## Perguntas Frequentes

- Etapas: TRIAGEM, ENTREVISTA, OFERTA, CONTRATADO, REPROVADO
- Exportação: CSV/PDF disponível nos relatórios
 - Integração externa: utilize o endpoint de divulgação externa nas vagas

## Troubleshooting

- E-mails não enviados: verificar SMTP
- Agenda sem lembrete: checar criação de evento
