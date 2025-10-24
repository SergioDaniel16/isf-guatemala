package com.isf.guatemala.controller;

import com.isf.guatemala.model.Puesto;
import com.isf.guatemala.repository.PuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PuestoController {
    
    @Autowired
    private PuestoRepository puestoRepository;
    
    @GetMapping("/puestos")
    public List<Puesto> getPuestos() {
        return puestoRepository.findAll();
    }
    
    @PostMapping("/puestos")
    public ResponseEntity<?> crearPuesto(@RequestBody Puesto puesto) {
        Puesto saved = puestoRepository.save(puesto);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/puestos/{id}")
    public ResponseEntity<?> actualizarPuesto(@PathVariable Long id, @RequestBody Puesto puesto) {
        Puesto existente = puestoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Puesto no encontrado"));
        
        existente.setNombre(puesto.getNombre());
        existente.setDetalle(puesto.getDetalle());
        existente.setSalarioPorHora(puesto.getSalarioPorHora());
        
        Puesto updated = puestoRepository.save(existente);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/puestos/{id}")
    public ResponseEntity<?> eliminarPuesto(@PathVariable Long id) {
        puestoRepository.deleteById(id);
        return ResponseEntity.ok("Puesto eliminado");
    }
}
