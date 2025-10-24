package com.isf.guatemala.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "viajes_iniciados")
public class ViajeIniciado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigoViaje;
    
    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;
    
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    
    @ManyToOne
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;
    
    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;
    
    private String destino;
    private LocalDate fechaInicio;
    private Double millajeSalida;
    
    @Enumerated(EnumType.STRING)
    private Estado estado;
    
    public enum Estado {
        ACTIVO, FINALIZADO
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCodigoViaje() { return codigoViaje; }
    public void setCodigoViaje(String codigoViaje) { this.codigoViaje = codigoViaje; }
    
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
    
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    
    public Oficina getOficina() { return oficina; }
    public void setOficina(Oficina oficina) { this.oficina = oficina; }
    
    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }
    
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public Double getMillajeSalida() { return millajeSalida; }
    public void setMillajeSalida(Double millajeSalida) { this.millajeSalida = millajeSalida; }
    
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
