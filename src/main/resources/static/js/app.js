// Aplicação principal (stub)
// Este arquivo evita 404 em páginas que referenciam /js/app.js
// e pode centralizar inicializações leves da UI.

(function () {
  // Log discreto para depuração
  if (typeof console !== 'undefined' && console.debug) {
    console.debug('app.js carregado');
  }

  // Inicialização segura
  document.addEventListener('DOMContentLoaded', function () {
    // Inicialização global de tooltips do Bootstrap 5
    try {
      var els = document.querySelectorAll('[data-bs-toggle="tooltip"]');
      if (els && typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        Array.prototype.forEach.call(els, function (el) {
          try { new bootstrap.Tooltip(el); } catch (e) {}
        });
      }
    } catch (err) {
      if (console && console.debug) console.debug('Tooltip init skipped:', err);
    }

    // Espaço para outras inicializações globais de UI
  });
})();