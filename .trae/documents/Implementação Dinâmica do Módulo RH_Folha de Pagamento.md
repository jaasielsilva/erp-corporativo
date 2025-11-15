## Objetivo
Transformar todas as páginas de `RH/Folha de Pagamento` em visões dinâmicas integradas ao banco, aproveitando os serviços e entidades já existentes, com proteção de acesso adequada e geração de holerite/relatórios.

## Arquitetura Atual (pontos de apoio)
- Controller da folha: `src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java` (rotas já criadas: index, gerar, processar, visualizar, listar, holerite, descontos, relatórios). Exemplos:
  - Index: linhas 44–56; Geração: 61–74; Processar: 79–100; Visualizar: 105–118; Listar: 123–133; Holerite: 178–192.
- Serviços: cálculo e agregações
  - `FolhaPagamentoService` com geração/fechamento/cancelamento e cálculos de INSS/IRRF/FGTS (1–407), ex.: gerar folha em 84–146.
  - `HoleriteService` com busca/listagens e resumo da folha (1–288), ex.: `calcularResumoFolha` em 115–128.
- Acesso RH já disponível via atributos globais: `podeAcessarRH` e `podeGerenciarRH` em `GlobalControllerAdvice`.
- PDF já existente no projeto (OpenPDF/iText) para reaproveitar padrão de geração.

## Entregáveis
1. Páginas Thymeleaf dinâmicas: `index.html`, `gerar.html`, `visualizar.html`, `listar.html`, `holerite.html`, `descontos.html`, `relatorios.html` sob `templates/rh/folha-pagamento/`.
2. Geração/fechamento/cancelamento de folha com feedback e validações.
3. Visualização de holerites com cálculo/resumo; download de holerite em PDF.
4. Segurança aplicada nas rotas com `@PreAuthorize` (acesso x gerência) e uso dos atributos globais na UI.
5. Documentação curta de uso na própria pasta (`README-curto.txt`).

## Plano de Implementação
### 1) Segurança e Controle de Acesso
- Anotar rotas do `FolhaPagamentoController`:
  - Acesso geral (index, listar, visualizar, holerite, descontos, relatórios): `@PreAuthorize("@globalControllerAdvice.podeAcessarRH()")`.
  - Ações administrativas (processar, fechar, cancelar): `@PreAuthorize("@globalControllerAdvice.podeGerenciarRH()")`.
- Expor botões/menus na UI condicionados aos atributos `podeAcessarRH` e `podeGerenciarRH`.

### 2) Templates Dinâmicos (Thymeleaf)
- `index.html`:
  - Lista `folhasRecentes` e destaca `folhaAtual`; mostra status e totais com `resumoAtual` quando existir.
  - Botões “Gerar folha” e “Visualizar” por período.
- `gerar.html`:
  - Form com `mesAtual` e `anoAtual`; exibe `existeFolhaAtual` para bloquear duplicidades; envia POST a `/processar`.
- `visualizar.html`:
  - Mostra dados da folha (`folha`), tabela de `holerites` com colunas chave (colaborador, proventos, descontos, líquido) e ações (abrir holerite, baixar PDF).
  - `resumo` com totais (proventos/descontos/líquido/quantidade).
  - Botões “Fechar” e “Cancelar” condicionados ao status.
- `listar.html`:
  - Filtro por ano; tabela com todas as folhas e ações.
- `holerite.html`:
  - Exibe informações do colaborador, período, número (`numeroHolerite`), totais e itens básicos (benefícios/descontos calculados pelo serviço).
  - Ação “Baixar PDF”.
- `descontos.html`:
  - Lista colaboradores ativos e permite navegar para holerites/lançar descontos (fase 2: CRUD de descontos avulsos).
- `relatorios.html`:
  - Seletores de colaborador/departamento/período e resumo da folha atual com `HoleriteService.calcularResumoFolha`.

### 3) Ações e Serviços
- Reaproveitar `FolhaPagamentoService.gerarFolhaPagamento(...)` (84–146) e métodos de fechamento/cancelamento (362–391).
- Reaproveitar `HoleriteService` para listagens e resumo; incluir endpoint de exportação PDF do holerite.
- PDF: criar método no controller (ou serviço) usando OpenPDF (mesma abordagem vista em `UsuarioController.gerarPdfUsuario`).

### 4) Preenchimentos de Modelo
- Index/Gerar/Listar/Visualizar já populam `Model` (linhas citadas); ajustar templates para consumir exatamente os atributos existentes: `folhasRecentes`, `folhaAtual`, `holerites`, `resumo`, `mesAtual`, `anoAtual`, `existeFolhaAtual`.

### 5) Lacunas e Melhorias Planejadas
- Criar templates ausentes: `listar.html`, `visualizar.html`, `holerites-colaborador.html` (referenciados pelo controller).
- Incluir ação “Exportar holerite PDF” e endpoint `/holerite/{id}/pdf` com OpenPDF.
- Fase 2 (opcional pós-entrega):
  - Rubricas configuráveis (proventos/descontos) e lançamentos manuais.
  - Integração com registro de ponto real e feriados regionais.
  - Integração com financeiro (gerar lançamentos/ordens de pagamento).

### 6) Validação
- Testar com perfis distintos (RH, GERENCIAL, ADMIN/MASTER) verificando menus e acessos.
- Gerar folha dos meses de teste; abrir `visualizar`, conferir totais; abrir holerites individuais; exportar PDF.

### 7) Cronograma
- Dia 1: segurança, index/gerar/listar dinâmicos.
- Dia 2: visualizar/holerite dinâmicos + PDF.
- Dia 3: relatórios/descontos + polimento e validações.

## Observação
Sem executar alterações agora; aguardo sua confirmação para aplicar os ajustes nos templates, adicionar `@PreAuthorize`, criar os templates faltantes e incluir o export em PDF, seguindo rigorosamente o estilo do projeto.