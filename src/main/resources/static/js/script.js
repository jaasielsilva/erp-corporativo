function toggleSidebar() {
  document.querySelector('.sidebar').classList.toggle('open');
}

const ctx = document.getElementById('graficoVendas').getContext('2d');
const chart = new Chart(ctx, {
  type: 'line',
  data: {
    labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai'],
    datasets: [{
      label: 'Vendas (R$)',
      data: [15000, 2000, 11800, 20500, 30200],
      borderColor: '#3b82f6',
      backgroundColor: 'rgba(59, 130, 246, 0.2)',
      tension: 0.3
    }]
  },
  options: {
    responsive: true,
    plugins: { legend: { display: true } }
  }
});
