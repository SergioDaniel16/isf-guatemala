package com.isf.guatemala.model;

import javax.persistence.*;

@Entity
@Table(name = "tareas")
public class Tarea {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String definicion;
    private Boolean cobrable;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDefinicion() { return definicion; }
    public void setDefinicion(String definicion) { this.definicion = definicion; }
    
    public Boolean getCobrable() { return cobrable; }
    public void setCobrable(Boolean cobrable) { this.cobrable = cobrable; }
}
