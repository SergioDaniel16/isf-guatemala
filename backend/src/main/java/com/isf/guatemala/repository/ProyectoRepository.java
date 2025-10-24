package com.isf.guatemala.repository;

import com.isf.guatemala.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    
    @Query("SELECT p FROM Proyecto p WHERE p.oficina.id = ?1 ORDER BY p.codigo DESC")
    Proyecto findTopByOficinaOrderByCodigoDesc(Long oficinaId);
}

