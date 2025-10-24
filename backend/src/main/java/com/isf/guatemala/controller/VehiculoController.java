package com.isf.guatemala.controller;

import com.isf.guatemala.model.Vehiculo;
import com.isf.guatemala.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VehiculoController {
    
    @Autowired
    private VehiculoRepository vehiculoRepository;
    
    @GetMapping("/vehiculos")
    public List<Vehiculo> getVehiculos() {
        return vehiculoRepository.findAll();
    }
    
    @PostMapping("/vehiculos")
    public ResponseEntity<?> crearVehiculo(@RequestBody Vehiculo vehiculo) {
        Vehiculo saved = vehiculoRepository.save(vehiculo);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/vehiculos/{id}")
    public ResponseEntity<?> actualizarVehiculo(@PathVariable Long id, @RequestBody Vehiculo vehiculo) {
        Vehiculo existente = vehiculoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
        
        existente.setMarca(vehiculo.getMarca());
        existente.setModelo(vehiculo.getModelo());
        existente.setAno(vehiculo.getAno());
        existente.setColor(vehiculo.getColor());
        existente.setPlaca(vehiculo.getPlaca());
        existente.setTipoVehiculo(vehiculo.getTipoVehiculo());
        existente.setOdometroActual(vehiculo.getOdometroActual());
        existente.setTipoCombustible(vehiculo.getTipoCombustible());
        existente.setTransmision(vehiculo.getTransmision());
        existente.setUnidadMedida(vehiculo.getUnidadMedida());
        existente.setEstado(vehiculo.getEstado());
        
        Vehiculo updated = vehiculoRepository.save(existente);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/vehiculos/{id}")
    public ResponseEntity<?> eliminarVehiculo(@PathVariable Long id) {
        vehiculoRepository.deleteById(id);
        return ResponseEntity.ok("Vehículo eliminado");
    }
}
