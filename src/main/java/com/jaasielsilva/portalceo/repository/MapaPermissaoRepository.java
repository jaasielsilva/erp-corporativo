package com.jaasielsilva.portalceo.repository;

import com.jaasielsilva.portalceo.model.MapaPermissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MapaPermissaoRepository extends JpaRepository<MapaPermissao, Long> {
    
    Optional<MapaPermissao> findByPermissao(String permissao);
    
    List<MapaPermissao> findByModulo(String modulo);
    
    @Query("SELECT DISTINCT m.modulo FROM MapaPermissao m ORDER BY m.modulo")
    List<String> findDistinctModulos();
}
