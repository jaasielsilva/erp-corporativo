-- Script para popular chamados dos últimos 12 meses
-- Baseado na estrutura real da tabela chamados

-- Inserindo chamados de Janeiro 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-001', 'Sistema lento na tela de vendas', 'O sistema está apresentando lentidão significativa na tela de vendas, afetando o atendimento aos clientes', 'SISTEMA', 'Performance', 'ALTA', 'FECHADO', '2024-01-05 09:15:00', '2024-01-05 14:30:00', '2024-01-05 14:30:00', '2024-01-05 17:15:00', 'vendas@empresa.com', 'João Silva', 315, 0, 'Problema resolvido com otimização de consultas SQL', 1),
('2024-002', 'Impressora não funciona', 'Impressora do setor financeiro não está imprimindo relatórios', 'HARDWARE', 'Impressora', 'MEDIA', 'FECHADO', '2024-01-08 10:30:00', '2024-01-08 16:45:00', '2024-01-08 16:45:00', '2024-01-10 10:30:00', 'financeiro@empresa.com', 'Maria Santos', 375, 0, 'Substituído cartucho de tinta e realizada manutenção', NULL),
('2024-003', 'Erro ao gerar relatório mensal', 'Sistema apresenta erro 500 ao tentar gerar relatório mensal de vendas', 'SISTEMA', 'Relatórios', 'URGENTE', 'FECHADO', '2024-01-15 08:00:00', '2024-01-15 10:30:00', '2024-01-15 10:30:00', '2024-01-15 12:00:00', 'gerencia@empresa.com', 'Carlos Lima', 150, 0, 'Corrigido bug na consulta de dados agregados', 1),
('2024-004', 'Acesso negado ao módulo RH', 'Usuário não consegue acessar módulo de recursos humanos', 'ACESSO', 'Permissões', 'ALTA', 'FECHADO', '2024-01-22 14:20:00', '2024-01-22 15:45:00', '2024-01-22 15:45:00', '2024-01-22 18:20:00', 'rh@empresa.com', 'Ana Costa', 85, 0, 'Ajustadas permissões de usuário no sistema', NULL);

-- Inserindo chamados de Fevereiro 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-005', 'Backup falhou na madrugada', 'Processo de backup automático falhou durante a execução noturna', 'SISTEMA', 'Backup', 'URGENTE', 'FECHADO', '2024-02-03 07:30:00', '2024-02-03 09:15:00', '2024-02-03 09:15:00', '2024-02-03 11:30:00', 'ti@empresa.com', 'João Silva', 105, 0, 'Corrigido problema de espaço em disco no servidor', 1),
('2024-006', 'Internet instável no escritório', 'Conexão com internet apresentando instabilidade constante', 'REDE', 'Conectividade', 'ALTA', 'FECHADO', '2024-02-10 11:00:00', '2024-02-10 17:30:00', '2024-02-10 17:30:00', '2024-02-10 15:00:00', 'suporte@empresa.com', 'Maria Santos', 390, 0, 'Substituído roteador principal e reconfigurada rede', NULL),
('2024-007', 'E-mail não está enviando', 'Sistema de e-mail corporativo não está enviando mensagens', 'EMAIL', 'SMTP', 'URGENTE', 'FECHADO', '2024-02-18 13:45:00', '2024-02-18 15:20:00', '2024-02-18 15:20:00', '2024-02-18 17:45:00', 'admin@empresa.com', 'Carlos Lima', 95, 0, 'Reconfigurado servidor SMTP e autenticação', 1),
('2024-008', 'Tela azul no computador da recepção', 'Computador da recepção apresentando tela azul frequentemente', 'HARDWARE', 'Desktop', 'MEDIA', 'FECHADO', '2024-02-25 16:10:00', '2024-02-26 10:30:00', '2024-02-26 10:30:00', '2024-02-27 16:10:00', 'recepcao@empresa.com', 'Ana Costa', 1100, 0, 'Substituída memória RAM defeituosa', NULL);

