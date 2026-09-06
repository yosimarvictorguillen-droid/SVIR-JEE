package com.svir.jee.model;

public class ProduccionDetalle {

    private Integer id;
    private Integer produccionId;
    private Integer productoId;
    private String productoNombre;
    private int cantidadPlanificada;
    private int cantidadProducida;

    public ProduccionDetalle() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProduccionId() {
        return produccionId;
    }

    public void setProduccionId(Integer produccionId) {
        this.produccionId = produccionId;
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

    public int getCantidadPlanificada() {
        return cantidadPlanificada;
    }

    public void setCantidadPlanificada(int cantidadPlanificada) {
        this.cantidadPlanificada = cantidadPlanificada;
    }

    public int getCantidadProducida() {
        return cantidadProducida;
    }

    public void setCantidadProducida(int cantidadProducida) {
        this.cantidadProducida = cantidadProducida;
    }
}
