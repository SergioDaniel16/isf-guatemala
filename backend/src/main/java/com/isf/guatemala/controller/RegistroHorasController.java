package com.isf.guatemala.controller;

import com.isf.guatemala.model.*;
import com.isf.guatemala.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RegistroHorasController {
    
    @Autowired
    private RegistroHorasRepository registroHorasRepository;
    
    @Autowired
    private OficinaRepository oficinaRepository;
    
    @Autowired
    private ProyectoRepository proyectoRepository;
    
    @Autowired
    private TareaRepository tareaRepository;
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    @GetMapping("/registro-horas")
    public List<RegistroHoras> getRegistros(
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String fechaFin,
        @RequestParam(required = false) Long proyectoId,
        @RequestParam(required = false) Long empleadoId
    ) {
        if (fechaInicio != null && fechaFin != null) {
            return registroHorasRepository.findByFechaBetween(
                LocalDate.parse(fechaInicio), 
                LocalDate.parse(fechaFin)
            );
        } else if (proyectoId != null) {
            return registroHorasRepository.findByProyectoId(proyectoId);
        } else if (empleadoId != null) {
            return registroHorasRepository.findByEmpleadoId(empleadoId);
        }
        return registroHorasRepository.findAll();
    }
    
    @PostMapping("/registro-horas")
    public ResponseEntity<?> crearRegistro(@RequestBody RegistroRequest request) {
        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
            .orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
        
        Proyecto proyecto = proyectoRepository.findById(request.getProyectoId())
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        Tarea tarea = tareaRepository.findById(request.getTareaId())
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        RegistroHoras registro = new RegistroHoras();
        registro.setOficina(oficina);
        registro.setProyecto(proyecto);
        registro.setTarea(tarea);
        registro.setEmpleado(empleado);
        registro.setFecha(LocalDate.parse(request.getFecha()));
        registro.setHoras(request.getHoras());
        registro.setMinutos(request.getMinutos());
        
        RegistroHoras saved = registroHorasRepository.save(registro);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/registro-horas/{id}")
    public ResponseEntity<?> actualizarRegistro(@PathVariable Long id, @RequestBody RegistroRequest request) {
        RegistroHoras existente = registroHorasRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        
        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
            .orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
        
        Proyecto proyecto = proyectoRepository.findById(request.getProyectoId())
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        Tarea tarea = tareaRepository.findById(request.getTareaId())
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        
        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        existente.setOficina(oficina);
        existente.setProyecto(proyecto);
        existente.setTarea(tarea);
        existente.setEmpleado(empleado);
        existente.setFecha(LocalDate.parse(request.getFecha()));
        existente.setHoras(request.getHoras());
        existente.setMinutos(request.getMinutos());
        
        RegistroHoras updated = registroHorasRepository.save(existente);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/registro-horas/{id}")
    public ResponseEntity<?> eliminarRegistro(@PathVariable Long id) {
        registroHorasRepository.deleteById(id);
        return ResponseEntity.ok("Registro eliminado");
    }
    
    @GetMapping("/proyectos/por-oficina/{oficinaId}")
    public List<Proyecto> getProyectosPorOficina(@PathVariable Long oficinaId) {
        return proyectoRepository.findAll().stream()
            .filter(p -> p.getOficina() != null && p.getOficina().getId().equals(oficinaId))
            .toList();
    }
    
    static class RegistroRequest {
        private Long oficinaId;
        private Long proyectoId;
        private Long tareaId;
        private Long empleadoId;
        private String fecha;
        private Integer horas;
        private Integer minutos;
        
        public Long getOficinaId() { return oficinaId; }
        public void setOficinaId(Long oficinaId) { this.oficinaId = oficinaId; }
        public Long getProyectoId() { return proyectoId; }
        public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }
        public Long getTareaId() { return tareaId; }
        public void setTareaId(Long tareaId) { this.tareaId = tareaId; }
        public Long getEmpleadoId() { return empleadoId; }
        public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
        public Integer getHoras() { return horas; }
        public void setHoras(Integer horas) { this.horas = horas; }
        public Integer getMinutos() { return minutos; }
        public void setMinutos(Integer minutos) { this.minutos = minutos; }
    }
    @GetMapping("/horas/paginado")
    public ResponseEntity<?> getHorasPaginado(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) Long empleadoId,
        @RequestParam(required = false) String fecha
    ) {
        List<RegistroHoras> todas = registroHorasRepository.findAll();
        
        // Aplicar filtros
        List<RegistroHoras> filtradas = todas.stream()
            .filter(h -> empleadoId == null || h.getEmpleado().getId().equals(empleadoId))
            .filter(h -> fecha == null || h.getFecha().toString().equals(fecha))
            .sorted((a, b) -> b.getFecha().compareTo(a.getFecha())) // Más recientes primero
            .collect(java.util.stream.Collectors.toList());
        
        // Calcular paginación
        int total = filtradas.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int start = page * size;
        int end = Math.min(start + size, total);
        
        List<RegistroHoras> paginadas = filtradas.subList(start, end);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", paginadas);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalElements", total);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }
}
