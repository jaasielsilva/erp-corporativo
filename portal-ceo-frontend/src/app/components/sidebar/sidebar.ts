import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss']
})
export class SidebarComponent implements OnInit {

  constructor(@Inject(PLATFORM_ID) private platformId: Object) { }

  ngOnInit(): void {
    this.initializeSubmenuToggle();
  }

  private initializeSubmenuToggle(): void {
    // Verifica se estamos no browser antes de acessar o document
    if (isPlatformBrowser(this.platformId)) {
      // Aguarda o DOM estar pronto
      setTimeout(() => {
        const submenuToggles = document.querySelectorAll('.submenu-toggle');
      
        submenuToggles.forEach(toggle => {
          toggle.addEventListener('click', (e) => {
            e.preventDefault();
            const parentLi = (e.target as HTMLElement).closest('li');
            const submenu = parentLi?.querySelector('.submenu') as HTMLElement;
            
            if (submenu) {
              // Toggle do submenu atual
              const isOpen = submenu.style.display === 'block';
              submenu.style.display = isOpen ? 'none' : 'block';
              
              // Adiciona/remove classe para rotação do ícone
              parentLi?.classList.toggle('open', !isOpen);
            }
          });
        });
      }, 100);
    }
  }
}
