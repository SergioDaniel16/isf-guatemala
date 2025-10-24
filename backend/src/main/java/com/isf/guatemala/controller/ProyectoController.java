package com.isf.guatemala.controller;

import com.isf.guatemala.model.Oficina;
import com.isf.guatemala.model.Proyecto;
import com.isf.guatemala.repository.OficinaRepository;
import com.isf.guatemala.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProyectoController {
    
    @Autowired
    private ProyectoRepository proyectoRepository;
    
    @Autowired
    private OficinaRepository oficinaRepository;
    
    @GetMapping("/oficinas")
    public List<Oficina> getOficinas() {
        return oficinaRepository.findAll();
    }
    
@PostMapping("/proyectos")
    public ResponseEntity<?> crearProyecto(@RequestBody ProyectoRequest request) {
        // Buscar la oficina completa
        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
            .orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
        
        // Generar código automático
        String inicialOficina = oficina.getNombre().substring(0, 1).toUpperCase();
        
        Proyecto ultimoProyecto = proyectoRepository.findTopByOficinaOrderByCodigoDesc(oficina.getId());
        int numeroSiguiente = 1;
        
        if (ultimoProyecto != null) {
            String ultimoCodigo = ultimoProyecto.getCodigo();
            String numeroStr = ultimoCodigo.substring(1);
            numeroSiguiente = Integer.parseInt(numeroStr) + 1;
        }
        
        String codigo = inicialOficina + String.format("%04d", numeroSiguiente);
        
        // Crear el proyecto
        Proyecto proyecto = new Proyecto();
        proyecto.setCodigo(codigo);
        proyecto.setNombre(request.getNombre());
        proyecto.setOficina(oficina);
        proyecto.setDepartamento(request.getDepartamento());
        proyecto.setMunicipio(request.getMunicipio());
        proyecto.setTipo(Proyecto.TipoProyecto.valueOf(request.getTipo()));
        proyecto.setCambioDolar(request.getCambioDolar());
        proyecto.setTarifaBaseVehiculo(request.getTarifaBaseVehiculo());
        proyecto.setTarifaKmExtra(request.getTarifaKmExtra());
        
        Proyecto saved = proyectoRepository.save(proyecto);
        return ResponseEntity.ok(saved);
    }
    
    static class ProyectoRequest {
        private String nombre;
        private Long oficinaId;
        private String departamento;
        private String municipio;
        private String tipo;
        private Double cambioDolar;
        private Double tarifaBaseVehiculo;
        private Double tarifaKmExtra;
        
        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Long getOficinaId() { return oficinaId; }
        public void setOficinaId(Long oficinaId) { this.oficinaId = oficinaId; }
        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }
        public String getMunicipio() { return municipio; }
        public void setMunicipio(String municipio) { this.municipio = municipio; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Double getCambioDolar() { return cambioDolar; }
        public void setCambioDolar(Double cambioDolar) { this.cambioDolar = cambioDolar; }
        public Double getTarifaBaseVehiculo() { return tarifaBaseVehiculo; }
        public void setTarifaBaseVehiculo(Double tarifaBaseVehiculo) { this.tarifaBaseVehiculo = tarifaBaseVehiculo; }
        public Double getTarifaKmExtra() { return tarifaKmExtra; }
        public void setTarifaKmExtra(Double tarifaKmExtra) { this.tarifaKmExtra = tarifaKmExtra; }
    }
    
    @GetMapping("/proyectos")
    public List<Proyecto> getProyectos() {
        return proyectoRepository.findAll();
    }
    
    @PutMapping("/proyectos/{id}")
    public ResponseEntity<?> actualizarProyecto(@PathVariable Long id, @RequestBody ProyectoRequest request) {
        Proyecto proyectoExistente = proyectoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        // Buscar la oficina completa
        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
            .orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
        
        // Actualizar campos pero MANTENER el código original
        proyectoExistente.setNombre(request.getNombre());
        proyectoExistente.setOficina(oficina);
        proyectoExistente.setDepartamento(request.getDepartamento());
        proyectoExistente.setMunicipio(request.getMunicipio());
        proyectoExistente.setTipo(Proyecto.TipoProyecto.valueOf(request.getTipo()));
        proyectoExistente.setCambioDolar(request.getCambioDolar());
        proyectoExistente.setTarifaBaseVehiculo(request.getTarifaBaseVehiculo());
        proyectoExistente.setTarifaKmExtra(request.getTarifaKmExtra());
        
        Proyecto updated = proyectoRepository.save(proyectoExistente);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<?> eliminarProyecto(@PathVariable Long id) {
        proyectoRepository.deleteById(id);
        return ResponseEntity.ok("Proyecto eliminado");
    }
}
