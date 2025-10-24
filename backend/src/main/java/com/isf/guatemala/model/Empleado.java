package com.isf.guatemala.model;

import javax.persistence.*;

@Entity
@Table(name = "empleados")
public class Empleado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String contacto;
    
    @ManyToOne
    @JoinColumn(name = "puesto_id")
    private Puesto puesto;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public Puesto getPuesto() { return puesto; }
    public void setPuesto(Puesto puesto) { this.puesto = puesto; }
}
