
function verDetalhesAdesao(id) {
  alert("Ver detalhes da adesão ID: " + id);
  // window.location.href = "/adesao/" + id;
}

function abrirModalAdesao() {
  document.getElementById("modalAdesao").style.display = "block";
}

function fecharModalAdesao() {
  document.getElementById("modalAdesao").style.display = "none";
}

// Fecha o modal clicando fora dele
window.onclick = function(event) {
  let modal = document.getElementById("modalAdesao");
  if (event.target === modal) {
    modal.style.display = "none";
  }
}

