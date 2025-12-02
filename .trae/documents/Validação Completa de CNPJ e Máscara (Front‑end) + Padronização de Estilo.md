## Objetivo
Adicionar máscara e validação completa dos dígitos do CNPJ no front‑end, e padronizar as páginas de consulta/processamento para o estilo visual do sistema (Bootstrap local, style.css, sidebar.css, notifications.css, Font Awesome), mantendo compatibilidade com Thymeleaf.

## Escopo
- Foco: páginas e scripts de consulta de CNPJ e processamento em massa.
- Defesa em profundidade: validar também no back‑end (opcional, recomendado).

## Alterações no Front‑end
### Padronização de HTML/Estilo
- Atualizar `templates/cadastros/consultar-cnpj.html` e `templates/utilidades/processar-cnpj.html` para:
  - `<!DOCTYPE html>`, `lang="pt-BR"`, `xmlns:th="http://www.thymeleaf.org"`.
  - Links: `/css/bootstrap.min.css`, `/css/style.css`, `/css/sidebar.css`, `/css/notifications.css`, Font Awesome 6.4.
  - Scripts: `/js/sidebar.js` (defer), `/js/notifications.js` (Thymeleaf `th:src`), manter Bootstrap Bundle.

### Máscara e Validação (ES6)
- No `static/js/cnpj-consulta.js`:
  - Função `applyCnpjMask(input)` aplicada no `input` conforme o usuário digita (`99.999.999/9999-99`).
  - Funções utilitárias:
    - `sanitizeCnpj()` (remove não dígitos)
    - `isValidCnpj()` (cálculo dos dois dígitos verificadores conforme pesos oficiais)
  - Antes de `fetch /cadastros/consultar`, validar com `isValidCnpj`; em caso inválido, impedir a requisição e mostrar Toast.
  - Manter Toasts para feedback de erro/sucesso.

- No `static/js/cnpj-processar.js` (não exige máscara), manter apenas padronização de imports/estilo (sem mudança funcional).

## Alterações no Back‑end (Recomendado)
- `CnpjUtils`:
  - Adicionar `isValid(String cnpj)` com algoritmo completo dos dígitos verificadores.
- `ReceitaService.consultarCnpj`:
  - Trocar checagem de tamanho por `isValid(...)` e retornar 400 em controllers quando inválido.

## Testes e Verificação
- Front‑end: validar que o campo não dispara consulta quando CNPJ inválido; verificar máscara e envio com 14 dígitos.
- Back‑end: ajustar teste unitário do `ReceitaService` para uso do novo validador; adicionar teste de falha para CNPJ inválido.

## Entregáveis
- HTML padronizado das páginas de consulta/processamento.
- JS com máscara e validação completa do CNPJ.
- Validação opcional no back‑end (utilitário e uso no service).
- Manter experiência visual consistente com o sistema.

## Observações
- Thymeleaf mantém o padrão de “natural templates”, então a estrutura HTML seguirá fiel ao design e ao render em runtime.
- Sem dependências adicionais (máscara/validação em ES6 puro).