-- Inserindo chamados de Março 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-009', 'Sistema de ponto eletrônico offline', 'Sistema de controle de ponto não está registrando entradas e saídas', 'SISTEMA', 'Ponto Eletrônico', 'ALTA', 'FECHADO', '2024-03-05 08:30:00', '2024-03-05 12:15:00', '2024-03-05 12:15:00', '2024-03-05 12:30:00', 'rh@empresa.com', 'João Silva', 225, 0, 'Reiniciado serviço e atualizado driver do leitor biométrico', 1),
('2024-010', 'Vírus detectado em estação de trabalho', 'Antivírus detectou malware em computador do setor comercial', 'SEGURANCA', 'Malware', 'URGENTE', 'FECHADO', '2024-03-12 14:20:00', '2024-03-12 16:45:00', '2024-03-12 16:45:00', '2024-03-12 18:20:00', 'comercial@empresa.com', 'Maria Santos', 145, 0, 'Removido malware e atualizado definições de antivírus', NULL),
('2024-011', 'Erro na integração com banco', 'Sistema ERP não está sincronizando com sistema bancário', 'INTEGRACAO', 'API Bancária', 'ALTA', 'FECHADO', '2024-03-19 10:00:00', '2024-03-19 15:30:00', '2024-03-19 15:30:00', '2024-03-19 14:00:00', 'financeiro@empresa.com', 'Carlos Lima', 330, 0, 'Atualizado certificado SSL da API bancária', 1),
('2024-012', 'Solicitação de novo usuário', 'Criação de acesso para novo funcionário do departamento de marketing', 'ACESSO', 'Novo Usuário', 'BAIXA', 'FECHADO', '2024-03-26 09:45:00', '2024-03-26 11:20:00', '2024-03-26 11:20:00', '2024-03-28 09:45:00', 'marketing@empresa.com', 'Ana Costa', 95, 0, 'Criado usuário com perfil adequado às funções', NULL);

-- Inserindo chamados de Abril 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-013', 'Atualização do sistema operacional', 'Necessário atualizar Windows em todas as estações de trabalho', 'SISTEMA', 'Atualização', 'MEDIA', 'FECHADO', '2024-04-02 08:00:00', '2024-04-05 17:00:00', '2024-04-05 17:00:00', '2024-04-09 08:00:00', 'ti@empresa.com', 'João Silva', 4860, 0, 'Atualização realizada em lotes durante horários de menor uso', 1),
('2024-014', 'Problema na videoconferência', 'Sistema de videoconferência apresentando falhas de áudio e vídeo', 'COMUNICACAO', 'Videoconferência', 'ALTA', 'FECHADO', '2024-04-09 13:30:00', '2024-04-09 15:45:00', '2024-04-09 15:45:00', '2024-04-09 17:30:00', 'diretoria@empresa.com', 'Maria Santos', 135, 0, 'Atualizado software e reconfigurado codec de vídeo', NULL),
('2024-015', 'Lentidão na rede interna', 'Transferência de arquivos na rede local muito lenta', 'REDE', 'Performance', 'MEDIA', 'FECHADO', '2024-04-16 11:15:00', '2024-04-16 16:30:00', '2024-04-16 16:30:00', '2024-04-18 11:15:00', 'todos@empresa.com', 'Carlos Lima', 315, 0, 'Otimizado switch principal e reorganizado VLANs', 1),
('2024-016', 'Erro ao salvar documentos', 'Usuários não conseguem salvar documentos no servidor de arquivos', 'SISTEMA', 'Servidor de Arquivos', 'ALTA', 'FECHADO', '2024-04-23 15:20:00', '2024-04-23 17:10:00', '2024-04-23 17:10:00', '2024-04-23 19:20:00', 'geral@empresa.com', 'Ana Costa', 110, 0, 'Corrigido problema de permissões no servidor de arquivos', NULL);

