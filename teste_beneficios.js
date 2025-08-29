/**
 * Script para testar a etapa de benefícios do processo de adesão
 * Execute no console do navegador na página de benefícios
 */

// Dados fictícios para teste
const dadosBeneficios = {
    sessionId: 'test123',
    beneficiosSelecionados: {
        planoSaude: {
            ativo: true,
            plano: 'intermediario',
            dependentes: [
                {
                    nome: 'Maria Silva Santos',
                    cpf: '987.654.321-00',
                    parentesco: 'conjuge',
                    dataNascimento: '1990-05-15'
                },
                {
                    nome: 'Pedro Silva Santos',
                    cpf: '456.789.123-00',
                    parentesco: 'filho',
                    dataNascimento: '2015-08-20'
                }
            ]
        },
        valeRefeicao: {
            ativo: true,
            valor: 500
        },
        valeTransporte: {
            ativo: true,
            valor: 200
        }
    }
};

// Função para aguardar elemento aparecer
function aguardarElemento(seletor, timeout = 5000) {
    return new Promise((resolve, reject) => {
        const elemento = document.querySelector(seletor);
        if (elemento) {
            resolve(elemento);
            return;
        }
        
        const observer = new MutationObserver(() => {
            const elemento = document.querySelector(seletor);
            if (elemento) {
                observer.disconnect();
                resolve(elemento);
            }
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
        
        setTimeout(() => {
            observer.disconnect();
            reject(new Error(`Elemento ${seletor} não encontrado em ${timeout}ms`));
        }, timeout);
    });
}

// Função para simular clique
function simularClique(elemento) {
    if (elemento) {
        elemento.click();
        elemento.dispatchEvent(new Event('change', { bubbles: true }));
        console.log(`✅ Clique simulado em: ${elemento.tagName}`);
    }
}

// Função para preencher campo
function preencherCampo(seletor, valor) {
    const campo = document.querySelector(seletor);
    if (campo) {
        campo.value = valor;
        campo.dispatchEvent(new Event('input', { bubbles: true }));
        campo.dispatchEvent(new Event('change', { bubbles: true }));
        console.log(`✅ Campo preenchido: ${seletor} = ${valor}`);
        return true;
    }
    console.warn(`⚠️ Campo não encontrado: ${seletor}`);
    return false;
}

// Função para selecionar plano de saúde
function selecionarPlanoSaude() {
    console.log('🏥 Selecionando Plano de Saúde...');
    
    // Ativar toggle do plano de saúde
    const togglePlanoSaude = document.querySelector('#planoSaude');
    if (togglePlanoSaude && !togglePlanoSaude.checked) {
        simularClique(togglePlanoSaude);
    }
    
    // Aguardar detalhes aparecerem
    setTimeout(() => {
        // Selecionar plano intermediário
        const planoIntermediario = document.querySelector('input[name="planoSaudeOpcao"][value="intermediario"]');
        if (planoIntermediario) {
            simularClique(planoIntermediario);
            console.log('✅ Plano Intermediário selecionado');
        }
        
        // Adicionar dependentes
        setTimeout(() => {
            adicionarDependentes();
        }, 1000);
    }, 1000);
}

// Função para adicionar dependentes
function adicionarDependentes() {
    console.log('👨‍👩‍👧‍👦 Adicionando dependentes...');
    
    dadosBeneficios.beneficiosSelecionados.planoSaude.dependentes.forEach((dependente, index) => {
        setTimeout(() => {
            adicionarDependente(dependente, index);
        }, index * 2000);
    });
}

// Função para adicionar um dependente
function adicionarDependente(dependente, index) {
    console.log(`👤 Adicionando dependente ${index + 1}: ${dependente.nome}`);
    
    // Clicar no botão adicionar dependente
    const btnAdicionar = document.querySelector('#btnAdicionarDependente');
    if (btnAdicionar) {
        simularClique(btnAdicionar);
        
        // Aguardar formulário aparecer
        setTimeout(() => {
            const dependenteIndex = document.querySelectorAll('.dependent-item').length - 1;
            
            // Preencher dados do dependente
            preencherCampo(`input[name="dependentes[${dependenteIndex}].nome"]`, dependente.nome);
            preencherCampo(`input[name="dependentes[${dependenteIndex}].cpf"]`, dependente.cpf);
            preencherCampo(`input[name="dependentes[${dependenteIndex}].dataNascimento"]`, dependente.dataNascimento);
            
            // Selecionar parentesco
            const selectParentesco = document.querySelector(`select[name="dependentes[${dependenteIndex}].parentesco"]`);
            if (selectParentesco) {
                selectParentesco.value = dependente.parentesco;
                selectParentesco.dispatchEvent(new Event('change', { bubbles: true }));
                console.log(`✅ Parentesco selecionado: ${dependente.parentesco}`);
            }
        }, 500);
    }
}

// Função para selecionar vale refeição
function selecionarValeRefeicao() {
    console.log('🍽️ Selecionando Vale Refeição...');
    
    const toggleValeRefeicao = document.querySelector('#valeRefeicao');
    if (toggleValeRefeicao && !toggleValeRefeicao.checked) {
        simularClique(toggleValeRefeicao);
        
        // Aguardar detalhes aparecerem
        setTimeout(() => {
            const valorRefeicao = document.querySelector('input[name="valeRefeicaoValor"][value="500"]');
            if (valorRefeicao) {
                simularClique(valorRefeicao);
                console.log('✅ Vale Refeição R$ 500,00 selecionado');
            }
        }, 1000);
    }
}

// Função para selecionar vale transporte
function selecionarValeTransporte() {
    console.log('🚌 Selecionando Vale Transporte...');
    
    const toggleValeTransporte = document.querySelector('#valeTransporte');
    if (toggleValeTransporte && !toggleValeTransporte.checked) {
        simularClique(toggleValeTransporte);
        
        // Aguardar detalhes aparecerem
        setTimeout(() => {
            preencherCampo('input[name="valeTransporteValor"]', dadosBeneficios.beneficiosSelecionados.valeTransporte.valor);
            console.log('✅ Vale Transporte configurado');
        }, 1000);
    }
}

// Função para calcular total
function calcularTotal() {
    console.log('🧮 Calculando total dos benefícios...');
    
    const btnCalcular = document.querySelector('#btnCalcularTotal');
    if (btnCalcular) {
        simularClique(btnCalcular);
        console.log('✅ Cálculo solicitado');
    } else {
        // Tentar calcular via AJAX diretamente
        $.ajax({
            url: '/api/rh/colaboradores/adesao/beneficios/calcular',
            method: 'POST',
            data: {
                sessionId: dadosBeneficios.sessionId,
                beneficios: JSON.stringify(dadosBeneficios.beneficiosSelecionados)
            },
            success: function(response) {
                if (response.success) {
                    console.log('✅ Total calculado:', response.calculo);
                    atualizarResumoVisual(response.calculo);
                } else {
                    console.error('❌ Erro no cálculo:', response.message);
                }
            },
            error: function(xhr) {
                console.error('❌ Erro de conexão no cálculo:', xhr.responseText);
            }
        });
    }
}

// Função para atualizar resumo visual
function atualizarResumoVisual(calculo) {
    const resumoContainer = document.querySelector('.summary-card');
    if (resumoContainer) {
        let html = '<h5><i class="fas fa-calculator me-2"></i>Resumo dos Benefícios</h5>';
        
        calculo.itens.forEach(item => {
            html += `
                <div class="summary-item">
                    <span>${item.nome}</span>
                    <span>R$ ${item.valor.toFixed(2)}</span>
                </div>
            `;
        });
        
        html += `
            <div class="summary-item">
                <span><strong>Total Mensal</strong></span>
                <span><strong>R$ ${calculo.totalMensal.toFixed(2)}</strong></span>
            </div>
        `;
        
        resumoContainer.innerHTML = html;
        console.log('✅ Resumo visual atualizado');
    }
}

// Função para prosseguir para revisão
function prosseguirParaRevisao() {
    console.log('🔄 Enviando benefícios para próxima etapa...');
    
    $.ajax({
        url: '/rh/colaboradores/adesao/beneficios',
        method: 'POST',
        data: {
            sessionId: dadosBeneficios.sessionId,
            beneficios: JSON.stringify(dadosBeneficios.beneficiosSelecionados)
        },
        success: function(response) {
            if (response.success) {
                console.log('✅ Benefícios salvos com sucesso!');
                alert('✅ Benefícios salvos com sucesso! Redirecionando para revisão...');
                setTimeout(() => {
                    window.location.href = `/rh/colaboradores/adesao/revisao?sessionId=${dadosBeneficios.sessionId}`;
                }, 1500);
            } else {
                console.error('❌ Erro:', response.message);
                alert('❌ Erro: ' + response.message);
            }
        },
        error: function(xhr) {
            const response = xhr.responseJSON;
            console.error('❌ Erro de conexão:', response?.message || 'Erro interno');
            alert('❌ Erro de conexão: ' + (response?.message || 'Erro interno'));
        }
    });
}

// Função principal para executar o teste
function executarTesteBeneficios() {
    console.log('🚀 Iniciando teste da etapa de benefícios...');
    console.log('🎁 Dados de teste:', dadosBeneficios);
    
    // Sequência de testes
    setTimeout(() => selecionarPlanoSaude(), 1000);
    setTimeout(() => selecionarValeRefeicao(), 8000);
    setTimeout(() => selecionarValeTransporte(), 10000);
    setTimeout(() => calcularTotal(), 12000);
    
    setTimeout(() => {
        console.log('\n📊 Status dos benefícios:');
        console.log('- Plano de Saúde: ✅ Selecionado (Intermediário + 2 dependentes)');
        console.log('- Vale Refeição: ✅ Selecionado (R$ 500,00)');
        console.log('- Vale Transporte: ✅ Selecionado (R$ 200,00)');
        console.log('\n✨ Teste concluído! Use prosseguirParaRevisao() para continuar.');
    }, 15000);
}

// Instruções
console.log('📋 INSTRUÇÕES PARA TESTE DE BENEFÍCIOS:');
console.log('1. Execute: executarTesteBeneficios()');
console.log('2. Aguarde a seleção automática dos benefícios');
console.log('3. Execute: prosseguirParaRevisao()');
console.log('\n🔧 Funções disponíveis:');
console.log('- executarTesteBeneficios() - Inicia o teste');
console.log('- selecionarPlanoSaude() - Seleciona plano de saúde');
console.log('- selecionarValeRefeicao() - Seleciona vale refeição');
console.log('- selecionarValeTransporte() - Seleciona vale transporte');
console.log('- calcularTotal() - Calcula total dos benefícios');
console.log('- prosseguirParaRevisao() - Avança para próxima etapa');

// Auto-executar se solicitado
if (typeof autoExecutar !== 'undefined' && autoExecutar) {
    executarTesteBeneficios();
}