    function abrirModalAdesao() {
      document.getElementById('modalAdesao').style.display = 'flex';
    }
    
    function fecharModalAdesao() {
      document.getElementById('modalAdesao').style.display = 'none';
      document.querySelector('#modalAdesao form').reset();
      document.getElementById('dependentesSection').style.display = 'none';
    }
    
    function toggleDependentes() {
      const checkbox = document.getElementById('incluirDependentes');
      const section = document.getElementById('dependentesSection');
      section.style.display = checkbox.checked ? 'block' : 'none';
    }
    
    function adicionarDependente() {
      const list = document.getElementById('dependentesList');
      const newItem = list.firstElementChild.cloneNode(true);
      // Limpar valores dos inputs
      newItem.querySelectorAll('input, select').forEach(input => input.value = '');
      list.appendChild(newItem);
    }
    
    function removerDependente(btn) {
      const list = document.getElementById('dependentesList');
      if (list.children.length > 1) {
        btn.closest('.dependente-item').remove();
      }
    }
    
    function salvarAdesao() {
      const form = document.querySelector('#modalAdesao form');
      if (form.checkValidity()) {
        alert('Adesão salva com sucesso!');
        fecharModalAdesao();
        location.reload();
      } else {
        alert('Por favor, preencha todos os campos obrigatórios.');
      }
    }
    
    function verDetalhes(id) {
      alert(`Visualizando detalhes do plano ${id}`);
    }
    
    function editarPlano(id) {
      alert(`Editando plano ${id}`);
    }
    
    function gerenciarDependentes(id) {
      alert(`Gerenciando dependentes do colaborador ${id}`);
    }
    
    function suspenderPlano(id) {
      if (confirm('Tem certeza que deseja suspender este plano?')) {
        alert(`Plano ${id} suspenso com sucesso!`);
      }
    }
    
    function reativarPlano(id) {
      if (confirm('Tem certeza que deseja reativar este plano?')) {
        alert(`Plano ${id} reativado com sucesso!`);
      }
    }
    
    function filtrarPlanos() {
      alert('Aplicando filtros...');
    }
    
    function importarDados() {
      alert('Abrindo importação de dados...');
    }
    
    function exportarRelatorio() {
      alert('Exportando relatório de planos de saúde...');
    }