-- Inserindo chamados de Maio 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-017', 'Instalação de novo software', 'Instalação do novo sistema de gestão de projetos', 'SOFTWARE', 'Instalação', 'BAIXA', 'FECHADO', '2024-05-07 10:00:00', '2024-05-07 14:30:00', '2024-05-07 14:30:00', '2024-05-10 10:00:00', 'projetos@empresa.com', 'João Silva', 270, 0, 'Software instalado e configurado com treinamento básico', 1),
('2024-018', 'Falha no sistema de backup', 'Backup incremental não está funcionando corretamente', 'SISTEMA', 'Backup', 'URGENTE', 'FECHADO', '2024-05-14 06:45:00', '2024-05-14 08:30:00', '2024-05-14 08:30:00', '2024-05-14 10:45:00', 'ti@empresa.com', 'Maria Santos', 105, 0, 'Reconfigurado agendamento e verificado integridade dos dados', NULL),
('2024-019', 'Monitor com defeito', 'Monitor do designer apresentando linhas na tela', 'HARDWARE', 'Monitor', 'MEDIA', 'FECHADO', '2024-05-21 09:30:00', '2024-05-21 11:45:00', '2024-05-21 11:45:00', '2024-05-23 09:30:00', 'design@empresa.com', 'Carlos Lima', 135, 0, 'Substituído monitor defeituoso por novo equipamento', 1),
('2024-020', 'Problema no sistema de telefonia', 'Ramais internos não estão funcionando adequadamente', 'COMUNICACAO', 'Telefonia', 'ALTA', 'FECHADO', '2024-05-28 14:15:00', '2024-05-28 17:20:00', '2024-05-28 17:20:00', '2024-05-28 18:15:00', 'recepcao@empresa.com', 'Ana Costa', 185, 0, 'Reiniciado PABX e reconfigurado ramais', NULL);

-- Inserindo chamados de Junho 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-021', 'Migração de dados', 'Migração de dados do sistema antigo para o novo ERP', 'SISTEMA', 'Migração', 'ALTA', 'FECHADO', '2024-06-04 08:00:00', '2024-06-06 18:00:00', '2024-06-06 18:00:00', '2024-06-11 08:00:00', 'ti@empresa.com', 'João Silva', 3480, 0, 'Migração concluída com sucesso, dados validados', 1),
('2024-022', 'Configuração de VPN', 'Configuração de acesso VPN para trabalho remoto', 'REDE', 'VPN', 'MEDIA', 'FECHADO', '2024-06-11 13:20:00', '2024-06-11 16:45:00', '2024-06-11 16:45:00', '2024-06-13 13:20:00', 'rh@empresa.com', 'Maria Santos', 205, 0, 'VPN configurada com autenticação de dois fatores', NULL),
('2024-023', 'Erro na folha de pagamento', 'Sistema de folha apresentando cálculos incorretos', 'SISTEMA', 'Folha de Pagamento', 'URGENTE', 'FECHADO', '2024-06-18 07:30:00', '2024-06-18 11:15:00', '2024-06-18 11:15:00', '2024-06-18 11:30:00', 'rh@empresa.com', 'Carlos Lima', 225, 0, 'Corrigido algoritmo de cálculo de horas extras', 1),
('2024-024', 'Solicitação de equipamento', 'Solicitação de notebook para novo funcionário', 'HARDWARE', 'Notebook', 'BAIXA', 'FECHADO', '2024-06-25 10:45:00', '2024-06-27 15:30:00', '2024-06-27 15:30:00', '2024-06-30 10:45:00', 'compras@empresa.com', 'Ana Costa', 3165, 0, 'Notebook adquirido, configurado e entregue', NULL);

