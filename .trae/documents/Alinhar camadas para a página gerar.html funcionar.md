## Estado Atual
- Controller: `FolhaPagamentoController#gerar` injeta `colaboradores`, `departamentos`, `existeFolhaAtual`, `mesAtual`, `anoAtual` e `diasMesAtual` (src/main/java/com/jaasielsilva/portalceo/controller/rh/folha/FolhaPagamentoController.java:73–86).
- Model: `Colaborador` não possui `diasTrabalhados` e `diasMes` (src/main/java/com/jaasielsilva/portalceo/model/Colaborador.java:26–181).
- Services/Repositories: `ColaboradorService.listarAtivos()` (usa `ColaboradorRepository.findByAtivoTrue`), `DepartamentoService.listarTodos()` (`DepartamentoRepository.findAll`), `FolhaPagamentoService.existeFolhaPorMesAno()` (`FolhaPagamentoRepository.existsByMesReferenciaAndAnoReferencia`). Estão coerentes com o uso na view.
- View: `templates/rh/folha-pagamento/gerar.html` consome os atributos; ainda há rotas de ação inconsistentes (`/folha/...`) e a coluna “Dias Trabalhados” deve depender de `diasMesAtual` e de um valor calculado de dias trabalhados.

## Ajustes de Alinhamento
1. Corrigir navegação de ações
- Trocar `th:onclick` com `/folha/...` por `th:href` para rotas existentes em `FolhaPagamentoController` (ex.: `visualizar/{id}`), ou remover até que os endpoints de detalhes/edição sejam implementados.

2. Padronizar “Dias Trabalhados” na view
- Manter `th:text` com fallback seguro usando `diasMesAtual`.
- Planejar cálculo real via serviço para popular `diasTrabalhados` antes da renderização.

3. Introduzir DTO para a tela (opcional, recomendado)
- Criar `ColaboradorResumoFolhaDTO { id, nome, departamento, salario, diasTrabalhados }`.
- Serviço calculará `diasTrabalhados` por colaborador para `mes/ano` e retornará lista de DTOs para a view.
- Atualizar a view para iterar sobre DTOs em vez da entidade.

4. Usar `departamentos` do modelo (opcional)
- Substituir o `<select>` estático por opções oriundas de `departamentos` para consistência com o controller.

## Validação
- Liberar a porta `8080` ou ajustar `server.port` e iniciar.
- Login e acesso a `GET /rh/folha-pagamento/gerar` para confirmar:
  - Renderização sem erros.
  - “Dias Trabalhados” exibindo corretamente (fallback ou cálculo).
  - Navegação das ações consistente (sem 404).

## Observações
- `GlobalControllerAdvice` injeta `usuarioLogado`, `nivelAcesso` e flags (`podeGerenciarRH`, etc.), compatíveis com os fragments e condicionais da página.
- Os serviços e repositórios atuais suportam a página; a principal lacuna funcional é o cálculo de `diasTrabalhados` (inexistente no `Colaborador`).