## Causa
- O `@Cacheable("beneficiosAll")` em `BeneficioService.listarTodos()` depende de um cache chamado `beneficiosAll`.
- Em `CacheConfig`, apenas `cnpjCache` e `departamentosAll` estão registrados. Ao não encontrar `beneficiosAll`, o `CacheManager.getCache(...)` retorna `null` e dispara `IllegalArgumentException`.
- Há outros `@Cacheable` (ex.: `estatisticasTermos` em `TermoService`) que também podem falhar se o cache não estiver registrado.

## Solução (preferível: criação dinâmica de caches)
- Alterar `CacheConfig` para usar `CaffeineCacheManager` sem lista fixa de nomes, permitindo criação dinâmica dos caches no primeiro uso.
- Manter o mesmo `builder` (TTL e `maxSize`) aplicado a todos os caches.

## Passos
1. Em `src/main/java/com/jaasielsilva/portalceo/config/CacheConfig.java`:
   - Trocar `new CaffeineCacheManager("cnpjCache","departamentosAll")` por `new CaffeineCacheManager()` e manter `manager.setCaffeine(builder)`.
   - (Opcional) se quiser manter lista fixa, adicionar explicitamente: `"beneficiosAll","estatisticasTermos"`.
2. Validar em runtime:
   - Acessar endpoints que usam `BeneficioService.listarTodos()` e `TermoService.buscarEstatisticas()`; confirmar ausência de exceções e que caches são criados.
3. (Opcional) Configurar nomes no `application.properties`:
   - `spring.cache.cache-names=cnpjCache,departamentosAll,beneficiosAll,estatisticasTermos` (caso queira auditoria dos nomes previstos).

## Considerações
- A criação dinâmica de caches reduz manutenção e evita erros futuros quando novos `@Cacheable` forem adicionados.
- TTL e tamanho máximos continuarão aplicados a todos os caches via `builder`.

Confirma que eu aplique a mudança para criação dinâmica (e opcionalmente incluir os nomes existentes como documentação), e em seguida faça um teste rápido nos pontos que hoje geram o 400 BAD_REQUEST?