// venda.js

document.addEventListener('DOMContentLoaded', () => {
  const btnAdicionarProduto = document.getElementById('btnAdicionarProduto');
  const inputEAN = document.getElementById('ean');
  const produtoEncontrado = document.getElementById('produtoEncontrado');
  const tabelaProdutosBody = document.querySelector('#tabelaProdutos tbody');
  const inputTotal = document.getElementById('total');
  const resumoTotal = document.getElementById('resumoTotal');

  // Formata número para moeda BRL
  function formatarValor(valor) {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  // Atualiza o texto do resumo do total formatado
  function atualizarResumo() {
    let val = parseFloat(inputTotal.value);
    if (!isNaN(val)) {
      resumoTotal.textContent = "Valor total formatado: " + formatarValor(val);
    } else {
      resumoTotal.textContent = "Valor total formatado: R$ 0,00";
    }
  }

  // Atualiza o total da venda somando os subtotais da tabela
  function atualizarResumoTotal() {
    let total = 0;
    tabelaProdutosBody.querySelectorAll('tr').forEach(tr => {
      const subtotalText = tr.querySelector('.subtotal').textContent;
      const subtotalNum = parseFloat(subtotalText.replace('R$ ', '').replace(',', '.'));
      if (!isNaN(subtotalNum)) total += subtotalNum;
    });
    inputTotal.value = total.toFixed(2);
    atualizarResumo();
  }

  // Função para remover uma linha da tabela
  function removerLinha(tr) {
    tr.remove();
    atualizarResumoTotal();
  }

  // Adicionar produto simulado (pode ser substituído por busca real no backend)
  btnAdicionarProduto.addEventListener('click', () => {
    const ean = inputEAN.value.trim();
    if (!ean) {
      alert('Por favor, digite ou escaneie o código de barras.');
      return;
    }

    // Simulação de busca - substitua por sua busca backend aqui
    const produtoSimulado = {
      nome: 'Produto Exemplo',
      ean: ean,
      precoUnitario: 25.50
    };

    // Verificar se produto já está na tabela
    const linhas = tabelaProdutosBody.querySelectorAll('tr');
    for (let linha of linhas) {
      if (linha.dataset.ean === ean) {
        alert('Produto já adicionado na lista.');
        inputEAN.value = '';
        return;
      }
    }

    produtoEncontrado.textContent = `Produto encontrado: ${produtoSimulado.nome}`;
    inputEAN.value = '';

    // Criar nova linha
    const tr = document.createElement('tr');
    tr.dataset.ean = produtoSimulado.ean;

    tr.innerHTML = `
      <td>${produtoSimulado.nome}</td>
      <td>${produtoSimulado.ean}</td>
      <td><input type="number" value="1" min="1" class="quantidade" style="width: 60px;" /></td>
      <td>R$ ${produtoSimulado.precoUnitario.toFixed(2)}</td>
      <td class="subtotal">R$ ${produtoSimulado.precoUnitario.toFixed(2)}</td>
      <td><button type="button" class="btnRemover" title="Remover produto"><i class="fas fa-trash-alt"></i></button></td>
    `;

    tabelaProdutosBody.appendChild(tr);

    const inputQuantidade = tr.querySelector('.quantidade');
    const tdSubtotal = tr.querySelector('.subtotal');

    // Atualiza subtotal ao alterar quantidade
    inputQuantidade.addEventListener('input', () => {
      let qty = parseInt(inputQuantidade.value);
      if (isNaN(qty) || qty < 1) {
        qty = 1;
        inputQuantidade.value = 1;
      }
      const subtotal = qty * produtoSimulado.precoUnitario;
      tdSubtotal.textContent = `R$ ${subtotal.toFixed(2)}`;
      atualizarResumoTotal();
    });

    // Remover produto
    tr.querySelector('.btnRemover').addEventListener('click', () => {
      removerLinha(tr);
    });

    atualizarResumoTotal();
  });

  // Inicializa resumo ao carregar a página
  atualizarResumoTotal();
});
