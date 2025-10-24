package com.isf.guatemala.repository;

import com.isf.guatemala.model.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface RegistroHorasRepository extends JpaRepository<RegistroHoras, Long> {
    
    @Query("SELECT r FROM RegistroHoras r WHERE r.proyecto.oficina.id = ?1")
    List<RegistroHoras> findByOficinaId(Long oficinaId);
    
    @Query("SELECT r FROM RegistroHoras r WHERE r.fecha BETWEEN ?1 AND ?2")
    List<RegistroHoras> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    @Query("SELECT r FROM RegistroHoras r WHERE r.proyecto.id = ?1")
    List<RegistroHoras> findByProyectoId(Long proyectoId);
    
    @Query("SELECT r FROM RegistroHoras r WHERE r.empleado.id = ?1")
    List<RegistroHoras> findByEmpleadoId(Long empleadoId);
}
