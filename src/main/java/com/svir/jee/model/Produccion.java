package com.svir.jee.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Produccion {

    private Integer id;
    private TipoProduccion tipo;
    private Integer pedidoId;
    private Integer usuarioId;
    private String usuarioNombre;
    private EstadoProduccion estado;
    private String observacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime createdAt;
    private List<ProduccionDetalle> detalles = new ArrayList<>();

    public Produccion() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoProduccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoProduccion tipo) {
        this.tipo = tipo;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
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

    public EstadoProduccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoProduccion estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProduccionDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ProduccionDetalle> detalles) {
        this.detalles = detalles;
    }
}
