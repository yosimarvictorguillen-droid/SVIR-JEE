package com.svir.jee.model;

import java.math.BigDecimal;
import java.util.List;

/** DTO de solo lectura con los datos agregados que muestra el dashboard. */
public class DashboardResumen {

    private BigDecimal ventasHoy = BigDecimal.ZERO;
    private BigDecimal ventasMes = BigDecimal.ZERO;
    private int pedidosHoy;
    private int pedidosPendientes;
    private int pedidosPreparacion;
    private int pedidosListo;
    private int totalProductos;
    private int totalIngredientes;
    private int productosStockBajo;
    private int ingredientesStockBajo;
    private List<Pedido> pedidosRecientes;

    public BigDecimal getVentasHoy() {
        return ventasHoy;
    }

    public void setVentasHoy(BigDecimal ventasHoy) {
        this.ventasHoy = ventasHoy;
    }

    public BigDecimal getVentasMes() {
        return ventasMes;
    }

    public void setVentasMes(BigDecimal ventasMes) {
        this.ventasMes = ventasMes;
    }

    public int getPedidosHoy() {
        return pedidosHoy;
    }

    public void setPedidosHoy(int pedidosHoy) {
        this.pedidosHoy = pedidosHoy;
    }

    public int getPedidosPendientes() {
        return pedidosPendientes;
    }

    public void setPedidosPendientes(int pedidosPendientes) {
        this.pedidosPendientes = pedidosPendientes;
    }

    public int getPedidosPreparacion() {
        return pedidosPreparacion;
    }

    public void setPedidosPreparacion(int pedidosPreparacion) {
        this.pedidosPreparacion = pedidosPreparacion;
    }

    public int getPedidosListo() {
        return pedidosListo;
    }

    public void setPedidosListo(int pedidosListo) {
        this.pedidosListo = pedidosListo;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    public int getTotalIngredientes() {
        return totalIngredientes;
    }

    public void setTotalIngredientes(int totalIngredientes) {
        this.totalIngredientes = totalIngredientes;
    }

    public int getProductosStockBajo() {
        return productosStockBajo;
    }

    public void setProductosStockBajo(int productosStockBajo) {
        this.productosStockBajo = productosStockBajo;
    }

    public int getIngredientesStockBajo() {
        return ingredientesStockBajo;
    }

    public void setIngredientesStockBajo(int ingredientesStockBajo) {
        this.ingredientesStockBajo = ingredientesStockBajo;
    }

    public List<Pedido> getPedidosRecientes() {
        return pedidosRecientes;
    }

    public void setPedidosRecientes(List<Pedido> pedidosRecientes) {
        this.pedidosRecientes = pedidosRecientes;
    }
}
