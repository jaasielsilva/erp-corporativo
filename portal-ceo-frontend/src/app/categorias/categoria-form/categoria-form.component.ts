import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CategoriasService, Categoria } from '../../services/categorias.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-categoria-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categoria-form.component.html',
  styleUrls: ['./categoria-form.component.scss']
})
export class CategoriaFormComponent implements OnInit {
  categoria: Categoria = { nome: '' };
  isEditMode = false;
  loading = false;
  error: string | null = null;
  categoriaId: number | null = null;
  showNavigationOptions = false;
  useAsyncAwait = true; // Flag para alternar entre subscribe e async/await

  constructor(
    private categoriasService: CategoriasService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.categoriaId = +params['id'];
        this.isEditMode = true;
        this.carregarCategoria();
      }
    });
  }

  // Versão com async/await
  async carregarCategoria(): Promise<void> {
    if (!this.categoriaId) return;

    try {
      this.loading = true;
      this.error = null;
      
      if (this.useAsyncAwait) {
        // Usando async/await
        this.categoria = await firstValueFrom(this.categoriasService.buscarPorId(this.categoriaId));
      } else {
        // Versão com subscribe (mantida como alternativa)
        this.categoriasService.buscarPorId(this.categoriaId).subscribe({
          next: (categoria) => {
            this.categoria = categoria;
          },
          error: (err) => {
            throw err;
          }
        });
      }
    } catch (err) {
      this.error = 'Erro ao carregar categoria';
      console.error('Erro ao carregar categoria:', err);
    } finally {
      this.loading = false;
    }
  }

  // Versão com async/await e navegação opcional
  async salvar(): Promise<void> {
    if (!this.categoria.nome.trim()) {
      this.error = 'Nome da categoria é obrigatório';
      return;
    }

    try {
      this.loading = true;
      this.error = null;

      if (this.useAsyncAwait) {
        // Usando async/await
        if (this.isEditMode) {
          await firstValueFrom(this.categoriasService.atualizar(this.categoriaId!, this.categoria));
        } else {
          await firstValueFrom(this.categoriasService.criar(this.categoria));
        }
      } else {
        // Versão com subscribe (mantida como alternativa)
        const operacao = this.isEditMode 
          ? this.categoriasService.atualizar(this.categoriaId!, this.categoria)
          : this.categoriasService.criar(this.categoria);

        await firstValueFrom(operacao);
      }

      // Sucesso - mostrar opções de navegação
      this.showNavigationOptions = true;
      
    } catch (err) {
      this.error = this.isEditMode ? 'Erro ao atualizar categoria' : 'Erro ao criar categoria';
      console.error('Erro ao salvar categoria:', err);
    } finally {
      this.loading = false;
    }
  }

  // Navegar de volta para a lista
  voltarParaLista(): void {
    this.router.navigate(['/categorias']);
  }

  // Criar nova categoria
  criarNova(): void {
    this.categoria = { nome: '' };
    this.showNavigationOptions = false;
    this.error = null;
    this.isEditMode = false;
    this.categoriaId = null;
  }

  // Permanecer no formulário
  permanecerNoFormulario(): void {
    this.showNavigationOptions = false;
  }

  cancelar(): void {
    this.router.navigate(['/categorias']);
  }
}