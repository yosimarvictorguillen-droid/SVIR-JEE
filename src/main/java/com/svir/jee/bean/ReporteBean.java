package com.svir.jee.bean;

import com.svir.jee.dao.IngredienteDAO;
import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.dao.ReporteDAO;
import com.svir.jee.model.Ingrediente;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Managed Bean de Reportes: ventas por rango de fechas y alertas de stock bajo. */
@Named
@ViewScoped
public class ReporteBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final PedidoDAO DAO_PEDIDO = new PedidoDAO();
    private static final ProductoDAO DAO_PRODUCTO = new ProductoDAO();
    private static final IngredienteDAO DAO_INGREDIENTE = new IngredienteDAO();
    private static final ReporteDAO DAO_REPORTE = new ReporteDAO();

    private LocalDate desde = LocalDate.now().withDayOfMonth(1);
    private LocalDate hasta = LocalDate.now();

    private LocalDate desdeAplicado = desde;
    private LocalDate hastaAplicado = hasta;

    private transient List<Pedido> pedidos;
    private transient BigDecimal totalVentas;
    private transient List<Producto> productosStockBajo;
    private transient List<Ingrediente> ingredientesStockBajo;

    public void consultar() {
        if (desde.isAfter(hasta)) {
            Mensajes.error("La fecha \"Desde\" no puede ser posterior a \"Hasta\".");
            return;
        }
        desdeAplicado = desde;
        hastaAplicado = hasta;
        pedidos = null;
        totalVentas = null;
    }

    public List<Pedido> getPedidos() {
        if (pedidos == null) {
            try {
                pedidos = DAO_PEDIDO.listarPorRangoFechas(desdeAplicado, hastaAplicado);
            } catch (SQLException e) {
                Mensajes.error("No se pudieron leer los pedidos del rango.");
                pedidos = List.of();
            }
        }
        return pedidos;
    }

    public BigDecimal getTotalVentas() {
        if (totalVentas == null) {
            try {
                totalVentas = DAO_REPORTE.totalVentasEnRango(desdeAplicado, hastaAplicado);
            } catch (SQLException e) {
                Mensajes.error("No se pudo calcular el total de ventas.");
                totalVentas = BigDecimal.ZERO;
            }
        }
        return totalVentas;
    }

    public List<Producto> getProductosStockBajo() {
        if (productosStockBajo == null) {
            try {
                productosStockBajo = DAO_PRODUCTO.listarStockBajo();
            } catch (SQLException e) {
                productosStockBajo = List.of();
            }
        }
        return productosStockBajo;
    }

    public List<Ingrediente> getIngredientesStockBajo() {
        if (ingredientesStockBajo == null) {
            try {
                ingredientesStockBajo = DAO_INGREDIENTE.listarStockBajo();
            } catch (SQLException e) {
                ingredientesStockBajo = List.of();
            }
        }
        return ingredientesStockBajo;
    }

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
}
