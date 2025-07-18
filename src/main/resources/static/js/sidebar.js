// metodo click no botão categoria
document.querySelectorAll('.submenu-toggle').forEach(item => {
  item.addEventListener('click', event => {
    const parentLi = item.parentElement;
    const submenu = parentLi.querySelector('.submenu');

    if (submenu.style.display === 'block') {
      submenu.style.display = 'none';
      parentLi.classList.remove('open');
    } else {
      submenu.style.display = 'block';
      parentLi.classList.add('open');
    }
  });
});