package com.jaasielsilva.portalceo.repository.ajuda;

import com.jaasielsilva.portalceo.model.ajuda.AjudaEmbedding;
import com.jaasielsilva.portalceo.model.ajuda.AjudaConteudo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AjudaEmbeddingRepository extends JpaRepository<AjudaEmbedding, Long> {
    List<AjudaEmbedding> findByConteudoOrderByChunkIndexAsc(AjudaConteudo conteudo);
}
