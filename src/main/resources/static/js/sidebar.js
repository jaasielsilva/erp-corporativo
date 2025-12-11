// Funcionalidade completa do sidebar
document.addEventListener('DOMContentLoaded', function() {
    // Controle de submenus
    initializeSubmenus();
    
    // Funcionalidade de busca
    initializeSearch();
    
    // Marcar página ativa
    markActivePage();
    
    // Adicionar badges de notificação
    addNotificationBadges();
    
    // Responsividade mobile
    initializeMobileMenu();
    
    // Inicializar triggers de notificação
    initializeNotificationTriggers();
});

// Controle de submenus com animações
function initializeSubmenus() {
  const submenuToggles = document.querySelectorAll('.submenu-toggle');
  if (submenuToggles.length === 0) return;

  submenuToggles.forEach(item => {
    item.addEventListener('click', e => {
      e.preventDefault();
      e.stopPropagation();

      const li = item.closest('.has-submenu');
      if (!li) return;

      // Fecha irmãos no mesmo nível
      const parentUl = li.parentElement;
      parentUl.querySelectorAll(':scope > .has-submenu.open').forEach(sib => {
        if (sib !== li) sib.classList.remove('open');
      });

      // Alterna o atual
      li.classList.toggle('open');
    });
  });
}

  
// Funcionalidade de busca
function initializeSearch() {
  const searchInput = document.getElementById('sidebar-search');
  if (searchInput) {
    searchInput.addEventListener('input', function() {
      const searchTerm = this.value.toLowerCase();
      const navItems = document.querySelectorAll('.sidebar nav ul li');
      
      // Reset all items first
      if (!searchTerm) {
        navItems.forEach(item => {
          item.style.display = 'block';
          // Não remove a classe 'open', mantém o estado atual dos menus
        });
        return;
      }
      
      navItems.forEach(item => {
        const link = item.querySelector('a');
        if (link && !link.classList.contains('submenu-toggle')) {
          const text = link.textContent.toLowerCase();
          
          if (text.includes(searchTerm)) {
            item.style.display = 'block';
            // Apenas torna visível os pais necessários, sem abrir
            let currentParent = item.closest('.has-submenu');
            while (currentParent) {
              currentParent.style.display = 'block';
              currentParent = currentParent.parentElement.closest('.has-submenu');
            }
          } else {
            // Só esconde se não for um item pai com filhos visíveis
            if (!item.classList.contains('has-submenu')) {
              item.style.display = 'none';
            }
          }
        }
      });
      
      // Esconde submenus vazios
      document.querySelectorAll('.has-submenu').forEach(parent => {
        const submenu = parent.querySelector('.submenu');
        if (submenu) {
          const visibleItems = submenu.querySelectorAll('li[style*="block"], li:not([style*="none"])');
          if (visibleItems.length === 0) {
            parent.style.display = 'none';
          }
        }
      });
    });
  }
}
  
// Marca página ativa baseada na URL
function markActivePage() {
  function setActivePage() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar nav ul li a:not(.submenu-toggle)');
    
    navLinks.forEach(link => {
      link.classList.remove('current-page', 'active');
      
      const href = link.getAttribute('href');
      if (href && (currentPath === href || currentPath.startsWith(href + '/'))) {
        link.classList.add('current-page');
        
        // Abre submenus pais se necessário
        let parent = link.closest('.submenu');
        while (parent) {
          const parentLi = parent.closest('.has-submenu');
          if (parentLi) {
            parentLi.classList.add('open');
          }
          parent = parentLi ? parentLi.closest('.submenu') : null;
        }
      }
    });
  }
  
  // Executa ao carregar e quando a URL muda
  setActivePage();
  window.addEventListener('popstate', setActivePage);
}
  
// Adiciona badges de notificação dinamicamente
function addNotificationBadges() {
  // Exemplo: adicionar badge para solicitações pendentes
  const solicitacoesLink = document.querySelector('a[href*="solicitacoes/pendentes"]');
  if (solicitacoesLink && !solicitacoesLink.querySelector('.nav-badge')) {
    const badge = document.createElement('span');
    badge.className = 'nav-badge warning';
    badge.textContent = '3';
    solicitacoesLink.appendChild(badge);
  }
  
  // Exemplo: badge para mensagens não lidas
  const chatLink = document.querySelector('a[href*="chat"]');
  if (chatLink && !chatLink.querySelector('.nav-badge')) {
    const badge = document.createElement('span');
    badge.className = 'nav-badge';
    badge.textContent = '5';
    chatLink.appendChild(badge);
  }
  
  // Exemplo: badge para suporte
  const suporteLink = document.querySelector('a[href*="suporte"]');
  if (suporteLink && !suporteLink.querySelector('.nav-badge')) {
    const badge = document.createElement('span');
    badge.className = 'nav-badge info';
    badge.textContent = '2';
    suporteLink.appendChild(badge);
  }
  

  
  // Adiciona badges após um pequeno delay
  setTimeout(() => {
    addNotificationBadges();
  }, 500);
}

// Função para inicializar triggers de notificação
function initializeNotificationTriggers() {
    // Badge para notificações
    const notificationLink = document.querySelector('.notification-trigger');
    if (notificationLink) {
        const badge = notificationLink.querySelector('.notification-badge');
        if (badge) {
            // O badge será controlado pelo sistema de notificações
            badge.style.display = 'none';
        }
        
        // Adicionar evento de clique para abrir central de notificações
        notificationLink.addEventListener('click', function(e) {
            e.preventDefault();
            // O sistema de notificações irá capturar este clique
        });
    }
}
  
// Responsividade para mobile
function initializeMobileMenu() {
    const menuToggle = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
        
        // Fechar sidebar ao clicar fora (mobile)
        document.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) {
                if (!sidebar.contains(e.target) && !menuToggle.contains(e.target)) {
                    sidebar.classList.remove('open');
                }
            }
        });
    }
}
