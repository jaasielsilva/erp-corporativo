/**
 * Debug específico para o erro 500 no resumo de benefícios
 * SessionId: f1554e30-66a4-4038-ae87-67ddb910220f
 */

// Teste o sessionId específico que está falhando
function debugResumoBeneficios() {
    const sessionId = 'f1554e30-66a4-4038-ae87-67ddb910220f';
    
    console.log('🔍 Debug específico para erro 500 no resumo de benefícios...');
    console.log(`SessionId: ${sessionId}`);
    
    // Teste 1: Verificar se a sessão existe
    console.log('\n1. Verificando se a sessão existe...');
    $.ajax({
        url: `/api/rh/colaboradores/adesao/beneficios/sessao?sessionId=${sessionId}`,
        method: 'GET',
        success: function(response) {
            console.log('✅ Sessão existe no serviço de benefícios:', response.success);
            
            // Teste 2: Tentar o endpoint de resumo (que estava dando 500)
            console.log('\n2. Testando endpoint de resumo (estava dando 500)...');
            $.ajax({
                url: `/api/rh/colaboradores/adesao/beneficios/resumo?sessionId=${sessionId}`,
                method: 'GET',
                success: function(resumoResponse) {
                    console.log('✅ Endpoint de resumo funcionando:', resumoResponse);
                    
                    if (resumoResponse.success) {
                        console.log(`  - Tem benefícios: ${resumoResponse.temBeneficios}`);
                        if (resumoResponse.temBeneficios) {
                            console.log(`  - Quantidade de itens: ${resumoResponse.resumo.quantidadeItens}`);
                            console.log(`  - Total mensal: R$ ${resumoResponse.resumo.totalMensal}`);
                        }
                    }
                },
                error: function(xhr) {
                    console.error('❌ Endpoint de resumo falhou:', {
                        status: xhr.status,
                        statusText: xhr.statusText,
                        response: xhr.responseJSON,
                        responseText: xhr.responseText
                    });
                    
                    // Se é um erro 500, significa que nosso endpoint está sendo chamado
                    // mas há uma exceção no processamento
                    if (xhr.status === 500) {
                        console.log('\n🔧 Detalhes do Erro 500:');
                        console.log('Isso indica uma exceção do lado do servidor no endpoint /resumo.');
                        console.log('Verifique os logs do servidor para o stack trace detalhado.');
                        console.log('Causas comuns:');
                        console.log('- NullPointerException no cálculo de benefícios');
                        console.log('- Dados de sessão ausentes ou dados de benefícios corrompidos');
                        console.log('- Problemas de conexão com banco de dados');
                        console.log('- Dependências ou serviços ausentes');
                        
                        // Tentar o endpoint de debug do servidor
                        console.log('\n3. Tentando endpoint de debug do servidor...');
                        $.ajax({
                            url: `/rh/colaboradores/adesao/debug/session/${sessionId}`,
                            method: 'GET',
                            success: function(debugResponse) {
                                console.log('✅ Debug do servidor:', debugResponse);
                            },
                            error: function(debugXhr) {
                                console.log('❌ Debug do servidor falhou:', debugXhr.status);
                            }
                        });
                    }
                }
            });
        },
        error: function(xhr) {
            console.error('❌ Sessão não existe ou erro no serviço de benefícios:', {
                status: xhr.status,
                response: xhr.responseJSON
            });
            
            if (xhr.status === 404) {
                console.log('ℹ️  Sessão não encontrada. Esta sessão pode ter expirado ou nunca existiu.');
                console.log('   Tente criar uma nova sessão iniciando o processo de adesão novamente.');
            }
        }
    });
}

// Teste direto do endpoint que estava falhando
function testarResumoDirecto() {
    const sessionId = 'f1554e30-66a4-4038-ae87-67ddb910220f';
    
    console.log('🎯 Teste direto do endpoint de resumo...');
    
    $.ajax({
        url: `/api/rh/colaboradores/adesao/beneficios/resumo?sessionId=${sessionId}`,
        method: 'GET',
        beforeSend: function() {
            console.log('📤 Enviando requisição para resumo...');
        },
        success: function(response) {
            console.log('✅ SUCESSO - Resumo funcionando:', response);
        },
        error: function(xhr, status, error) {
            console.error('❌ ERRO - Resumo falhou:', {
                status: xhr.status,
                statusText: xhr.statusText,
                responseJSON: xhr.responseJSON,
                responseText: xhr.responseText,
                error: error
            });
            
            // Tentar analisar o erro
            if (xhr.responseText) {
                try {
                    const errorData = JSON.parse(xhr.responseText);
                    console.log('📋 Dados do erro parsados:', errorData);
                } catch(e) {
                    console.log('📋 Resposta de erro (texto bruto):', xhr.responseText);
                }
            }
        }
    });
}

// Verificar o sessionId atual da página
function obterSessionIdAtual() {
    const urlParams = new URLSearchParams(window.location.search);
    const sessionId = urlParams.get('sessionId');
    
    if (sessionId) {
        console.log(`SessionId da página atual: ${sessionId}`);
        return sessionId;
    } else {
        // Tentar extrair do caminho
        const pathParts = window.location.pathname.split('/');
        const lastPart = pathParts[pathParts.length - 1];
        
        if (lastPart && lastPart.length > 10) { // Provavelmente um session ID
            console.log(`SessionId do caminho: ${lastPart}`);
            return lastPart;
        }
    }
    
    console.log('Nenhum sessionId encontrado na URL');
    return null;
}

// Exportar funções
window.debugResumoBeneficios = debugResumoBeneficios;
window.testarResumoDirecto = testarResumoDirecto;
window.obterSessionIdAtual = obterSessionIdAtual;

console.log('🛠️  Ferramentas de Debug para Erro 500 Carregadas');
console.log('Comandos:');
console.log('  debugResumoBeneficios() - Debug completo do erro de resumo');
console.log('  testarResumoDirecto() - Teste direto do endpoint que estava falhando');
console.log('  obterSessionIdAtual() - Obter session ID da página atual');

// Auto-execução se estivermos na página de revisão
if (window.location.pathname.includes('/revisao')) {
    const currentSessionId = obterSessionIdAtual();
    if (currentSessionId) {
        console.log(`\n🚀 Auto-executando debug para sessão atual: ${currentSessionId}`);
        setTimeout(() => {
            // Atualizar o sessionId do teste para o atual
            window.debugResumoBeneficiosAtual = function() {
                const sessionId = currentSessionId;
                
                console.log('🔍 Debug para sessão atual...');
                $.ajax({
                    url: `/api/rh/colaboradores/adesao/beneficios/resumo?sessionId=${sessionId}`,
                    method: 'GET',
                    success: function(response) {
                        console.log('✅ AUTO-TESTE SUCESSO:', response);
                    },
                    error: function(xhr) {
                        console.error('❌ AUTO-TESTE FALHOU:', {
                            status: xhr.status,
                            statusText: xhr.statusText,
                            responseText: xhr.responseText
                        });
                    }
                });
            };
            
            // Executar o teste
            window.debugResumoBeneficiosAtual();
        }, 1000);
    }
}