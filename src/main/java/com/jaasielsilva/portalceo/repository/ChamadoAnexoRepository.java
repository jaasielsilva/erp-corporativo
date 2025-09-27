package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.suporte.ChamadoAnexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoAnexoRepository extends JpaRepository<ChamadoAnexo, Long> {

    /**
     * Busca anexos ativos por chamado
     */
    @Query("SELECT a FROM ChamadoAnexo a WHERE a.chamado.id = :chamadoId AND a.ativo = true ORDER BY a.dataUpload DESC")
    List<ChamadoAnexo> findByChamadoIdAndAtivoTrue(@Param("chamadoId") Long chamadoId);

    /**
     * Busca todos os anexos por chamado (incluindo inativos)
     */
    @Query("SELECT a FROM ChamadoAnexo a WHERE a.chamado.id = :chamadoId ORDER BY a.dataUpload DESC")
    List<ChamadoAnexo> findByChamadoId(@Param("chamadoId") Long chamadoId);

    /**
     * Conta anexos ativos por chamado
     */
    @Query("SELECT COUNT(a) FROM ChamadoAnexo a WHERE a.chamado.id = :chamadoId AND a.ativo = true")
    Long countByChamadoIdAndAtivoTrue(@Param("chamadoId") Long chamadoId);

    /**
     * Busca anexos por tipo MIME
     */
    @Query("SELECT a FROM ChamadoAnexo a WHERE a.tipoMime LIKE :tipoMime AND a.ativo = true")
    List<ChamadoAnexo> findByTipoMimeLikeAndAtivoTrue(@Param("tipoMime") String tipoMime);

    /**
     * Busca anexos por usuário
     */
    @Query("SELECT a FROM ChamadoAnexo a WHERE a.usuario.id = :usuarioId AND a.ativo = true ORDER BY a.dataUpload DESC")
    List<ChamadoAnexo> findByUsuarioIdAndAtivoTrue(@Param("usuarioId") Long usuarioId);
}