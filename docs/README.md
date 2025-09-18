# Documentação - Módulo de Suporte

Esta pasta contém toda a documentação técnica e guias de uso do módulo de suporte do sistema ERP.

## 📋 Índice da Documentação

### 📖 Documentação Técnica
- **[modulo-suporte.md](./modulo-suporte.md)** - Documentação técnica completa
  - Visão geral e funcionalidades
  - Estrutura técnica (entidades, serviços, controllers)
  - Casos de uso detalhados
  - Fluxograma do sistema
  - APIs REST disponíveis
  - Troubleshooting e comandos úteis

### 🚀 Guias de Uso
- **[guia-rapido-chamados.md](./guia-rapido-chamados.md)** - Guia prático para usuários
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

- **Backend**: Spring Boot 3.x, Java 17
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