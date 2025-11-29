
/*<![CDATA[*/
  // Inicializa gráfico de vendas somente quando o elemento existir e Chart.js estiver disponível
  var graficoElement = document.getElementById('graficoVendas');
  if (graficoElement && typeof Chart !== 'undefined') {
    var labels = /*[[${graficoLabels}]]*/[];
    var data = /*[[${graficoValores}]]*/[];
    var ctx = graficoElement.getContext('2d');
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Vendas (R$)',
          data: data,
          backgroundColor: 'rgba(54, 162, 235, 0.5)',
          borderColor: 'rgba(54, 162, 235, 1)',
          borderWidth: 1
        }]
      },
      options: {
        scales: {
          y: { beginAtZero: true }
        }
      }
    });
  }
  /*]]>*/