-- Inserindo chamados de Julho 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-025', 'Atualização de antivírus', 'Atualização das definições de antivírus em todas as estações', 'SEGURANCA', 'Antivírus', 'MEDIA', 'FECHADO', '2024-07-02 09:00:00', '2024-07-02 12:30:00', '2024-07-02 12:30:00', '2024-07-04 09:00:00', 'ti@empresa.com', 'João Silva', 210, 0, 'Antivírus atualizado e scan completo realizado', 1),
('2024-026', 'Problema na impressão de etiquetas', 'Impressora de etiquetas não está imprimindo corretamente', 'HARDWARE', 'Impressora', 'MEDIA', 'FECHADO', '2024-07-09 14:30:00', '2024-07-09 16:15:00', '2024-07-09 16:15:00', '2024-07-11 14:30:00', 'expedicao@empresa.com', 'Maria Santos', 105, 0, 'Calibrada impressora e substituído rolo de etiquetas', NULL),
('2024-027', 'Sistema de estoque com erro', 'Erro ao dar baixa no estoque de produtos', 'SISTEMA', 'Estoque', 'ALTA', 'FECHADO', '2024-07-16 11:20:00', '2024-07-16 14:45:00', '2024-07-16 14:45:00', '2024-07-16 15:20:00', 'estoque@empresa.com', 'Carlos Lima', 205, 0, 'Corrigido trigger de atualização de estoque', 1),
('2024-028', 'Configuração de e-mail', 'Configuração de e-mail corporativo para novo funcionário', 'EMAIL', 'Configuração', 'BAIXA', 'FECHADO', '2024-07-23 08:45:00', '2024-07-23 10:20:00', '2024-07-23 10:20:00', '2024-07-25 08:45:00', 'novofuncionario@empresa.com', 'Ana Costa', 95, 0, 'E-mail configurado com sincronização móvel', NULL);

-- Inserindo chamados de Agosto 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-029', 'Falha no ar condicionado do servidor', 'Ar condicionado da sala de servidores apresentando falha', 'INFRAESTRUTURA', 'Climatização', 'URGENTE', 'FECHADO', '2024-08-06 15:30:00', '2024-08-06 18:45:00', '2024-08-06 18:45:00', '2024-08-06 19:30:00', 'ti@empresa.com', 'João Silva', 195, 0, 'Técnico especializado chamado, equipamento reparado', 1),
('2024-030', 'Erro na sincronização mobile', 'Aplicativo móvel não está sincronizando com servidor', 'SISTEMA', 'Mobile', 'ALTA', 'FECHADO', '2024-08-13 12:15:00', '2024-08-13 15:30:00', '2024-08-13 15:30:00', '2024-08-13 16:15:00', 'vendas@empresa.com', 'Maria Santos', 195, 0, 'Atualizado API e reconfigurado sincronização', NULL),
('2024-031', 'Problema no sistema de ponto', 'Funcionários não conseguem bater ponto', 'SISTEMA', 'Ponto Eletrônico', 'ALTA', 'FECHADO', '2024-08-20 07:45:00', '2024-08-20 09:30:00', '2024-08-20 09:30:00', '2024-08-20 11:45:00', 'rh@empresa.com', 'Carlos Lima', 105, 0, 'Reiniciado sistema e verificado conexão com banco', 1),
('2024-032', 'Instalação de câmeras', 'Instalação de sistema de monitoramento por câmeras', 'SEGURANCA', 'CFTV', 'MEDIA', 'FECHADO', '2024-08-27 08:00:00', '2024-08-29 17:00:00', '2024-08-29 17:00:00', '2024-09-03 08:00:00', 'seguranca@empresa.com', 'Ana Costa', 3420, 0, 'Sistema de CFTV instalado e configurado', NULL);

-- Inserindo chamados de Setembro 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-033', 'Upgrade de memória RAM', 'Upgrade de memória em estações de trabalho do design', 'HARDWARE', 'Upgrade', 'MEDIA', 'FECHADO', '2024-09-03 10:30:00', '2024-09-03 15:45:00', '2024-09-03 15:45:00', '2024-09-05 10:30:00', 'design@empresa.com', 'João Silva', 315, 0, 'Memória RAM expandida de 8GB para 16GB', 1),
('2024-034', 'Erro no relatório fiscal', 'Sistema não está gerando relatório fiscal corretamente', 'SISTEMA', 'Fiscal', 'URGENTE', 'FECHADO', '2024-09-10 13:20:00', '2024-09-10 16:45:00', '2024-09-10 16:45:00', '2024-09-10 17:20:00', 'contabilidade@empresa.com', 'Maria Santos', 205, 0, 'Corrigido cálculo de impostos no relatório', NULL),
('2024-035', 'Configuração de firewall', 'Configuração de novas regras de firewall', 'SEGURANCA', 'Firewall', 'ALTA', 'FECHADO', '2024-09-17 09:15:00', '2024-09-17 12:30:00', '2024-09-17 12:30:00', '2024-09-17 13:15:00', 'ti@empresa.com', 'Carlos Lima', 195, 0, 'Regras de firewall atualizadas conforme política de segurança', 1),
('2024-036', 'Problema na rede Wi-Fi', 'Rede Wi-Fi instável em algumas áreas do escritório', 'REDE', 'Wi-Fi', 'MEDIA', 'FECHADO', '2024-09-24 14:45:00', '2024-09-24 17:20:00', '2024-09-24 17:20:00', '2024-09-26 14:45:00', 'geral@empresa.com', 'Ana Costa', 155, 0, 'Reposicionado access points e otimizado canais', NULL);

