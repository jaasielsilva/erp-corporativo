import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface Categoria {
  id?: number;
  nome: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategoriasService {
  private apiUrl = 'http://localhost:8080/api/categorias';
  private categoriasAtualizadasSubject = new Subject<void>();

  // Observable que outros componentes podem escutar
  public categoriasAtualizadas$ = this.categoriasAtualizadasSubject.asObservable();

  constructor(private http: HttpClient) { }

  // GET /api/categorias - Listar todas as categorias
  listarTodas(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
  }

  // GET /api/categorias/{id} - Buscar categoria por ID
  buscarPorId(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.apiUrl}/${id}`);
  }

  // POST /api/categorias - Criar nova categoria
  criar(categoria: Categoria): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, categoria).pipe(
      tap(() => this.notificarAtualizacao())
    );
  }

  // PUT /api/categorias/{id} - Atualizar categoria
  atualizar(id: number, categoria: Categoria): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/${id}`, categoria).pipe(
      tap(() => this.notificarAtualizacao())
    );
  }

  // DELETE /api/categorias/{id} - Deletar categoria
  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.notificarAtualizacao())
    );
  }

  // Método privado para notificar atualizações
  private notificarAtualizacao(): void {
    this.categoriasAtualizadasSubject.next();
  }
}