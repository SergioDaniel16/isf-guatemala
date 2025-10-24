package com.isf.guatemala.model;

import javax.persistence.*;

@Entity
@Table(name = "proyectos")
public class Proyecto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    private String nombre;
    
    @ManyToOne
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;
    
    private String departamento;
    private String municipio;
    
    @Enumerated(EnumType.STRING)
    private TipoProyecto tipo;
    
    private Double cambioDolar;
    private Double tarifaBaseVehiculo;
    private Double tarifaKmExtra;
    
    public enum TipoProyecto {
        DIRECTO, CAPITULO, EXTERNO
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Oficina getOficina() { return oficina; }
    public void setOficina(Oficina oficina) { this.oficina = oficina; }
    
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    
    public TipoProyecto getTipo() { return tipo; }
    public void setTipo(TipoProyecto tipo) { this.tipo = tipo; }
    
    public Double getCambioDolar() { return cambioDolar; }
    public void setCambioDolar(Double cambioDolar) { this.cambioDolar = cambioDolar; }
    
    public Double getTarifaBaseVehiculo() { return tarifaBaseVehiculo; }
    public void setTarifaBaseVehiculo(Double tarifaBaseVehiculo) { this.tarifaBaseVehiculo = tarifaBaseVehiculo; }
    
    public Double getTarifaKmExtra() { return tarifaKmExtra; }
    public void setTarifaKmExtra(Double tarifaKmExtra) { this.tarifaKmExtra = tarifaKmExtra; }
}