-- Inserindo chamados de Outubro 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-037', 'Backup de dados críticos', 'Implementação de backup adicional para dados críticos', 'SISTEMA', 'Backup', 'ALTA', 'FECHADO', '2024-10-01 08:30:00', '2024-10-02 16:00:00', '2024-10-02 16:00:00', '2024-10-03 08:30:00', 'ti@empresa.com', 'João Silva', 1890, 0, 'Backup em nuvem configurado para dados críticos', 1),
('2024-038', 'Erro na integração contábil', 'Sistema ERP não está integrando com software contábil', 'INTEGRACAO', 'Contábil', 'ALTA', 'FECHADO', '2024-10-08 11:45:00', '2024-10-08 15:20:00', '2024-10-08 15:20:00', '2024-10-08 15:45:00', 'contabilidade@empresa.com', 'Maria Santos', 215, 0, 'Atualizado conector e reconfigurado mapeamento de contas', NULL),
('2024-039', 'Manutenção preventiva servidores', 'Manutenção preventiva programada nos servidores', 'INFRAESTRUTURA', 'Manutenção', 'MEDIA', 'FECHADO', '2024-10-15 19:00:00', '2024-10-15 23:30:00', '2024-10-15 23:30:00', '2024-10-16 07:00:00', 'ti@empresa.com', 'Carlos Lima', 270, 0, 'Manutenção realizada com sucesso, sistemas testados', 1),
('2024-040', 'Problema no scanner', 'Scanner do departamento jurídico não está funcionando', 'HARDWARE', 'Scanner', 'BAIXA', 'FECHADO', '2024-10-22 13:30:00', '2024-10-22 15:45:00', '2024-10-22 15:45:00', '2024-10-24 13:30:00', 'juridico@empresa.com', 'Ana Costa', 135, 0, 'Driver atualizado e configuração ajustada', NULL);

-- Inserindo chamados de Novembro 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-041', 'Atualização sistema operacional servidor', 'Atualização crítica de segurança no servidor principal', 'SISTEMA', 'Atualização', 'URGENTE', 'FECHADO', '2024-11-05 20:00:00', '2024-11-05 22:30:00', '2024-11-05 22:30:00', '2024-11-06 08:00:00', 'ti@empresa.com', 'João Silva', 150, 0, 'Atualização aplicada com sucesso durante janela de manutenção', 1),
('2024-042', 'Erro no módulo financeiro', 'Módulo financeiro apresentando erro ao calcular juros', 'SISTEMA', 'Financeiro', 'ALTA', 'FECHADO', '2024-11-12 09:20:00', '2024-11-12 13:45:00', '2024-11-12 13:45:00', '2024-11-12 13:20:00', 'financeiro@empresa.com', 'Maria Santos', 265, 0, 'Corrigido algoritmo de cálculo de juros compostos', NULL),
('2024-043', 'Configuração de proxy', 'Configuração de servidor proxy para controle de acesso', 'REDE', 'Proxy', 'MEDIA', 'FECHADO', '2024-11-19 10:15:00', '2024-11-19 14:30:00', '2024-11-19 14:30:00', '2024-11-21 10:15:00', 'ti@empresa.com', 'Carlos Lima', 255, 0, 'Proxy configurado com filtros de conteúdo e logs', 1),
('2024-044', 'Problema na videoconferência', 'Sala de reunião com problemas de áudio na videoconferência', 'COMUNICACAO', 'Videoconferência', 'ALTA', 'FECHADO', '2024-11-26 15:30:00', '2024-11-26 17:15:00', '2024-11-26 17:15:00', '2024-11-26 19:30:00', 'reunioes@empresa.com', 'Ana Costa', 105, 0, 'Microfone substituído e sistema de áudio calibrado', NULL);

