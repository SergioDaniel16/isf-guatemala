package com.isf.guatemala.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_horas")
public class RegistroHoras {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;
    
    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;
    
    @ManyToOne
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;
    
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    
    private LocalDate fecha;
    private Integer horas;
    private Integer minutos;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Oficina getOficina() { return oficina; }
    public void setOficina(Oficina oficina) { this.oficina = oficina; }
    
    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }
    
    public Tarea getTarea() { return tarea; }
    public void setTarea(Tarea tarea) { this.tarea = tarea; }
    
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public Integer getHoras() { return horas; }
    public void setHoras(Integer horas) { this.horas = horas; }
    
    public Integer getMinutos() { return minutos; }
    public void setMinutos(Integer minutos) { this.minutos = minutos; }
}
