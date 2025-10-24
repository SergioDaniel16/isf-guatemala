package com.isf.guatemala.model;

import javax.persistence.*;

@Entity
@Table(name = "puestos")
public class Puesto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String detalle;
    private Double salarioPorHora;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    
    public Double getSalarioPorHora() { return salarioPorHora; }
    public void setSalarioPorHora(Double salarioPorHora) { this.salarioPorHora = salarioPorHora; }
}
