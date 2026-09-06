package com.svir.jee.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoIngrediente {

    private Integer id;
    private Integer ingredienteId;
    private String ingredienteNombre;
    private TipoMovimiento tipo;
    private MotivoMovimientoIngrediente motivo;
    private BigDecimal cantidad;
    private BigDecimal stockAnterior;
    private BigDecimal stockNuevo;
    private String referenciaTipo;
    private Integer referenciaId;
    private Integer usuarioId;
    private String usuarioNombre;
    private LocalDateTime createdAt;

    public MovimientoIngrediente() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIngredienteId() {
        return ingredienteId;
    }

    public void setIngredienteId(Integer ingredienteId) {
        this.ingredienteId = ingredienteId;
    }

    public String getIngredienteNombre() {
        return ingredienteNombre;
    }

    public void setIngredienteNombre(String ingredienteNombre) {
        this.ingredienteNombre = ingredienteNombre;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public MotivoMovimientoIngrediente getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoMovimientoIngrediente motivo) {
        this.motivo = motivo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(BigDecimal stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public BigDecimal getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(BigDecimal stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public void setReferenciaTipo(String referenciaTipo) {
        this.referenciaTipo = referenciaTipo;
    }

    public Integer getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Integer referenciaId) {
        this.referenciaId = referenciaId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
