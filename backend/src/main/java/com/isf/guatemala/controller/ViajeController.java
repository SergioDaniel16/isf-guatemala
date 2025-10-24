package com.isf.guatemala.controller;

import com.isf.guatemala.model.*;
import com.isf.guatemala.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ViajeController {
    
    @Autowired
    private ViajeIniciadoRepository viajeIniciadoRepository;
    
    @Autowired
    private ViajeFinalizadoRepository viajeFinalizadoRepository;
    
    @Autowired
    private VehiculoRepository vehiculoRepository;
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    @Autowired
    private OficinaRepository oficinaRepository;
    
    @Autowired
    private ProyectoRepository proyectoRepository;
    
    private static final String UPLOAD_DIR = "/var/www/isf-guatemala/uploads/kilometraje/";
    private static final double KM_POR_MILLA = 1.60934;
    
    @PostMapping("/viajes/iniciar")
    public ResponseEntity<?> iniciarViaje(@RequestBody IniciarViajeRequest request) {
        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
            .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));
        
        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        
        // Validar que el vehículo NO tenga viajes activos
        List<ViajeIniciado> viajesActivosVehiculo = viajeIniciadoRepository.findViajesActivos()
            .stream()
            .filter(v -> v.getVehiculo().getId().equals(request.getVehiculoId()))
            .collect(java.util.stream.Collectors.toList());
        
        if (!viajesActivosVehiculo.isEmpty()) {
            return ResponseEntity.badRequest().body(
                "El vehículo " + vehiculo.getPlaca() + " ya tiene un viaje activo. Código: " + 
                viajesActivosVehiculo.get(0).getCodigoViaje()
            );
        }
        
        // Validar que el conductor NO tenga viajes activos
        List<ViajeIniciado> viajesActivosConductor = viajeIniciadoRepository.findViajesActivos()
            .stream()
            .filter(v -> v.getEmpleado().getId().equals(request.getEmpleadoId()))
            .collect(java.util.stream.Collectors.toList());
        
        if (!viajesActivosConductor.isEmpty()) {
            return ResponseEntity.badRequest().body(
                "El conductor " + empleado.getPrimerNombre() + " " + empleado.getPrimerApellido() + 
                " ya tiene un viaje activo. Código: " + viajesActivosConductor.get(0).getCodigoViaje()
            );
        }
        
        Oficina oficina = oficinaRepository.findById(request.getOficinaId())
            .orElseThrow(() -> new RuntimeException("Oficina no encontrada"));
        
        Proyecto proyecto = proyectoRepository.findById(request.getProyectoId())
            .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        
        ViajeIniciado viaje = new ViajeIniciado();
        viaje.setCodigoViaje(generarCodigoViaje());
        viaje.setVehiculo(vehiculo);
        viaje.setEmpleado(empleado);
        viaje.setOficina(oficina);
        viaje.setProyecto(proyecto);
        viaje.setDestino(request.getDestino());
        viaje.setFechaInicio(LocalDate.parse(request.getFechaInicio()));
        viaje.setMillajeSalida(request.getMillajeSalida());
        viaje.setEstado(ViajeIniciado.Estado.ACTIVO);
        
        ViajeIniciado saved = viajeIniciadoRepository.save(viaje);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/viajes/activos")
    public List<ViajeIniciado> getViajesActivos() {
        return viajeIniciadoRepository.findViajesActivos();
    }
    
    @PostMapping("/viajes/finalizar")
    public ResponseEntity<?> finalizarViaje(
        @RequestParam("viajeId") Long viajeId,
        @RequestParam("millajeEntrada") Double millajeEntrada,
        @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        ViajeIniciado viajeIniciado = viajeIniciadoRepository.findById(viajeId)
            .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        
        Vehiculo vehiculo = viajeIniciado.getVehiculo();
        
        // Validaciones
        if (millajeEntrada < viajeIniciado.getMillajeSalida()) {
            return ResponseEntity.badRequest().body("El millaje de entrada no puede ser menor al millaje de salida");
        }
        
        double diferencia = millajeEntrada - viajeIniciado.getMillajeSalida();
        if (diferencia > 500) {
            return ResponseEntity.badRequest().body("La diferencia de millaje es mayor a 500. Verifica los datos.");
        }
        
        // Guardar foto
        String rutaFoto = null;
        if (foto != null && !foto.isEmpty()) {
            rutaFoto = guardarFoto(foto);
        }
        
        // Calcular millas y km recorridos
        double millasRecorridas = millajeEntrada - viajeIniciado.getMillajeSalida();
        double kmRecorridos;
        
        if (vehiculo.getUnidadMedida() == Vehiculo.UnidadMedida.MILLAS) {
            kmRecorridos = millasRecorridas * KM_POR_MILLA;
        } else {
            kmRecorridos = millasRecorridas;
            millasRecorridas = kmRecorridos / KM_POR_MILLA;
        }
        
        // Calcular costo según lógica de negocio
        // Calcular km extra y costo
        Proyecto proyecto = viajeIniciado.getProyecto();
        double tarifaBase = proyecto.getTarifaBaseVehiculo();
        double tarifaKmExtra = proyecto.getTarifaKmExtra();
        double cambioDolar = proyecto.getCambioDolar();
        
        double kmExtra = 0;
        double costoUSD = 0;
        
        if (kmRecorridos <= 50) {
            costoUSD = tarifaBase;
        } else {
            kmExtra = kmRecorridos - 50;
            costoUSD = tarifaBase + (kmExtra * tarifaKmExtra);
        }
        
        double costoGTQ = costoUSD * cambioDolar;
        
        // Crear viaje finalizado
        ViajeFinalizado viajeFinalizado = new ViajeFinalizado();
        viajeFinalizado.setViajeIniciado(viajeIniciado);
        viajeFinalizado.setRutaFoto(rutaFoto);
        viajeFinalizado.setMillajeEntrada(millajeEntrada);
        viajeFinalizado.setFechaFin(LocalDate.now());
        viajeFinalizado.setMillasRecorridas(millasRecorridas);
        viajeFinalizado.setKmRecorridos(kmRecorridos);
        viajeFinalizado.setKmExtra(kmExtra);
        viajeFinalizado.setCostoUSD(costoUSD);
        viajeFinalizado.setCostoGTQ(costoGTQ);
        
        viajeFinalizadoRepository.save(viajeFinalizado);
        
        // Actualizar estado y odómetro del vehículo
        viajeIniciado.setEstado(ViajeIniciado.Estado.FINALIZADO);
        viajeIniciadoRepository.save(viajeIniciado);
        
        vehiculo.setOdometroActual(millajeEntrada);
        vehiculoRepository.save(vehiculo);
        
        return ResponseEntity.ok(viajeFinalizado);
    }
    
    @GetMapping("/viajes/finalizados")
    public List<ViajeFinalizado> getViajesFinalizados() {
        return viajeFinalizadoRepository.findAll();
    }
    
    private String generarCodigoViaje() {
        return "V-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String guardarFoto(MultipartFile foto) {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            String fileName = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, foto.getBytes());
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
        }
    }
    
    static class IniciarViajeRequest {
        private Long vehiculoId;
        private Long empleadoId;
        private Long oficinaId;
        private Long proyectoId;
        private String destino;
        private String fechaInicio;
        private Double millajeSalida;
        
        public Long getVehiculoId() { return vehiculoId; }
        public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
        public Long getEmpleadoId() { return empleadoId; }
        public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
        public Long getOficinaId() { return oficinaId; }
        public void setOficinaId(Long oficinaId) { this.oficinaId = oficinaId; }
        public Long getProyectoId() { return proyectoId; }
        public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }
        public String getDestino() { return destino; }
        public void setDestino(String destino) { this.destino = destino; }
        public String getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
        public Double getMillajeSalida() { return millajeSalida; }
        public void setMillajeSalida(Double millajeSalida) { this.millajeSalida = millajeSalida; }
    }

    @PutMapping("/viajes/finalizado/{id}")
    public ResponseEntity<?> editarViajeFinalizado(
        @PathVariable Long id,
        @RequestParam("millajeEntrada") Double millajeEntrada,
        @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        ViajeFinalizado viajeFinalizado = viajeFinalizadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Viaje finalizado no encontrado"));
        
        ViajeIniciado viajeIniciado = viajeFinalizado.getViajeIniciado();
        Vehiculo vehiculo = viajeIniciado.getVehiculo();
        
        // Validaciones
        if (millajeEntrada < viajeIniciado.getMillajeSalida()) {
            return ResponseEntity.badRequest().body("El millaje de entrada no puede ser menor al millaje de salida");
        }
        
        double diferencia = millajeEntrada - viajeIniciado.getMillajeSalida();
        if (diferencia > 500) {
            return ResponseEntity.badRequest().body("La diferencia de millaje es mayor a 500. Verifica los datos.");
        }
        
        // Si hay nueva foto, eliminar la anterior y guardar la nueva
        if (foto != null && !foto.isEmpty()) {
            // Eliminar foto anterior
            if (viajeFinalizado.getRutaFoto() != null) {
                eliminarFoto(viajeFinalizado.getRutaFoto());
            }
            // Guardar nueva foto
            String nuevaRutaFoto = guardarFoto(foto);
            viajeFinalizado.setRutaFoto(nuevaRutaFoto);
        }
        
        // Recalcular millas y km recorridos
        double millasRecorridas = millajeEntrada - viajeIniciado.getMillajeSalida();
        double kmRecorridos;
        
        if (vehiculo.getUnidadMedida() == Vehiculo.UnidadMedida.MILLAS) {
            kmRecorridos = millasRecorridas * KM_POR_MILLA;
        } else {
            kmRecorridos = millasRecorridas;
            millasRecorridas = kmRecorridos / KM_POR_MILLA;
        }
        
        // Recalcular km extra y costo
        Proyecto proyecto = viajeIniciado.getProyecto();
        double tarifaBase = proyecto.getTarifaBaseVehiculo();
        double tarifaKmExtra = proyecto.getTarifaKmExtra();
        double cambioDolar = proyecto.getCambioDolar();
        
        double kmExtra = 0;
        double costoUSD = 0;
        
        if (kmRecorridos <= 50) {
            costoUSD = tarifaBase;
        } else {
            kmExtra = kmRecorridos - 50;
            costoUSD = tarifaBase + (kmExtra * tarifaKmExtra);
        }
        
        double costoGTQ = costoUSD * cambioDolar;
        
        // Actualizar viaje finalizado
        viajeFinalizado.setMillajeEntrada(millajeEntrada);
        viajeFinalizado.setMillasRecorridas(millasRecorridas);
        viajeFinalizado.setKmRecorridos(kmRecorridos);
        viajeFinalizado.setKmExtra(kmExtra);
        viajeFinalizado.setCostoUSD(costoUSD);
        viajeFinalizado.setCostoGTQ(costoGTQ);
        
        viajeFinalizadoRepository.save(viajeFinalizado);
        
        // Actualizar odómetro del vehículo
        vehiculo.setOdometroActual(millajeEntrada);
        vehiculoRepository.save(vehiculo);
        
        return ResponseEntity.ok(viajeFinalizado);
    }
    
    @DeleteMapping("/viajes/{id}")
    public ResponseEntity<?> eliminarViaje(@PathVariable Long id) {
        ViajeIniciado viaje = viajeIniciadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
        
        // Si está finalizado, eliminar foto y registro finalizado
        if (viaje.getEstado() == ViajeIniciado.Estado.FINALIZADO) {
            viajeFinalizadoRepository.findAll().stream()
                .filter(vf -> vf.getViajeIniciado().getId().equals(id))
                .findFirst()
                .ifPresent(viajeFinalizado -> {
                    // Eliminar foto del servidor
                    if (viajeFinalizado.getRutaFoto() != null) {
                        eliminarFoto(viajeFinalizado.getRutaFoto());
                    }
                    viajeFinalizadoRepository.delete(viajeFinalizado);
                });
        }
        
        viajeIniciadoRepository.delete(viaje);
        return ResponseEntity.ok("Viaje eliminado");
    }
    
    private void eliminarFoto(String nombreArchivo) {
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(UPLOAD_DIR + nombreArchivo);
            java.nio.file.Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.err.println("Error al eliminar foto: " + e.getMessage());
        }
    }
    
    @GetMapping("/viajes/todos")
    public List<ViajeIniciado> getTodosLosViajes() {
        return viajeIniciadoRepository.findAll();
    }

    @GetMapping("/viajes/reporte")
    public ResponseEntity<?> generarReporte(
        @RequestParam String fechaInicio,
        @RequestParam String fechaFin,
        @RequestParam(required = false) Long proyectoId,
        @RequestParam(required = false) Long vehiculoId,
        @RequestParam(required = false) Long oficinaId
    ) {
        List<ViajeIniciado> todosViajes = viajeIniciadoRepository.findAll();
        
        // Filtrar por fecha y estado FINALIZADO
        List<ViajeIniciado> viajesFiltrados = todosViajes.stream()
            .filter(v -> v.getEstado() == ViajeIniciado.Estado.FINALIZADO)
            .filter(v -> {
                LocalDate fecha = v.getFechaInicio();
                LocalDate inicio = LocalDate.parse(fechaInicio);
                LocalDate fin = LocalDate.parse(fechaFin);
                return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
            })
            .filter(v -> proyectoId == null || v.getProyecto().getId().equals(proyectoId))
            .filter(v -> vehiculoId == null || v.getVehiculo().getId().equals(vehiculoId))
            .filter(v -> oficinaId == null || v.getOficina().getId().equals(oficinaId))
            .collect(java.util.stream.Collectors.toList());
        
        // Cargar datos finalizados
        List<ViajeFinalizado> finalizados = viajeFinalizadoRepository.findAll();
        
        Map<String, Object> reporte = new java.util.HashMap<>();
        
        // REPORTE POR PROYECTO
        Map<String, Map<String, Object>> porProyecto = new java.util.LinkedHashMap<>();
        
        for (ViajeIniciado viaje : viajesFiltrados) {
            String proyectoNombre = viaje.getProyecto().getNombre();
            String vehiculoPlaca = viaje.getVehiculo().getPlaca();
            String key = proyectoNombre + "_" + vehiculoPlaca;
            
            ViajeFinalizado finalizado = finalizados.stream()
                .filter(vf -> vf.getViajeIniciado().getId().equals(viaje.getId()))
                .findFirst()
                .orElse(null);
            
            if (finalizado == null) continue;
            
            if (!porProyecto.containsKey(proyectoNombre)) {
                porProyecto.put(proyectoNombre, new java.util.HashMap<>());
            }
            
            Map<String, Object> proyecto = porProyecto.get(proyectoNombre);
            
            if (!proyecto.containsKey("vehiculos")) {
                proyecto.put("vehiculos", new java.util.LinkedHashMap<>());
                proyecto.put("tarifaBase", viaje.getProyecto().getTarifaBaseVehiculo());
                proyecto.put("tarifaKmExtra", viaje.getProyecto().getTarifaKmExtra());
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> vehiculos = (Map<String, Object>) proyecto.get("vehiculos");
            
            if (!vehiculos.containsKey(vehiculoPlaca)) {
                Map<String, Object> vehiculoData = new java.util.HashMap<>();
                vehiculoData.put("diasBase", 0);
                vehiculoData.put("kmExtra", 0.0);
                vehiculoData.put("costoUSD", 0.0);
                vehiculoData.put("costoGTQ", 0.0);
                vehiculos.put(vehiculoPlaca, vehiculoData);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> vehiculoData = (Map<String, Object>) vehiculos.get(vehiculoPlaca);
            
            vehiculoData.put("diasBase", (int) vehiculoData.get("diasBase") + 1);
            vehiculoData.put("kmExtra", (double) vehiculoData.get("kmExtra") + finalizado.getKmExtra());
            vehiculoData.put("costoUSD", (double) vehiculoData.get("costoUSD") + finalizado.getCostoUSD());
            vehiculoData.put("costoGTQ", (double) vehiculoData.get("costoGTQ") + finalizado.getCostoGTQ());
        }
        
        // REPORTE POR VEHÍCULO
        Map<String, Object> porVehiculo = new java.util.LinkedHashMap<>();
        
        for (ViajeIniciado viaje : viajesFiltrados) {
            String vehiculoKey = viaje.getVehiculo().getPlaca() + " - " + 
                                viaje.getVehiculo().getMarca() + " " + 
                                viaje.getVehiculo().getModelo();
            
            if (!porVehiculo.containsKey(vehiculoKey)) {
                Map<String, Object> vehiculoData = new java.util.HashMap<>();
                vehiculoData.put("millageInicial", Double.MAX_VALUE);
                vehiculoData.put("millageFinal", 0.0);
                vehiculoData.put("totalRecorrido", 0.0);
                porVehiculo.put(vehiculoKey, vehiculoData);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> vehiculoData = (Map<String, Object>) porVehiculo.get(vehiculoKey);
            
            ViajeFinalizado finalizado = finalizados.stream()
                .filter(vf -> vf.getViajeIniciado().getId().equals(viaje.getId()))
                .findFirst()
                .orElse(null);
            
            if (finalizado != null) {
                double millageInicial = Math.min((double) vehiculoData.get("millageInicial"), viaje.getMillajeSalida());
                double millageFinal = Math.max((double) vehiculoData.get("millageFinal"), finalizado.getMillajeEntrada());
                
                vehiculoData.put("millageInicial", millageInicial);
                vehiculoData.put("millageFinal", millageFinal);
                vehiculoData.put("totalRecorrido", millageFinal - millageInicial);
            }
        }
        
        reporte.put("porProyecto", porProyecto);
        reporte.put("porVehiculo", porVehiculo);
        
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/vehiculos/disponibles")
    public List<Vehiculo> getVehiculosDisponibles() {
        List<ViajeIniciado> viajesActivos = viajeIniciadoRepository.findViajesActivos();
        List<Long> vehiculosOcupados = viajesActivos.stream()
            .map(v -> v.getVehiculo().getId())
            .collect(java.util.stream.Collectors.toList());
        
        return vehiculoRepository.findAll().stream()
            .filter(v -> v.getEstado() == Vehiculo.Estado.ACTIVO)
            .filter(v -> !vehiculosOcupados.contains(v.getId()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    @GetMapping("/empleados/disponibles")
    public List<Empleado> getEmpleadosDisponibles() {
        List<ViajeIniciado> viajesActivos = viajeIniciadoRepository.findViajesActivos();
        List<Long> empleadosOcupados = viajesActivos.stream()
            .map(v -> v.getEmpleado().getId())
            .collect(java.util.stream.Collectors.toList());
        
        return empleadoRepository.findAll().stream()
            .filter(e -> !empleadosOcupados.contains(e.getId()))
            .collect(java.util.stream.Collectors.toList());
    }
    @GetMapping("/viajes/paginado")
    public ResponseEntity<?> getViajesPaginado(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) Long vehiculoId,
        @RequestParam(required = false) Long conductorId,
        @RequestParam(required = false) String fecha
    ) {
        List<ViajeIniciado> todos = viajeIniciadoRepository.findAll();
        
        // Solo viajes finalizados
        List<ViajeIniciado> finalizados = todos.stream()
            .filter(v -> v.getEstado() == ViajeIniciado.Estado.FINALIZADO)
            .collect(java.util.stream.Collectors.toList());
        
        // Cargar datos finalizados
        List<ViajeFinalizado> todosFinalizados = viajeFinalizadoRepository.findAll();
        
        // Aplicar filtros
        List<ViajeIniciado> filtrados = finalizados.stream()
            .filter(v -> vehiculoId == null || v.getVehiculo().getId().equals(vehiculoId))
            .filter(v -> conductorId == null || v.getEmpleado().getId().equals(conductorId))
            .filter(v -> fecha == null || v.getFechaInicio().toString().equals(fecha))
            .sorted((a, b) -> b.getFechaInicio().compareTo(a.getFechaInicio())) // Más recientes primero
            .collect(java.util.stream.Collectors.toList());
        
        // Calcular paginación
        int total = filtrados.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int start = page * size;
        int end = Math.min(start + size, total);
        
        List<ViajeIniciado> paginados = filtrados.subList(start, end);
        
        // Enriquecer con datos finalizados
        List<Map<String, Object>> content = new java.util.ArrayList<>();
        for (ViajeIniciado viaje : paginados) {
            ViajeFinalizado finalizado = todosFinalizados.stream()
                .filter(vf -> vf.getViajeIniciado().getId().equals(viaje.getId()))
                .findFirst()
                .orElse(null);
            
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("viaje", viaje);
            item.put("finalizado", finalizado);
            content.add(item);
        }
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", content);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalElements", total);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }
}
