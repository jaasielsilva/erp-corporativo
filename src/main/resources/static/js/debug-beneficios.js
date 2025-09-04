/**
 * Script de Teste e Debugging para Benefícios de Adesão
 * Sistema ERP Corporativo - Módulo RH
 * 
 * Este script ajuda a identificar problemas na integração frontend-backend
 */

// Função para testar endpoint de benefícios
function testarEndpointCalcular() {
    console.log('=== TESTANDO ENDPOINT DE CÁLCULO ===');
    
    const sessionId = $('#sessionId').val() || 'teste-session-123';
    
    // Dados de teste simulando seleção real do usuário
    const dadosTeste = {
        sessionId: sessionId,
        beneficios: {
            "PLANO_SAUDE": {
                "planoId": "1", // ou "basico", "intermediario", "premium"
                "dependentes": [
                    {
                        "nome": "Maria Silva",
                        "parentesco": "conjuge", 
                        "dataNascimento": "1990-05-15"
                    }
                ]
            },
            "VALE_REFEICAO": {
                "valor": "300"
            },
            "VALE_TRANSPORTE": {
                "valor": "250"
            }
        }
    };
    
    console.log('Dados enviados:', dadosTeste);
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/calcular',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(dadosTeste),
        beforeSend: function() {
            console.log('Enviando requisição...');
        },
        success: function(response) {
            console.log('✅ SUCESSO - Resposta recebida:', response);
            
            if (response.success) {
                console.log('✅ Cálculo realizado com sucesso!');
                console.log('Total mensal:', response.data.resumo.totalMensal);
                console.log('Itens:', response.data.resumo.itens);
            } else {
                console.warn('⚠️ Resposta indica erro:', response.message);
                if (response.erros) {
                    console.warn('Erros específicos:', response.erros);
                }
            }
        },
        error: function(xhr, status, error) {
            console.error('❌ ERRO na requisição:');
            console.error('Status:', xhr.status);
            console.error('Texto do status:', xhr.statusText);
            console.error('Erro:', error);
            
            if (xhr.responseJSON) {
                console.error('Resposta JSON:', xhr.responseJSON);
            } else {
                console.error('Resposta texto:', xhr.responseText);
            }
        },
        complete: function() {
            console.log('=== FIM DO TESTE ===');
        }
    });
}

// Função para testar benefícios disponíveis
function testarBeneficiosDisponiveis() {
    console.log('=== TESTANDO BENEFÍCIOS DISPONÍVEIS ===');
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/disponiveis',
        method: 'GET',
        success: function(response) {
            console.log('✅ Benefícios disponíveis:', response);
        },
        error: function(xhr, status, error) {
            console.error('❌ Erro ao buscar benefícios:', error);
            console.error('Resposta:', xhr.responseText);
        }
    });
}

// Função para testar endpoint de teste
function testarEndpointTeste() {
    console.log('=== TESTANDO ENDPOINT DE TESTE ===');
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/test',
        method: 'GET',
        success: function(response) {
            console.log('✅ Endpoint de teste funcionando:', response);
        },
        error: function(xhr, status, error) {
            console.error('❌ Endpoint de teste com erro:', error);
        }
    });
}

// Função para monitorar logs do browser
function monitorarLogs() {
    console.log('=== MONITORAMENTO DE LOGS ATIVADO ===');
    
    // Interceptar todos os erros JavaScript
    window.addEventListener('error', function(e) {
        console.error('❌ ERRO JAVASCRIPT DETECTADO:');
        console.error('Mensagem:', e.message);
        console.error('Arquivo:', e.filename);
        console.error('Linha:', e.lineno);
        console.error('Coluna:', e.colno);
        console.error('Erro:', e.error);
    });
    
    // Interceptar erros de promises rejeitadas
    window.addEventListener('unhandledrejection', function(e) {
        console.error('❌ PROMISE REJEITADA NÃO TRATADA:');
        console.error('Motivo:', e.reason);
    });
}

// Função para verificar estado da página
function verificarEstadoPagina() {
    console.log('=== VERIFICANDO ESTADO DA PÁGINA ===');
    
    // Verificar se jQuery está carregado
    if (typeof $ === 'undefined') {
        console.error('❌ jQuery não está carregado!');
        return;
    } else {
        console.log('✅ jQuery carregado (versão:', $.fn.jquery, ')');
    }
    
    // Verificar se sessionId existe
    const sessionId = $('#sessionId').val();
    if (sessionId) {
        console.log('✅ SessionId encontrado:', sessionId);
    } else {
        console.warn('⚠️ SessionId não encontrado no campo #sessionId');
    }
    
    // Verificar se há benefícios selecionados
    const beneficiosSelecionados = $('.benefit-toggle input:checked').length;
    console.log('Benefícios selecionados:', beneficiosSelecionados);
    
    // Verificar se há planos selecionados
    const planosSelecionados = $('.plan-option.selected').length;
    console.log('Planos selecionados:', planosSelecionados);
}

