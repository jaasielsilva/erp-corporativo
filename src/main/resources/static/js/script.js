
/*<![CDATA[*/
  const labels = /*[[${graficoLabels}]]*/[];
  const data = /*[[${graficoValores}]]*/[];

  console.log("Labels recebidos:", labels);
  console.log("Valores recebidos:", data);

  const graficoElement = document.getElementById('graficoVendas');
  if (graficoElement) {
    const ctx = graficoElement.getContext('2d');
    const graficoVendas = new Chart(ctx, {
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
        y: {
          beginAtZero: true
        }
      }
    }
  });
  }
  /*]]>*/
