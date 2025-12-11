# Manual do Usuário — Módulo RH

## Acesso
- Menu principal: `Recursos Humanos (RH)`.
- Submódulos disponíveis no sidebar: Colaboradores, Folha, Benefícios, Ponto/Escalas, Férias, Avaliação, Recrutamento, Treinamentos, Relatórios, Configurações, Auditoria.

## Colaboradores
- Listar: `RH > Colaboradores > Listar` (`templates/rh/colaboradores/listar.html`).
- Novo cadastro: `RH > Colaboradores > Novo` (`templates/rh/colaboradores/novo.html`).
  - Preencha nome, CPF, e-mail, telefone, data de admissão, cargo, departamento.
  - Adicione benefícios conforme necessário.
- Editar: `RH > Colaboradores > Editar` (`templates/rh/colaboradores/editar.html`).
- Ficha e histórico: `ficha.html`, `historico.html`.

## Benefícios
- Vale-Transporte: `beneficios/vale-transporte/listar.html` e `form.html`.
- Vale-Refeição: `beneficios/vale-refeicao/listar.html` e `form.html`.
- Plano de Saúde: `beneficios/plano-de-saude/adesoes.html`, `plano-saude.html`.

## Folha de Pagamento
- Geração: `folha-pagamento/gerar.html`.
- Visualização: `folha-pagamento/visualizar.html`, `holerite.html`.
- Holerites por colaborador: `holerites-colaborador.html`.
- Descontos: `descontos.html`.

## Ponto e Escalas
- Registros: `ponto-escalas/registros.html`.
- Correções: `ponto-escalas/correcoes.html`.
- Escalas: `ponto-escalas/escalas.html`, `nova.html`, `vigencias.html`.
- Exceções e espelho: `excecoes.html`, `espelho-ponto-pdf.html`.

## Férias
- Solicitar: `ferias/solicitar.html`.
- Aprovar: `ferias/aprovar.html`.
- Planejamento e Calendário: `ferias/planejamento.html`, `ferias/calendario.html`.

## Avaliação de Desempenho
- Configurar periodicidade: `avaliacao/periodicidade.html`.
- Registrar feedbacks: `avaliacao/feedbacks.html`.
- Relatórios: `avaliacao/relatorios.html`.

## Recrutamento
- Triagem, Entrevistas, Pipeline, Vagas, Relatórios: `recrutamento/*.html`.
- Ações: criar vaga, registrar candidatura, agendar entrevista, avaliar.

## Treinamentos
- Cadastro, Cursos, Instrutores, Turmas, Inscrição, Relatórios: `treinamentos/*.html`.
- Certificado: `treinamentos/certificado.html`.

## Relatórios RH
- Turnover, Admissões/Demissões, Férias/Benefícios, Headcount, Indicadores: `relatorios/*.html`.

## Configurações e Auditoria
- Políticas de férias, ponto e integrações: `configuracoes/*.html`.
- Auditoria: acessos, alterações, exportações, revisões: `auditoria/*.html`.

## Perfis de Acesso
- Perfis: `ROLE_RH`, `ROLE_ADMIN`, `ROLE_MASTER`, `ROLE_GERENCIAL`.
- Algumas páginas exigem perfis administrativos para aprovação e relatórios.

## Capturas de Tela (Sugestões)
- Utilize as páginas listadas para capturas: menu lateral, telas de cadastro, listagens, gráficos.
- Recomendações: resolução 1920x1080, modo claro, filtros preenchidos, gráficos atualizados.

## Passo a Passo — Exemplos
- Cadastrar colaborador:
  1. Acesse `RH > Colaboradores > Novo`.
  2. Preencha campos obrigatórios e selecione cargo/departamento.
  3. Adicione benefícios se aplicável.
  4. Salve e confirme mensagem de sucesso.
- Gerar folha:
  1. Acesse `RH > Folha > Gerar`.
  2. Selecione mês/ano e confirme processamento.
  3. Visualize e exporte holerites.

## Limitações e Restrições
- Dados sensíveis: não armazenar senhas em texto plano, biometria bruta, diagnósticos médicos.
- Acesso: relatórios completos restritos a perfis gerenciais e RH.
- Exportações: CSV/PDF podem ter limites de paginação.

