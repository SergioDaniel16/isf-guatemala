package com.isf.guatemala.model;

import javax.persistence.*;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String marca;
    private String modelo;
    private Integer ano;
    private String color;
    private String placa;
    private String tipoVehiculo;
    private Double odometroActual;
    private String tipoCombustible;
    private String transmision;
    
    @Enumerated(EnumType.STRING)
    private UnidadMedida unidadMedida;
    
    @Enumerated(EnumType.STRING)
    private Estado estado;
    
    public enum UnidadMedida {
        MILLAS, KILOMETROS
    }
    
    public enum Estado {
        ACTIVO, INACTIVO
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    
    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
    
    public Double getOdometroActual() { return odometroActual; }
    public void setOdometroActual(Double odometroActual) { this.odometroActual = odometroActual; }
    
    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }
    
    public String getTransmision() { return transmision; }
    public void setTransmision(String transmision) { this.transmision = transmision; }
    
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida; }
    
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
