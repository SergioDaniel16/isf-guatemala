package com.isf.guatemala.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "viajes_finalizados")
public class ViajeFinalizado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "viaje_iniciado_id")
    private ViajeIniciado viajeIniciado;
    
    private String rutaFoto;
    private Double millajeEntrada;
    private LocalDate fechaFin;
    private Double millasRecorridas;
    private Double kmRecorridos;
    private Double kmExtra;
    private Double costoUSD;
    private Double costoGTQ;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ViajeIniciado getViajeIniciado() { return viajeIniciado; }
    public void setViajeIniciado(ViajeIniciado viajeIniciado) { this.viajeIniciado = viajeIniciado; }
    
    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }
    
    public Double getMillajeEntrada() { return millajeEntrada; }
    public void setMillajeEntrada(Double millajeEntrada) { this.millajeEntrada = millajeEntrada; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public Double getMillasRecorridas() { return millasRecorridas; }
    public void setMillasRecorridas(Double millasRecorridas) { this.millasRecorridas = millasRecorridas; }
    
    public Double getKmRecorridos() { return kmRecorridos; }
    public void setKmRecorridos(Double kmRecorridos) { this.kmRecorridos = kmRecorridos; }
    
    public Double getKmExtra() { return kmExtra; }
    public void setKmExtra(Double kmExtra) { this.kmExtra = kmExtra; }
    
    public Double getCostoUSD() { return costoUSD; }
    public void setCostoUSD(Double costoUSD) { this.costoUSD = costoUSD; }

    public Double getCostoGTQ() { return costoGTQ; }
    public void setCostoGTQ(Double costoGTQ) { this.costoGTQ = costoGTQ; }
}
