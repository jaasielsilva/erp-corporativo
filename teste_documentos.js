/**
 * Script para testar a etapa de documentos do processo de adesão
 * Execute no console do navegador na página de documentos
 */

// Dados fictícios para teste
const dadosDocumentos = {
    sessionId: 'test123',
    documentos: {
        rg: {
            nome: 'rg_joao_silva.pdf',
            tipo: 'application/pdf',
            tamanho: '1.2MB'
        },
        cpf: {
            nome: 'cpf_joao_silva.pdf', 
            tipo: 'application/pdf',
            tamanho: '800KB'
        },
        endereco: {
            nome: 'comprovante_endereco.pdf',
            tipo: 'application/pdf', 
            tamanho: '1.5MB'
        }
    }
};

// Função para simular upload de documento
function simularUploadDocumento(tipo, nomeArquivo) {
    console.log(`🔄 Simulando upload do documento: ${tipo} - ${nomeArquivo}`);
    
    // Simular progresso de upload
    const progressBar = document.querySelector(`#progress-${tipo} .progress-bar`);
    const progressContainer = document.querySelector(`#progress-${tipo}`);
    
    if (progressContainer) {
        progressContainer.style.display = 'block';
        
        let progress = 0;
        const interval = setInterval(() => {
            progress += 10;
            if (progressBar) {
                progressBar.style.width = progress + '%';
                progressBar.textContent = progress + '%';
            }
            
            if (progress >= 100) {
                clearInterval(interval);
                setTimeout(() => {
                    mostrarDocumentoEnviado(tipo, nomeArquivo);
                    progressContainer.style.display = 'none';
                }, 500);
            }
        }, 200);
    }
}

// Função para mostrar documento como enviado
function mostrarDocumentoEnviado(tipo, nomeArquivo) {
    const preview = document.querySelector(`#preview-${tipo}`);
    if (preview) {
        preview.innerHTML = `
            <div class="file-success">
                <i class="fas fa-file-pdf fa-2x text-danger mb-2"></i>
                <p class="mb-1"><strong>${nomeArquivo}</strong></p>
                <small class="text-success">✓ Enviado com sucesso</small>
                <div class="mt-2">
                    <button class="btn btn-sm btn-outline-danger" onclick="removerDocumento('${tipo}')">
                        <i class="fas fa-trash"></i> Remover
                    </button>
                </div>
            </div>
        `;
        preview.style.display = 'block';
    }
    
    console.log(`✅ Documento ${tipo} enviado com sucesso: ${nomeArquivo}`);
}

// Função para remover documento
function removerDocumento(tipo) {
    const preview = document.querySelector(`#preview-${tipo}`);
    if (preview) {
        preview.style.display = 'none';
        preview.innerHTML = '';
    }
    console.log(`🗑️ Documento ${tipo} removido`);
}

// Função para verificar se todos os documentos obrigatórios foram enviados
function verificarDocumentosObrigatorios() {
    const documentosObrigatorios = ['rg', 'cpf', 'endereco'];
    const documentosEnviados = [];
    
    documentosObrigatorios.forEach(tipo => {
        const preview = document.querySelector(`#preview-${tipo}`);
        if (preview && preview.style.display !== 'none' && preview.innerHTML.trim() !== '') {
            documentosEnviados.push(tipo);
        }
    });
    
    const todosEnviados = documentosEnviados.length === documentosObrigatorios.length;
    console.log(`📋 Documentos obrigatórios: ${documentosEnviados.length}/${documentosObrigatorios.length}`);
    console.log(`✅ Todos enviados: ${todosEnviados}`);
    
    return todosEnviados;
}

// Função para prosseguir para próxima etapa
function prosseguirParaBeneficios() {
    if (!verificarDocumentosObrigatorios()) {
        alert('❌ Todos os documentos obrigatórios devem ser enviados!');
        return;
    }
    
    console.log('🔄 Enviando dados para próxima etapa...');
    
    // Simular envio AJAX
    $.ajax({
        url: '/rh/colaboradores/adesao/documentos',
        method: 'POST',
        data: { sessionId: dadosDocumentos.sessionId },
        success: function(response) {
            if (response.success) {
                console.log('✅ Documentos processados com sucesso!');
                alert('✅ Documentos processados com sucesso! Redirecionando para benefícios...');
                setTimeout(() => {
                    window.location.href = `/rh/colaboradores/adesao/beneficios?sessionId=${dadosDocumentos.sessionId}`;
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
function executarTesteDocumentos() {
    console.log('🚀 Iniciando teste da etapa de documentos...');
    console.log('📄 Dados de teste:', dadosDocumentos);
    
    // Simular upload dos documentos obrigatórios
    setTimeout(() => simularUploadDocumento('rg', dadosDocumentos.documentos.rg.nome), 1000);
    setTimeout(() => simularUploadDocumento('cpf', dadosDocumentos.documentos.cpf.nome), 2000);
    setTimeout(() => simularUploadDocumento('endereco', dadosDocumentos.documentos.endereco.nome), 3000);
    
    // Verificar status após uploads
    setTimeout(() => {
        console.log('\n📊 Status dos documentos:');
        verificarDocumentosObrigatorios();
        console.log('\n✨ Teste concluído! Use prosseguirParaBeneficios() para continuar.');
    }, 4000);
}

// Instruções
console.log('📋 INSTRUÇÕES PARA TESTE DE DOCUMENTOS:');
console.log('1. Execute: executarTesteDocumentos()');
console.log('2. Aguarde o upload dos documentos');
console.log('3. Execute: prosseguirParaBeneficios()');
console.log('\n🔧 Funções disponíveis:');
console.log('- executarTesteDocumentos() - Inicia o teste');
console.log('- verificarDocumentosObrigatorios() - Verifica status');
console.log('- prosseguirParaBeneficios() - Avança para próxima etapa');
console.log('- removerDocumento(tipo) - Remove um documento específico');

// Auto-executar se solicitado
if (typeof autoExecutar !== 'undefined' && autoExecutar) {
    executarTesteDocumentos();
}