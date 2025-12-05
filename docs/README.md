# Documentação do Sistema ERP

Esta pasta contém a documentação técnica e guias de uso para os diversos módulos do sistema ERP.

> Documentação canônica do sistema: consulte `../DOCUMENTACAO_COMPLETA_SISTEMA_UNIFICADA.md` (arquitetura, módulos e endpoints).

## 📋 Índice da Documentação

### 📖 Módulos do Sistema

- **[MODULO_CONFIG.md](./MODULO_CONFIG.md)** - Documentação do módulo de Configuração.
- **[MODULO_CONTROLLER.md](./MODULO_CONTROLLER.md)** - Documentação do módulo de Controladores.
- **[MODULO_DTO.md](./MODULO_DTO.md)** - Documentação do módulo de Objetos de Transferência de Dados (DTOs).
- **[MODULO_ESTOQUE.md](./MODULO_ESTOQUE.md)** - Documentação do módulo de Estoque.
- **[MODULO_EXCEPTION.md](./MODULO_EXCEPTION.md)** - Documentação do módulo de Exceções.
- **[MODULO_FORMATTER.md](./MODULO_FORMATTER.md)** - Documentação do módulo de Formatadores.
- **[MODULO_INDICADORES.md](./MODULO_INDICADORES.md)** - Documentação do módulo de Indicadores.
- **[MODULO_MAPPER.md](./MODULO_MAPPER.md)** - Documentação do módulo de Mapeadores.
- **[MODULO_MARKETING.md](./MODULO_MARKETING.md)** - Documentação do módulo de Marketing.
- **[MODULO_MODEL.md](./MODULO_MODEL.md)** - Documentação do módulo de Modelos de Dados.
- **[MODULO_REPOSITORY.md](./MODULO_REPOSITORY.md)** - Documentação do módulo de Repositórios.
- **[MODULO_SECURITY.md](./MODULO_SECURITY.md)** - Documentação do módulo de Segurança.
- **[MODULO_SERVICE.md](./MODULO_SERVICE.md)** - Documentação do módulo de Serviços.
- **[MODULO_TI.md](./MODULO_TI.md)** - Documentação do módulo de TI.
- **[MODULO_VALIDATION.md](./MODULO_VALIDATION.md)** - Documentação do módulo de Validação.

### 📖 Módulo de Suporte (Exemplo Detalhado)
- **[modulo-suporte.md](./modulo-suporte.md)** - Documentação técnica completa do módulo de suporte.
  - Visão geral e funcionalidades
  - Estrutura técnica (entidades, serviços, controllers)
  - Casos de uso detalhados
  - Fluxograma do sistema
  - APIs REST disponíveis
  - Troubleshooting e comandos úteis

### 🚀 Guias de Uso
- **[guia-rapido-chamados.md](./guia-rapido-chamados.md)** - Guia prático para usuários do módulo de suporte.
  - Como abrir um chamado (web e API)
  - Prioridades e SLA
  - Status dos chamados
  - Dicas e boas práticas
  - Exemplos práticos

## 🎯 Para Quem é Cada Documento

### Desenvolvedores e Administradores
👉 **[modulo-suporte.md](./modulo-suporte.md)**
- Arquitetura do sistema
- Estrutura de código
- APIs e endpoints
- Configuração e manutenção
- Troubleshooting técnico

### Usuários Finais e Suporte
👉 **[guia-rapido-chamados.md](./guia-rapido-chamados.md)**
- Como usar o sistema
- Processo de abertura de chamados
- Acompanhamento de solicitações
- Boas práticas

## 🔗 Links Rápidos

### Sistema em Produção
- **Dashboard**: [http://localhost:8080/suporte](http://localhost:8080/suporte)
- **Novo Chamado**: [http://localhost:8080/suporte/novo](http://localhost:8080/suporte/novo)
- **API REST**: [http://localhost:8080/api/chamados](http://localhost:8080/api/chamados)

### Funcionalidades Principais
- ✅ Abertura de chamados via web e API
- ✅ Sistema automático de SLA
- ✅ Dashboard com estatísticas em tempo real
- ✅ Workflow de status estruturado
- ✅ Numeração automática de chamados
- ✅ Integração com autenticação do sistema
- ✅ **NOVO**: Análise temporal profissional (padrão ERP TOTVS/SAP)

## 📊 Status do Módulo

| Funcionalidade | Status | Versão |
|----------------|--------|---------|
| Criação de Chamados | ✅ Funcionando | 1.0 |
| API REST | ✅ Funcionando | 1.0 |
| Dashboard | ✅ Funcionando | 1.0 |
| Sistema SLA | ✅ Funcionando | 1.0 |
| Workflow Status | ✅ Funcionando | 1.0 |
| Relatórios | ✅ Funcionando | 1.0 |
| **Análise Temporal Profissional** | ✅ **NOVO** | 1.1 |

## 🛠️ Tecnologias Utilizadas

- **Backend**: Spring Boot 3.5.5, Java 21
- **Frontend**: Thymeleaf, Bootstrap 5, JavaScript
- **Banco de Dados**: JPA/Hibernate
- **APIs**: REST com JSON
- **Autenticação**: Spring Security

## 📞 Suporte

Para dúvidas sobre a documentação ou funcionamento do módulo:

- **Equipe de Desenvolvimento**: Consulte o código-fonte
- **Usuários**: Utilize o próprio sistema de chamados
- **Emergências**: Verifique os logs do sistema

---

*Documentação atualizada em: Janeiro 2025*
*Versão do Sistema: 1.1 - Nova implementação de análise temporal profissional*
