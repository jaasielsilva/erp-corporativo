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
      // Remove "R$ ", troca vírgula por ponto, e faz parseFloat
      const subtotalNum = parseFloat(subtotalText.replace('R$ ', '').replace(/\./g, '').replace(',', '.'));
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

  // Função para adicionar o produto na tabela
  function adicionarProdutoNaTabela(produto) {
    console.log('Produto recebido:', produto);

    // Tentativa de pegar o preço de forma flexível
    const precoRaw = produto.preco || produto.precoVenda || produto.precoUnitario;
    if (!precoRaw && precoRaw !== 0) {
      alert('Erro: preço do produto não encontrado.');
      return;
    }

    // Normaliza preço string para número float
    const preco = parseFloat(typeof precoRaw === 'string' ? precoRaw.replace(/\./g, '').replace(',', '.') : precoRaw);
    if (isNaN(preco)) {
      alert('Erro: preço do produto inválido.');
      return;
    }

    // Verificar se produto já está na tabela
    if ([...tabelaProdutosBody.children].some(row => row.dataset.ean === produto.ean)) {
      alert('Produto já adicionado na lista.');
      return;
    }

    const tr = document.createElement('tr');
    tr.dataset.ean = produto.ean;

    tr.innerHTML = `
      <td>${produto.nome}</td>
      <td>${produto.ean}</td>
      <td><input type="number" min="1" max="${produto.estoque}" value="1" class="quantidade" style="width: 60px;" /></td>
      <td>R$ ${preco.toFixed(2)}</td>
      <td class="subtotal">R$ ${preco.toFixed(2)}</td>
      <td><button type="button" class="btnRemover" title="Remover produto"><i class="fas fa-trash-alt"></i></button></td>
    `;

    tabelaProdutosBody.appendChild(tr);

    const inputQuantidade = tr.querySelector('.quantidade');
    const tdSubtotal = tr.querySelector('.subtotal');

    inputQuantidade.addEventListener('input', () => {
      let qty = parseInt(inputQuantidade.value);
      if (isNaN(qty) || qty < 1) {
        qty = 1;
        inputQuantidade.value = 1;
      }
      if (qty > produto.estoque) {
        qty = produto.estoque;
        inputQuantidade.value = qty;
        alert('Quantidade maior que o estoque disponível.');
      }
      const subtotal = qty * preco;
      tdSubtotal.textContent = `R$ ${subtotal.toFixed(2)}`;
      atualizarResumoTotal();
    });

    tr.querySelector('.btnRemover').addEventListener('click', () => {
      removerLinha(tr);
    });

    atualizarResumoTotal();
  }

  // Evento principal para o botão de adicionar produto
  btnAdicionarProduto.addEventListener('click', () => {
    const ean = inputEAN.value.trim();
    if (!ean) {
      alert('Por favor, digite ou escaneie o código de barras.');
      return;
    }

    fetch(`/api/produtos/buscar-por-ean?ean=${ean}`)
      .then(response => {
        if (!response.ok) {
          return response.text().then(text => { throw new Error(text) });
        }
        return response.json();
      })
      .then(produto => {
        adicionarProdutoNaTabela(produto);
        inputEAN.value = '';
        produtoEncontrado.style.color = 'green';
        produtoEncontrado.textContent = `Produto encontrado: ${produto.nome} adicionado.`;
      })
      .catch(error => {
        produtoEncontrado.style.color = 'red';
        produtoEncontrado.textContent = error.message;
      });
  });

  // Inicializa resumo ao carregar a página
  atualizarResumoTotal();
});
