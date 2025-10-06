package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.Categoria;
import com.jaasielsilva.portalceo.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        List<Categoria> categorias = categoriaService.findAll();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.buscarPorId(id);
        if (categoria != null) {
            return ResponseEntity.ok(categoria);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Categoria> criar(@RequestBody Categoria categoria) {
        try {
            Categoria categoriaSalva = categoriaService.salvar(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        try {
            Categoria categoriaExistente = categoriaService.buscarPorId(id);
            if (categoriaExistente == null) {
                return ResponseEntity.notFound().build();
            }
            categoria.setId(id);
            Categoria categoriaAtualizada = categoriaService.salvar(categoria);
            return ResponseEntity.ok(categoriaAtualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            Categoria categoria = categoriaService.buscarPorId(id);
            if (categoria == null) {
                return ResponseEntity.notFound().build();
            }
            categoriaService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
