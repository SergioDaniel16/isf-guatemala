package com.isf.guatemala.controller;

import com.isf.guatemala.model.Tarea;
import com.isf.guatemala.repository.TareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TareaController {
    
    @Autowired
    private TareaRepository tareaRepository;
    
    @GetMapping("/tareas")
    public List<Tarea> getTareas() {
        return tareaRepository.findAll();
    }
    
    @PostMapping("/tareas")
    public ResponseEntity<?> crearTarea(@RequestBody Tarea tarea) {
        Tarea saved = tareaRepository.save(tarea);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/tareas/{id}")
    public ResponseEntity<?> actualizarTarea(@PathVariable Long id, @RequestBody Tarea tarea) {
        Tarea existente = tareaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        existente.setNombre(tarea.getNombre());
        existente.setDefinicion(tarea.getDefinicion());
        existente.setCobrable(tarea.getCobrable());
        
        Tarea updated = tareaRepository.save(existente);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<?> eliminarTarea(@PathVariable Long id) {
        tareaRepository.deleteById(id);
        return ResponseEntity.ok("Tarea eliminada");
    }
}
