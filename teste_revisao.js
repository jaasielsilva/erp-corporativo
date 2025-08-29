// Script de Teste para Página de Revisão - Adesão de Colaboradores
// Este script simula a revisão final e finalização do processo de adesão

console.log('=== INICIANDO TESTE DA PÁGINA DE REVISÃO ===');

// Função principal para executar o teste da revisão
function executarTesteRevisao() {
    console.log('\n1. Verificando se estamos na página de revisão...');
    
    // Verificar se estamos na página correta
    if (!window.location.pathname.includes('/revisao')) {
        console.error('❌ Não estamos na página de revisão!');
        console.log('📍 Navegue para: /rh/colaboradores/adesao/revisao?sessionId=SEU_SESSION_ID');
        return;
    }
    
    console.log('✅ Página de revisão carregada');
    
    // Aguardar carregamento dos dados
    setTimeout(() => {
        console.log('\n2. Verificando dados carregados...');
        verificarDadosCarregados();
        
        console.log('\n3. Simulando aceitação dos termos...');
        aceitarTermos();
        
        console.log('\n4. Preparando para finalização...');
        prepararFinalizacao();
        
    }, 2000);
}

// Função para verificar se os dados foram carregados
function verificarDadosCarregados() {
    const dadosPessoais = $('#dadosPessoaisRevisao').length;
    const documentos = $('#documentosEnviados').length;
    const beneficios = $('#beneficiosSelecionados').length;
    
    console.log(`📋 Seções encontradas:`);
    console.log(`   - Dados Pessoais: ${dadosPessoais ? '✅' : '❌'}`);
    console.log(`   - Documentos: ${documentos ? '✅' : '❌'}`);
    console.log(`   - Benefícios: ${beneficios ? '✅' : '❌'}`);
    
    // Verificar se há dados carregados
    if ($('#dadosPessoaisRevisao .info-row').length > 0) {
        console.log('✅ Dados pessoais carregados');
    } else {
        console.log('⚠️ Dados pessoais não encontrados');
    }
    
    if ($('#documentosEnviados .document-item').length > 0) {
        console.log('✅ Documentos carregados');
    } else {
        console.log('⚠️ Documentos não encontrados');
    }
}

// Função para aceitar todos os termos
function aceitarTermos() {
    console.log('📝 Aceitando termos e condições...');
    
    // Aceitar termos e condições
    if ($('#aceitarTermos').length) {
        $('#aceitarTermos').prop('checked', true);
        console.log('✅ Termos e condições aceitos');
    } else {
        console.log('❌ Checkbox de termos não encontrado');
    }
    
    // Autorizar desconto em folha
    if ($('#autorizarDesconto').length) {
        $('#autorizarDesconto').prop('checked', true);
        console.log('✅ Desconto em folha autorizado');
    } else {
        console.log('❌ Checkbox de desconto não encontrado');
    }
    
    // Confirmar veracidade dos dados
    if ($('#confirmarDados').length) {
        $('#confirmarDados').prop('checked', true);
        console.log('✅ Veracidade dos dados confirmada');
    } else {
        console.log('❌ Checkbox de confirmação não encontrado');
    }
    
    // Disparar evento de mudança para habilitar botão
    $('#aceitarTermos, #autorizarDesconto, #confirmarDados').trigger('change');
    
    // Verificar se o botão foi habilitado
    setTimeout(() => {
        const botaoHabilitado = !$('#btnFinalizar').prop('disabled');
        console.log(`🔘 Botão finalizar: ${botaoHabilitado ? 'HABILITADO ✅' : 'DESABILITADO ❌'}`);
    }, 500);
}

// Função para preparar a finalização
function prepararFinalizacao() {
    console.log('\n🎯 PRONTO PARA FINALIZAR!');
    console.log('\n📋 Resumo do que foi feito:');
    console.log('   ✅ Dados pessoais revisados');
    console.log('   ✅ Documentos verificados');
    console.log('   ✅ Benefícios confirmados');
    console.log('   ✅ Termos aceitos');
    console.log('   ✅ Desconto autorizado');
    console.log('   ✅ Dados confirmados');
    
    console.log('\n🚀 Para finalizar o processo, execute:');
    console.log('   finalizarProcessoAdesao()');
}

