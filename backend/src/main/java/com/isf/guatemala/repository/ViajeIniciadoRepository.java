package com.isf.guatemala.repository;

import com.isf.guatemala.model.ViajeIniciado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ViajeIniciadoRepository extends JpaRepository<ViajeIniciado, Long> {
    
    @Query("SELECT v FROM ViajeIniciado v WHERE v.estado = 'ACTIVO'")
    List<ViajeIniciado> findViajesActivos();
    
    ViajeIniciado findByCodigoViaje(String codigoViaje);
}
