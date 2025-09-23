import { Routes } from '@angular/router';
import { CategoriasListComponent } from './categorias/categorias-list/categorias-list.component';
import { CategoriaFormComponent } from './categorias/categoria-form/categoria-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/categorias', pathMatch: 'full' },
  { path: 'categorias', component: CategoriasListComponent },
  { path: 'categorias/novo', component: CategoriaFormComponent },
  { path: 'categorias/:id/editar', component: CategoriaFormComponent }
];
