import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { CategoriasService, Categoria } from '../../services/categorias.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-categorias-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './categorias-list.component.html',
  styleUrls: ['./categorias-list.component.scss']
})
export class CategoriasListComponent implements OnInit, OnDestroy {
  categorias: Categoria[] = [];
  loading = false;
  error: string | null = null;
  private routerSubscription: Subscription = new Subscription();
  private categoriasSubscription: Subscription = new Subscription();

  constructor(
    private categoriasService: CategoriasService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.carregarCategorias();
    
    // Escuta mudanças de rota para recarregar dados quando voltar para esta página
    this.routerSubscription = this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        if (event.url === '/categorias') {
          this.carregarCategorias();
        }
      });

    // Escuta notificações de atualizações do serviço
    this.categoriasSubscription = this.categoriasService.categoriasAtualizadas$.subscribe(() => {
      this.carregarCategorias();
    });
  }

  ngOnDestroy(): void {
    this.routerSubscription.unsubscribe();
    this.categoriasSubscription.unsubscribe();
  }

  carregarCategorias(): void {
    this.loading = true;
    this.error = null;
    
    this.categoriasService.listarTodas().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erro ao carregar categorias';
        this.loading = false;
        console.error('Erro ao carregar categorias:', err);
      }
    });
  }

  deletarCategoria(id: number): void {
    if (confirm('Tem certeza que deseja deletar esta categoria?')) {
      this.categoriasService.deletar(id).subscribe({
        next: () => {
          // A lista será recarregada automaticamente via notificação do serviço
        },
        error: (err) => {
          this.error = 'Erro ao deletar categoria';
          console.error('Erro ao deletar categoria:', err);
        }
      });
    }
  }
}