-- Inserindo chamados de Dezembro 2024
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, data_fechamento, data_resolucao, sla_vencimento, solicitante_email, tecnico_responsavel, tempo_resolucao_minutos, foi_reaberto, observacoes, colaborador_id) VALUES
('2024-045', 'Backup de fim de ano', 'Backup completo de todos os sistemas para fechamento anual', 'SISTEMA', 'Backup', 'ALTA', 'FECHADO', '2024-12-03 18:00:00', '2024-12-04 06:00:00', '2024-12-04 06:00:00', '2024-12-05 18:00:00', 'ti@empresa.com', 'João Silva', 720, 0, 'Backup completo realizado e verificado', 1),
('2024-046', 'Erro no fechamento mensal', 'Sistema apresenta erro ao processar fechamento de novembro', 'SISTEMA', 'Fechamento', 'URGENTE', 'FECHADO', '2024-12-10 08:15:00', '2024-12-10 11:30:00', '2024-12-10 11:30:00', '2024-12-10 12:15:00', 'contabilidade@empresa.com', 'Maria Santos', 195, 0, 'Corrigido processo de fechamento contábil', NULL),
('2024-047', 'Instalação de certificado SSL', 'Renovação e instalação de certificado SSL do site', 'SEGURANCA', 'SSL', 'MEDIA', 'FECHADO', '2024-12-17 14:20:00', '2024-12-17 16:45:00', '2024-12-17 16:45:00', '2024-12-19 14:20:00', 'web@empresa.com', 'Carlos Lima', 145, 0, 'Certificado SSL renovado e instalado com sucesso', 1);

-- Inserindo chamados de Janeiro 2025 (em andamento e abertos)
INSERT INTO chamados (numero, assunto, descricao, categoria, subcategoria, prioridade, status, data_abertura, sla_vencimento, solicitante_email, tecnico_responsavel, colaborador_id) VALUES
('2025-001', 'Migração para novo servidor', 'Planejamento e execução da migração para novo servidor', 'INFRAESTRUTURA', 'Migração', 'ALTA', 'EM_ANDAMENTO', '2025-01-08 09:00:00', '2025-01-15 09:00:00', 'ti@empresa.com', 'João Silva', 1),
('2025-002', 'Implementação de sistema de tickets', 'Implementação de novo sistema de gestão de tickets', 'SISTEMA', 'Implementação', 'MEDIA', 'ABERTO', '2025-01-10 10:30:00', '2025-01-17 10:30:00', 'suporte@empresa.com', 'Maria Santos', NULL),
('2025-003', 'Auditoria de segurança', 'Auditoria completa de segurança da infraestrutura', 'SEGURANCA', 'Auditoria', 'ALTA', 'ABERTO', '2025-01-12 08:00:00', '2025-01-19 08:00:00', 'ti@empresa.com', 'Carlos Lima', 1);

-- Consultas para verificar os dados inseridos
SELECT 'Total de chamados inseridos:' as info, COUNT(*) as quantidade FROM chamados;

SELECT 'Distribuição por mês:' as info;
SELECT 
    DATE_FORMAT(data_abertura, '%Y-%m') as mes,
    COUNT(*) as quantidade
FROM chamados 
WHERE data_abertura >= '2024-01-01'
ORDER BY mes;

SELECT 'Distribuição por status:' as info;
SELECT status, COUNT(*) as quantidade FROM chamados GROUP BY status;

SELECT 'Distribuição por prioridade:' as info;
SELECT prioridade, COUNT(*) as quantidade FROM chamados GROUP BY prioridade;

SELECT 'Distribuição por categoria:' as info;
SELECT categoria, COUNT(*) as quantidade FROM chamados GROUP BY categoria ORDER BY quantidade DESC;

SELECT 'Chamados em aberto ou em andamento:' as info;
SELECT numero, assunto, status, prioridade, data_abertura 
FROM chamados 
WHERE status IN ('ABERTO', 'EM_ANDAMENTO') 
ORDER BY data_abertura DESC;