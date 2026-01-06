package com.jaasielsilva.portalceo.controller;

import com.jaasielsilva.portalceo.model.MapaPermissao;
import com.jaasielsilva.portalceo.service.MapaPermissaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mapa-permissoes")
public class MapaPermissaoController {

    @Autowired
    private MapaPermissaoService mapaPermissaoService;

    @GetMapping
    public ResponseEntity<List<MapaPermissao>> listarMapeamento() {
        // Força a sincronização para garantir que os dados estejam atualizados ao acessar
        mapaPermissaoService.sincronizarPermissoes();
        
        List<MapaPermissao> mapeamento = mapaPermissaoService.listarTodos();
        return ResponseEntity.ok(mapeamento);
    }
    
    @GetMapping("/verificar")
    public String verificarStatus() {
        mapaPermissaoService.sincronizarPermissoes();
        List<MapaPermissao> lista = mapaPermissaoService.listarTodos();
        
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Relatório de Mapa de Permissões</h1>");
        sb.append("<p>Total de permissões mapeadas: ").append(lista.size()).append("</p>");
        sb.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        sb.append("<tr style='background-color: #f2f2f2;'><th>ID</th><th>Módulo</th><th>Recurso</th><th>Tipo</th><th>Permissão</th><th>Descrição</th></tr>");
        
        for (MapaPermissao m : lista) {
            sb.append("<tr>");
            sb.append("<td>").append(m.getId()).append("</td>");
            sb.append("<td>").append(m.getModulo()).append("</td>");
            sb.append("<td>").append(m.getRecurso()).append("</td>");
            sb.append("<td>").append(m.getTipo()).append("</td>");
            sb.append("<td>").append(m.getPermissao()).append("</td>");
            sb.append("<td>").append(m.getDescricao()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        
        return sb.toString();
    }
}
