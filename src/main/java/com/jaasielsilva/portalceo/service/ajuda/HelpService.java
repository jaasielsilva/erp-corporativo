package com.jaasielsilva.portalceo.service.ajuda;

import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.ajuda.*;
import com.jaasielsilva.portalceo.repository.ajuda.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HelpService {
    @Autowired private AjudaCategoriaRepository categoriaRepo;
    @Autowired private AjudaConteudoRepository conteudoRepo;
    @Autowired private AjudaFeedbackRepository feedbackRepo;
    @Autowired private AjudaBuscaLogRepository buscaLogRepo;

    public List<AjudaCategoria> listarCategorias() {
        return categoriaRepo.findAll();
    }

    public Page<AjudaConteudo> buscar(String q, String categoriaSlug, int page, int size, Usuario usuario) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 50));
        Page<AjudaConteudo> result;
        if (categoriaSlug != null && !categoriaSlug.isBlank()) {
            Optional<AjudaCategoria> cat = categoriaRepo.findBySlug(categoriaSlug);
            result = cat.map(c -> conteudoRepo.findByPublicadoTrueAndCategoria(c, pageable))
                        .orElse(Page.empty(pageable));
        } else if (q != null && !q.isBlank()) {
            result = conteudoRepo.searchPublished(q, pageable);
        } else {
            result = conteudoRepo.findByPublicadoTrue(pageable);
        }
        AjudaBuscaLog log = new AjudaBuscaLog();
        log.setUsuario(usuario);
        log.setQuery(q == null ? "" : q);
        log.setResultados((int) result.getTotalElements());
        log.setResolveu(false);
        buscaLogRepo.save(log);
        return result;
    }

    public Optional<AjudaConteudo> obterConteudo(Long id) { return conteudoRepo.findById(id); }

    public Optional<AjudaConteudo> obterConteudoIncrementandoView(Long id) {
        return conteudoRepo.findById(id).map(c -> {
            c.setVisualizacoes((c.getVisualizacoes() == null ? 0 : c.getVisualizacoes()) + 1);
            return conteudoRepo.save(c);
        });
    }

    public AjudaFeedback registrarFeedback(Long conteudoId, Usuario usuario, boolean upvote, String comentario) {
        AjudaConteudo c = conteudoRepo.findById(conteudoId).orElseThrow();
        AjudaFeedback f = new AjudaFeedback();
        f.setConteudo(c);
        f.setUsuario(usuario);
        f.setUpvote(upvote);
        f.setComentario(comentario);
        return feedbackRepo.save(f);
    }
}
