package com.jaasielsilva.portalceo.juridico.previdenciario.documentos.repository;

import com.jaasielsilva.portalceo.juridico.previdenciario.documentos.entity.DocumentoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface DocumentoProcessoRepository extends JpaRepository<DocumentoProcesso, Long> {
    @EntityGraph(attributePaths = { "enviadoPor" })
    List<DocumentoProcesso> findByProcessoPrevidenciario_IdOrderByDataUploadDesc(Long processoId);
}
