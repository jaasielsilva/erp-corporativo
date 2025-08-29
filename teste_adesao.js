// Script para testar o fluxo de adesao de colaborador - PASSO A PASSO
// Execute este script no console do navegador na pagina de adesao

console.log('=== INICIANDO TESTE DE ADESAO DE COLABORADOR ===');

// Dados ficticios para teste
const dadosFicticios = {
    nome: 'Joao Silva Santos',
    sexo: 'MASCULINO',
    cpf: '123.456.789-00',
    rg: '12.345.678-9',
    dataNascimento: '1990-05-15',
    email: 'joao.silva@empresa.com',
    telefone: '(11) 99999-8888',
    estadoCivil: 'SOLTEIRO',
    dataAdmissao: '2024-08-29',
    salario: '5000.00',
    tipoContrato: 'CLT',
    cargaHoraria: '40',
    cep: '01310-100',
    logradouro: 'Avenida Paulista',
    numero: '1000',
    complemento: 'Sala 101',
    bairro: 'Bela Vista',
    cidade: 'Sao Paulo',
    estado: 'SP',
    observacoes: 'Colaborador com experiencia em desenvolvimento de sistemas.'
};

// Funcao para aguardar um tempo
function aguardar(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// Funcao para preencher um campo especifico
function preencherCampo(id, valor) {
    const elemento = document.getElementById(id);
    if (elemento) {
        elemento.value = valor;
        elemento.dispatchEvent(new Event('input', { bubbles: true }));
        elemento.dispatchEvent(new Event('change', { bubbles: true }));
        console.log(`✓ Campo ${id} preenchido: ${valor}`);
        return true;
    } else {
        console.warn(`✗ Campo ${id} nao encontrado`);
        return false;
    }
}

// Funcao para selecionar opcao em select
function selecionarOpcao(id, indice = 1) {
    const select = document.getElementById(id);
    if (select && select.options.length > indice) {
        select.selectedIndex = indice;
        select.dispatchEvent(new Event('change', { bubbles: true }));
        console.log(`✓ ${id} selecionado: ${select.options[indice].text}`);
        return true;
    } else {
        console.warn(`✗ Select ${id} nao encontrado ou sem opcoes`);
        return false;
    }
}

// Funcao principal para preencher formulario passo a passo
async function preencherFormularioCompleto() {
    console.log('\n--- ETAPA 1: PREENCHENDO DADOS PESSOAIS ---');
    
    // Dados pessoais
    preencherCampo('nome', dadosFicticios.nome);
    await aguardar(300);
    
    selecionarOpcao('sexo', 1); // MASCULINO
    await aguardar(300);
    
    preencherCampo('cpf', dadosFicticios.cpf);
    await aguardar(300);
    
    preencherCampo('rg', dadosFicticios.rg);
    await aguardar(300);
    
    preencherCampo('dataNascimento', dadosFicticios.dataNascimento);
    await aguardar(300);
    
    preencherCampo('email', dadosFicticios.email);
    await aguardar(300);
    
    preencherCampo('telefone', dadosFicticios.telefone);
    await aguardar(300);
    
    selecionarOpcao('estadoCivil', 1); // SOLTEIRO
    await aguardar(500);
    
    console.log('\n--- ETAPA 2: PREENCHENDO DADOS PROFISSIONAIS ---');
    
    // Aguardar carregamento dos selects
    await aguardar(1000);
    
    selecionarOpcao('cargoId', 1);
    await aguardar(300);
    
    selecionarOpcao('departamentoId', 1);
    await aguardar(300);
    
    preencherCampo('dataAdmissao', dadosFicticios.dataAdmissao);
    await aguardar(300);
    
    preencherCampo('salario', dadosFicticios.salario);
    await aguardar(300);
    
    selecionarOpcao('tipoContrato', 1); // CLT
    await aguardar(300);
    
    preencherCampo('cargaHoraria', dadosFicticios.cargaHoraria);
    await aguardar(500);
    
    console.log('\n--- ETAPA 3: PREENCHENDO ENDERECO ---');
    
    preencherCampo('cep', dadosFicticios.cep);
    await aguardar(1000); // Aguardar busca do CEP
    
    preencherCampo('logradouro', dadosFicticios.logradouro);
    await aguardar(300);
    
    preencherCampo('numero', dadosFicticios.numero);
    await aguardar(300);
    
    preencherCampo('complemento', dadosFicticios.complemento);
    await aguardar(300);
    
    preencherCampo('bairro', dadosFicticios.bairro);
    await aguardar(300);
    
    preencherCampo('cidade', dadosFicticios.cidade);
    await aguardar(300);
    
    preencherCampo('estado', dadosFicticios.estado);
    await aguardar(500);
    
    console.log('\n--- ETAPA 4: PREENCHENDO OBSERVACOES ---');
    
    preencherCampo('observacoes', dadosFicticios.observacoes);
    await aguardar(500);
    
    console.log('\n✅ FORMULARIO PREENCHIDO COMPLETAMENTE!');
    console.log('\n--- PROXIMO PASSO: ENVIAR DADOS ---');
    console.log('Execute: enviarDadosPessoais() para continuar');
}

// Funcao para enviar os dados
function enviarDadosPessoais() {
    console.log('\n🚀 ENVIANDO DADOS PESSOAIS...');
    
    const botaoEnviar = document.querySelector('button[onclick="enviarDadosPessoais()"]');
    if (botaoEnviar) {
        botaoEnviar.click();
        console.log('✓ Botao de envio clicado!');
    } else {
        // Tentar encontrar por texto
        const botoes = Array.from(document.querySelectorAll('button'));
        const botaoProxima = botoes.find(btn => 
            btn.textContent.includes('Próxima') || 
            btn.textContent.includes('Proxima') || 
            btn.textContent.includes('Etapa')
        );
        if (botaoProxima) {
            botaoProxima.click();
            console.log('✓ Botao "Proxima Etapa" clicado!');
        } else {
            console.error('✗ Botao de envio nao encontrado');
        }
    }
}

// Executar automaticamente
console.log('\n📋 INICIANDO PREENCHIMENTO AUTOMATICO...');
preencherFormularioCompleto();

console.log('\n💡 COMANDOS DISPONIVEIS:');
console.log('- preencherFormularioCompleto() - Preenche todo o formulario');
console.log('- enviarDadosPessoais() - Envia os dados para proxima etapa');
console.log('- preencherCampo("id", "valor") - Preenche campo especifico');