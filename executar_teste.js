// Script automatizado para executar o teste completo da adesao
// Este script simula o preenchimento e envio automatico

console.log('🚀 EXECUTANDO TESTE AUTOMATIZADO DE ADESAO');

// Simular preenchimento dos campos principais
const camposObrigatorios = [
    { id: 'nome', valor: 'Joao Silva Santos' },
    { id: 'cpf', valor: '123.456.789-00' },
    { id: 'rg', valor: '12.345.678-9' },
    { id: 'dataNascimento', valor: '1990-05-15' },
    { id: 'email', valor: 'joao.silva@empresa.com' },
    { id: 'telefone', valor: '(11) 99999-8888' },
    { id: 'dataAdmissao', valor: '2024-08-29' },
    { id: 'salario', valor: '5000.00' },
    { id: 'cargaHoraria', valor: '40' },
    { id: 'cep', valor: '01310-100' },
    { id: 'numero', valor: '1000' },
    { id: 'observacoes', valor: 'Teste automatizado de adesao' }
];

// Funcao para preencher campos
function preencherCampos() {
    let preenchidos = 0;
    
    camposObrigatorios.forEach(campo => {
        const elemento = document.getElementById(campo.id);
        if (elemento) {
            elemento.value = campo.valor;
            elemento.dispatchEvent(new Event('input', { bubbles: true }));
            elemento.dispatchEvent(new Event('change', { bubbles: true }));
            preenchidos++;
            console.log(`✓ ${campo.id}: ${campo.valor}`);
        }
    });
    
    // Preencher selects
    const selects = ['sexo', 'estadoCivil', 'tipoContrato'];
    selects.forEach(selectId => {
        const select = document.getElementById(selectId);
        if (select && select.options.length > 1) {
            select.selectedIndex = 1;
            select.dispatchEvent(new Event('change', { bubbles: true }));
            preenchidos++;
            console.log(`✓ ${selectId}: ${select.options[1].text}`);
        }
    });
    
    // Aguardar carregamento e preencher cargo/departamento
    setTimeout(() => {
        ['cargoId', 'departamentoId'].forEach(selectId => {
            const select = document.getElementById(selectId);
            if (select && select.options.length > 1) {
                select.selectedIndex = 1;
                select.dispatchEvent(new Event('change', { bubbles: true }));
                console.log(`✓ ${selectId}: ${select.options[1].text}`);
            }
        });
    }, 1000);
    
    console.log(`\n📊 RESUMO: ${preenchidos} campos preenchidos`);
    return preenchidos;
}

// Funcao para enviar formulario
function enviarFormulario() {
    console.log('\n📤 TENTANDO ENVIAR FORMULARIO...');
    
    // Tentar diferentes seletores para o botao
    const seletores = [
        'button[onclick="enviarDadosPessoais()"]',
        'button[type="submit"]',
        'button.btn-primary',
        'input[type="submit"]'
    ];
    
    for (let seletor of seletores) {
        const botao = document.querySelector(seletor);
        if (botao) {
            botao.click();
            console.log(`✅ FORMULARIO ENVIADO via ${seletor}`);
            return true;
        }
    }
    
    // Buscar por texto
    const botoes = Array.from(document.querySelectorAll('button'));
    const botaoTexto = botoes.find(btn => 
        btn.textContent.toLowerCase().includes('próxima') ||
        btn.textContent.toLowerCase().includes('proxima') ||
        btn.textContent.toLowerCase().includes('enviar') ||
        btn.textContent.toLowerCase().includes('etapa')
    );
    
    if (botaoTexto) {
        botaoTexto.click();
        console.log(`✅ FORMULARIO ENVIADO via texto: "${botaoTexto.textContent}"`);
        return true;
    }
    
    console.error('❌ ERRO: Botao de envio nao encontrado');
    return false;
}

// Executar teste completo
function executarTesteCompleto() {
    console.log('\n=== INICIANDO TESTE COMPLETO ===');
    
    // Preencher campos
    const camposPreenchidos = preencherCampos();
    
    if (camposPreenchidos > 0) {
        console.log('\n⏳ Aguardando 3 segundos antes de enviar...');
        
        setTimeout(() => {
            const enviado = enviarFormulario();
            
            if (enviado) {
                console.log('\n🎉 TESTE DA ETAPA 1 CONCLUIDO COM SUCESSO!');
                console.log('\n📋 PROXIMOS PASSOS:');
                console.log('- Aguardar redirecionamento para etapa de documentos');
                console.log('- Testar upload de documentos');
                console.log('- Testar selecao de beneficios');
                console.log('- Finalizar processo de adesao');
            } else {
                console.log('\n⚠️ TESTE PARCIAL: Campos preenchidos, mas envio falhou');
            }
        }, 3000);
    } else {
        console.error('\n❌ ERRO: Nenhum campo foi preenchido');
    }
}

// Executar automaticamente
executarTesteCompleto();

console.log('\n💡 COMANDOS MANUAIS DISPONIVEIS:');
console.log('- executarTesteCompleto() - Executa teste completo');
console.log('- preencherCampos() - Apenas preenche os campos');
console.log('- enviarFormulario() - Apenas envia o formulario');