package com.jaasielsilva.portalceo.repository.ti;

import com.jaasielsilva.portalceo.model.ti.BackupRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BackupRegistroRepository extends JpaRepository<BackupRegistro, Long> {
    @Query("select b from BackupRegistro b order by b.dataInicio desc")
    List<BackupRegistro> listarRecentes();
}