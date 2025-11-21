# Manual do Usuário – Módulo Jurídico

Versão: 2.1.0
Ambiente: `http://localhost:8080/`

## Páginas

### 1. Dashboard Jurídico (`/juridico`)
- Objetivo: visão geral de contratos, processos, prazos e compliance.
- Elementos:
  - Cards de métricas: contratos ativos, processos em andamento, prazos a vencer, alertas de compliance.
  - Ações rápidas: navegue para Contratos, Processos, Documentos, Compliance.
- Exemplo de uso:
  - Acompanhe “Prazos a vencer” e clique para ir ao detalhe do contrato.

### 2. Contratos (`/juridico/contratos`)
- Objetivo: listar, filtrar e acompanhar contratos.
- Filtros:
  - `status`, `tipo`, `numero`, `page`, `size`, `sortBy`, `sortDir`.
- Ações:
  - Criar contrato: preencha título, tipo, data de início, duração, valores.
  - Enviar para análise, aprovar, assinar, ativar: ações disponíveis via endpoints dedicados.
- Exemplo:
  - Criar contrato de prestação de serviços com renovação automática e prazo de notificação de 30 dias.

### 3. Processos (`/juridico/processos`)
- Objetivo: acompanhar processos, audiências e prazos.
- Elementos:
  - Listas por status (em andamento, suspensos, encerrados).
  - Próximas audiências e prazos críticos.
- Exemplo:
  - Registrar processo com número e tribunal, depois adicionar audiência e prazo de 7 dias.

### 4. Compliance (`/juridico/compliance`)
- Objetivo: auditorias e não conformidades.
- Elementos:
  - Métricas de conformidade, contagem de não conformidades e auditorias pendentes.
- Ações:
  - Criar auditoria via formulário: tipo, escopo, data, auditor.

### 5. Documentos (`/juridico/documentos`)
- Objetivo: biblioteca de documentos jurídicos e modelos.
- Elementos:
  - Upload de documento: título, categoria, descrição e arquivo.
  - Filtros de listagem: categoria e busca.
  - Modelos de Documentos: lista dinâmica com versão e status; ações “Usar” e “Baixar”.
  - Documentos Recentes: últimos uploads com autor e download.
- Exemplo de uso:
  - Faça upload de um PDF com título “Contrato de Locação”, categoria “Contratos”.
  - Use um modelo publicado para gerar um documento e baixar o arquivo.

## Modelos de Documentos

### Ciclo de Vida
- Estados: `RASCUNHO`, `EM_REVISAO`, `APROVADO`, `PUBLICADO`, `DEPRECADO`, `ARQUIVADO`.
- Regras:
  - Apenas `PUBLICADO` é versão corrente.
  - `DEPRECADO`/`ARQUIVADO` não podem ser usados para novos documentos.
  - Auditoria: registra autor, aprovador e data de publicação.

### Como criar modelo (upload)
- Página: `Documentos` → seção Modelos.
- Ação via API:
  - `POST /juridico/api/modelos/upload-multipart`
  - Campos: `file`, `nome`, `categoria`, `versao` (opcional), `changelog` (opcional).
- Exemplo (cURL):
```
curl -F "file=@modelo_contrato.docx" \
     -F "nome=Modelo de Contrato de Prestação de Serviços" \
     -F "categoria=Contratos" \
     -F "versao=2.1.0" \
     -F "changelog=Atualização de cláusulas de SLA" \
     http://localhost:8080/juridico/api/modelos/upload-multipart
```

### Como publicar, aprovar ou deprecar
- `PUT /juridico/api/modelos/{id}/status?status=APROVADO|PUBLICADO|DEPRECADO|ARQUIVADO`
- Exemplo:
```
curl -X PUT "http://localhost:8080/juridico/api/modelos/10/status?status=APROVADO"
curl -X PUT "http://localhost:8080/juridico/api/modelos/10/status?status=PUBLICADO"
```

### Como listar e baixar modelos
- Listar:
  - `GET /juridico/api/modelos?categoria=Contratos&status=PUBLICADO&search=Prestacao&page=0&size=10`
- Baixar:
  - `GET /juridico/modelos/download/{id}`

### Como usar um modelo para gerar documento
- Na página `Documentos`, clique em “Usar” em um modelo, informe o título.
- API:
  - `POST /juridico/api/documentos/gerar`
  - Campos: `modeloId`, `titulo`, `categoria` (opcional).
- Exemplo (cURL):
```
curl -X POST "http://localhost:8080/juridico/api/documentos/gerar" \
     -d "modeloId=10" -d "titulo=Contrato de Prestação de Serviços - Cliente X"
```

## Documentos

### Upload de Documentos
- `POST /juridico/api/documentos/upload-multipart`
- Campos: `file`, `titulo`, `categoria`, `descricao` (opcional).
- Exemplos de arquivo: `.pdf`, `.doc`, `.docx`, `.png`, `.jpg`.

### Listagem e Download
- Listar:
  - `GET /juridico/api/documentos?categoria=Contratos&search=Locacao&page=0&size=10`
- Download:
  - `GET /juridico/documentos/download/{id}`

## Campos e Botões (Página Documentos)

### Upload
- `Título`: obrigatório, até 120 caracteres.
- `Categoria`: obrigatório; escolha uma categoria existente.
- `Descrição`: opcional, até 200 caracteres.
- `Arquivo`: obrigatório; tipos permitidos e limite de 10MB.
- Botão `Enviar`: executa upload e exibe notificação.

### Filtros
- `Categoria`: lista todas as categorias ativas.
- `Busca`: pesquisa por título ou descrição.
- Botão `Buscar`: carrega lista paginada de documentos.

### Modelos
- Item do modelo: mostra `Nome`, `Categoria`, `vVersão`, `Status`.
- Botão `Usar`: gera documento a partir do modelo.
- Botão `Baixar`: baixa o arquivo do modelo.

## Boas Práticas
- Utilize modelos `PUBLICADO` para novos documentos.
- Mantenha `versao` e `changelog` atualizados em cada alteração.
- Deprecie modelos substituídos e arquive quando necessário.
- Garanta que usuários tenham perfis adequados para cada ação.

## Troubleshooting
- Upload falhou: verifique tipo e tamanho do arquivo.
- Geração deu erro: confirme que o modelo existe e está acessível.
- Download 404: caminho do arquivo pode não existir; reenvie ou atualize o modelo.