// Função para finalizar o processo (chamada manual)
function finalizarProcessoAdesao() {
    console.log('\n🔄 FINALIZANDO PROCESSO DE ADESÃO...');
    
    // Verificar se todos os termos foram aceitos
    const termosAceitos = $('#aceitarTermos').is(':checked');
    const descontoAutorizado = $('#autorizarDesconto').is(':checked');
    const dadosConfirmados = $('#confirmarDados').is(':checked');
    
    if (!termosAceitos || !descontoAutorizado || !dadosConfirmados) {
        console.error('❌ Nem todos os termos foram aceitos!');
        console.log('🔄 Executando aceitação automática...');
        aceitarTermos();
        
        setTimeout(() => {
            finalizarProcessoAdesao();
        }, 1000);
        return;
    }
    
    console.log('✅ Todos os termos aceitos, prosseguindo...');
    
    // Simular clique no botão finalizar
    if ($('#btnFinalizar').length && !$('#btnFinalizar').prop('disabled')) {
        console.log('🎯 Clicando no botão finalizar...');
        $('#btnFinalizar').click();
        
        // Monitorar o processo
        setTimeout(() => {
            monitorarFinalizacao();
        }, 1000);
    } else {
        console.error('❌ Botão finalizar não disponível ou desabilitado');
    }
}

// Função para monitorar a finalização
function monitorarFinalizacao() {
    console.log('\n👀 Monitorando finalização...');
    
    // Verificar se o loading está visível
    if ($('#loadingOverlay').is(':visible')) {
        console.log('⏳ Loading ativo - processando...');
        
        // Continuar monitorando
        setTimeout(() => {
            monitorarFinalizacao();
        }, 2000);
    } else {
        // Verificar se o modal de sucesso apareceu
        if ($('#successModal').hasClass('show')) {
            console.log('🎉 SUCESSO! Modal de finalização exibido!');
            
            const protocolo = $('#protocoloAdesao').text();
            if (protocolo) {
                console.log(`📋 Protocolo gerado: ${protocolo}`);
            }
            
            console.log('\n✅ PROCESSO DE ADESÃO FINALIZADO COM SUCESSO!');
            console.log('\n📧 Você receberá um e-mail com o status da solicitação.');
            
        } else {
            console.log('⚠️ Finalização concluída, verificando resultado...');
            
            // Verificar se há mensagens de erro
            const alertas = $('.alert-danger');
            if (alertas.length > 0) {
                console.error('❌ Erro na finalização:');
                alertas.each(function() {
                    console.error(`   ${$(this).text().trim()}`);
                });
            } else {
                console.log('✅ Nenhum erro detectado');
            }
        }
    }
}

// Função para ir para o dashboard (após finalização)
function irParaDashboard() {
    console.log('\n🏠 Redirecionando para o dashboard...');
    if (typeof irParaDashboard === 'function') {
        irParaDashboard();
    } else {
        window.location.href = '/dashboard';
    }
}

// Função para exibir instruções
function exibirInstrucoes() {
    console.log('\n📖 INSTRUÇÕES DE USO:');
    console.log('\n1. Navegue para a página de revisão:');
    console.log('   /rh/colaboradores/adesao/revisao?sessionId=SEU_SESSION_ID');
    console.log('\n2. Abra o console do navegador (F12)');
    console.log('\n3. Cole este script e execute:');
    console.log('   executarTesteRevisao()');
    console.log('\n4. Para finalizar o processo:');
    console.log('   finalizarProcessoAdesao()');
    console.log('\n5. Para ir ao dashboard após finalização:');
    console.log('   irParaDashboard()');
}

// Executar instruções automaticamente
exibirInstrucoes();

console.log('\n🎯 Script carregado! Execute executarTesteRevisao() para começar.');