// Função para testar endpoint de debug de planos
function testarDebugPlanos() {
    console.log('=== TESTANDO DEBUG DE PLANOS DE SAÚDE ===');
    
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/debug/planos',
        method: 'GET',
        success: function(response) {
            console.log('✅ Debug de planos bem-sucedido:', response);
            
            if (response.success && response.beneficios) {
                response.beneficios.forEach(beneficio => {
                    console.log(`Benefício: ${beneficio.nome} (${beneficio.tipo})`);
                    if (beneficio.opcoes) {
                        beneficio.opcoes.forEach(opcao => {
                            console.log(`  -> ${opcao.nome} (ID: ${opcao.id}) - R$ ${opcao.valor}`);
                        });
                    }
                });
            }
        },
        error: function(xhr, status, error) {
            console.error('❌ Erro no debug de planos:', error);
            console.error('Resposta:', xhr.responseText);
        }
    });
}

// Função para testar com plano específico que existe
function testarComPlanoExistente() {
    console.log('=== TESTANDO COM PLANO ESPECÍFICO ===');
    
    // Primeiro buscar quais planos existem
    $.ajax({
        url: '/api/rh/colaboradores/adesao/beneficios/debug/planos',
        method: 'GET',
        success: function(response) {
            if (response.success && response.beneficios) {
                const planoSaude = response.beneficios.find(b => b.tipo === 'PLANO_SAUDE');
                if (planoSaude && planoSaude.opcoes && planoSaude.opcoes.length > 0) {
                    const primeiroPlano = planoSaude.opcoes[0];
                    console.log('✅ Encontrou plano para teste:', primeiroPlano.nome, 'ID:', primeiroPlano.id);
                    
                    // Testar com o plano que realmente existe
                    const dadosTeste = {
                        sessionId: $('#sessionId').val() || 'teste-session-123',
                        beneficios: {
                            "PLANO_SAUDE": {
                                "planoId": primeiroPlano.id // Usar ID real do banco
                            }
                        }
                    };
                    
                    console.log('Testando com dados reais:', dadosTeste);
                    
                    $.ajax({
                        url: '/api/rh/colaboradores/adesao/beneficios/calcular',
                        method: 'POST',
                        contentType: 'application/json',
                        data: JSON.stringify(dadosTeste),
                        success: function(calcResponse) {
                            console.log('✅ SUCESSO COM PLANO REAL:', calcResponse);
                        },
                        error: function(xhr) {
                            console.error('❌ AINDA ERRO COM PLANO REAL:', xhr.responseJSON || xhr.responseText);
                        }
                    });
                } else {
                    console.warn('⚠️ Nenhum plano de saúde encontrado');
                }
            }
        },
        error: function(xhr) {
            console.error('❌ Erro ao buscar planos existentes:', xhr.responseText);
        }
    });
}

// Função para executar todos os testes
function executarTodosTestes() {
    console.log('🚀 EXECUTANDO TODOS OS TESTES DE DEBUGGING...');
    
    monitorarLogs();
    verificarEstadoPagina();
    
    setTimeout(() => {
        testarEndpointTeste();
    }, 1000);
    
    setTimeout(() => {
        testarDebugPlanos();
    }, 2000);
    
    setTimeout(() => {
        testarBeneficiosDisponiveis();
    }, 3000);
    
    setTimeout(() => {
        testarComPlanoExistente();
    }, 4000);
    
    setTimeout(() => {
        testarEndpointCalcular();
    }, 5000);
}

// Inicializar debugging quando a página carregar
$(document).ready(function() {
    console.log('🔧 DEBUG SCRIPT CARREGADO - Funções disponíveis:');
    console.log('- executarTodosTestes()');
    console.log('- testarEndpointCalcular()');
    console.log('- testarBeneficiosDisponiveis()');
    console.log('- testarDebugPlanos()');
    console.log('- testarComPlanoExistente()');
    console.log('- verificarEstadoPagina()');
    console.log('Para executar um teste completo, digite: executarTodosTestes()');
});