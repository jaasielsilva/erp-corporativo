## Problema
- Erros na página: "VueDemi is not defined" e "Cannot destructure property 'createPinia' of 'Pinia' as it is undefined" indicam que o bundle IIFE da Pinia não está carregando corretamente (ordem/arquivo CDN incompatível).

## Correções Propostas (sem bundler, via CDN)
1) **Ajustar ordem e fontes dos scripts** em `templates/chat/index.html`:
   - Carregar nesta ordem:
     - Vue: `<script src="https://unpkg.com/vue@3/dist/vue.global.prod.js"></script>`
     - Pinia (IIFE): `<script src="https://unpkg.com/pinia@2.1.7/dist/pinia.iife.js"></script>`
     - SockJS/STOMP: já presentes
     - `chat-app.js`
   - Remover a referência atual a `pinia.iife.prod.js` (que depende de VueDemi em alguns CDNs) ou, alternativamente, **adicionar VueDemi** antes da Pinia: `<script src="https://unpkg.com/vue-demi@0.14.6"></script>`.

2) **Garantir acesso a Pinia global** no `chat-app.js`:
   - Usar `const { createPinia, defineStore } = window.Pinia;` para evitar conflitos de escopo.
   - Executar somente após garantir `window.Vue` e `window.Pinia` definidos.

3) **Delay de inicialização** (se necessário):
   - Envolver a montagem em `window.addEventListener('load', ...)` para garantir que os scripts CDN foram carregados antes do uso.

4) **Verificação rápida**:
   - Abrir `/chat` e confirmar no console:
     - `window.Vue` e `window.Pinia` existem;
     - `createPinia` e `defineStore` disponíveis;
     - Sem erros "VueDemi".

5) **Opcional (melhor prática)**:
   - Migrar para build com Vite (bundler) e servir bundle único; elimina dependências de CDN e problemas de ordem.

## Entregáveis
- Atualização de `index.html` com CDNs corretos e ordem garantida.
- Ajuste de `chat-app.js` para usar `window.Pinia` e inicializar após scripts.
- Teste manual da página `/chat` sem erros de console.

Posso aplicar essas mudanças agora para estabilizar a página `/chat`?