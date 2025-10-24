package com.isf.guatemala.controller;

import com.isf.guatemala.model.Empleado;
import com.isf.guatemala.model.Puesto;
import com.isf.guatemala.repository.EmpleadoRepository;
import com.isf.guatemala.repository.PuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EmpleadoController {
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    @Autowired
    private PuestoRepository puestoRepository;
    
    @GetMapping("/empleados")
    public List<Empleado> getEmpleados() {
        return empleadoRepository.findAll();
    }
    
    @PostMapping("/empleados")
    public ResponseEntity<?> crearEmpleado(@RequestBody EmpleadoRequest request) {
        Puesto puesto = puestoRepository.findById(request.getPuestoId())
            .orElseThrow(() -> new RuntimeException("Puesto no encontrado"));
        
        Empleado empleado = new Empleado();
        empleado.setPrimerNombre(request.getPrimerNombre());
        empleado.setSegundoNombre(request.getSegundoNombre());
        empleado.setPrimerApellido(request.getPrimerApellido());
        empleado.setSegundoApellido(request.getSegundoApellido());
        empleado.setContacto(request.getContacto());
        empleado.setPuesto(puesto);
        
        Empleado saved = empleadoRepository.save(empleado);
        return ResponseEntity.ok(saved);
    }
    
    @PutMapping("/empleados/{id}")
    public ResponseEntity<?> actualizarEmpleado(@PathVariable Long id, @RequestBody EmpleadoRequest request) {
        Empleado existente = empleadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        Puesto puesto = puestoRepository.findById(request.getPuestoId())
            .orElseThrow(() -> new RuntimeException("Puesto no encontrado"));
        
        existente.setPrimerNombre(request.getPrimerNombre());
        existente.setSegundoNombre(request.getSegundoNombre());
        existente.setPrimerApellido(request.getPrimerApellido());
        existente.setSegundoApellido(request.getSegundoApellido());
        existente.setContacto(request.getContacto());
        existente.setPuesto(puesto);
        
        Empleado updated = empleadoRepository.save(existente);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/empleados/{id}")
    public ResponseEntity<?> eliminarEmpleado(@PathVariable Long id) {
        empleadoRepository.deleteById(id);
        return ResponseEntity.ok("Empleado eliminado");
    }
    
    static class EmpleadoRequest {
        private String primerNombre;
        private String segundoNombre;
        private String primerApellido;
        private String segundoApellido;
        private String contacto;
        private Long puestoId;
        
        public String getPrimerNombre() { return primerNombre; }
        public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
        public String getSegundoNombre() { return segundoNombre; }
        public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
        public String getPrimerApellido() { return primerApellido; }
        public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
        public String getSegundoApellido() { return segundoApellido; }
        public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
        public String getContacto() { return contacto; }
        public void setContacto(String contacto) { this.contacto = contacto; }
        public Long getPuestoId() { return puestoId; }
        public void setPuestoId(Long puestoId) { this.puestoId = puestoId; }
    }
}
