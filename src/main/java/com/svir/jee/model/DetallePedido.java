package com.svir.jee.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetallePedido {

    private Integer id;
    private Integer pedidoId;
    private Integer productoId;
    private String productoNombre; // solo para mostrar en pantalla (join)
    private int cantidad;
    private int cantidadAtendida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private EstadoDetallePedido estado;
    private LocalDateTime createdAt;

    public DetallePedido() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidadAtendida() {
        return cantidadAtendida;
    }

    public void setCantidadAtendida(int cantidadAtendida) {
        this.cantidadAtendida = cantidadAtendida;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public EstadoDetallePedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoDetallePedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
