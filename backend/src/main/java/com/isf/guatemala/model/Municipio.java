package com.isf.guatemala.model;

import javax.persistence.*;

@Entity
@Table(name = "municipios")
public class Municipio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    
    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
}
