package com.svir.jee.model;

import java.math.BigDecimal;

/** Una linea de receta: cuanto de un ingrediente lleva un producto. */
public class RecetaItem {

    private Integer id;
    private Integer productoId;
    private Integer ingredienteId;
    private String ingredienteNombre;
    private String unidadMedida;
    private BigDecimal cantidad;

    public RecetaItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
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

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }
}
