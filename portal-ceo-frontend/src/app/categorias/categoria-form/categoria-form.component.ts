import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CategoriasService, Categoria } from '../../services/categorias.service';

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

  carregarCategoria(): void {
    if (this.categoriaId) {
      this.loading = true;
      this.categoriasService.buscarPorId(this.categoriaId).subscribe({
        next: (categoria) => {
          this.categoria = categoria;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Erro ao carregar categoria';
          this.loading = false;
          console.error('Erro ao carregar categoria:', err);
        }
      });
    }
  }

  salvar(): void {
    if (!this.categoria.nome.trim()) {
      this.error = 'Nome da categoria é obrigatório';
      return;
    }

    this.loading = true;
    this.error = null;

    const operacao = this.isEditMode 
      ? this.categoriasService.atualizar(this.categoriaId!, this.categoria)
      : this.categoriasService.criar(this.categoria);

    operacao.subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/categorias']);
      },
      error: (err) => {
        this.error = this.isEditMode ? 'Erro ao atualizar categoria' : 'Erro ao criar categoria';
        this.loading = false;
        console.error('Erro ao salvar categoria:', err);
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/categorias']);
  }
}