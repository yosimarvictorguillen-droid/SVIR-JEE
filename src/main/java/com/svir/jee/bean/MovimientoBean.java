package com.svir.jee.bean;

import com.svir.jee.dao.MovimientoIngredienteDAO;
import com.svir.jee.dao.MovimientoProductoDAO;
import com.svir.jee.model.MovimientoIngrediente;
import com.svir.jee.model.MovimientoProducto;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/** Managed Bean de la auditoria de movimientos de stock (productos e ingredientes), con paginacion. */
@Named
@ViewScoped
public class MovimientoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final MovimientoProductoDAO DAO_PRODUCTO = new MovimientoProductoDAO();
    private static final MovimientoIngredienteDAO DAO_INGREDIENTE = new MovimientoIngredienteDAO();

    private final int filas = 10;
    private int primeroProducto = 0;
    private int primeroIngrediente = 0;

    private transient List<MovimientoProducto> productos;
    private transient List<MovimientoIngrediente> ingredientes;

    public List<MovimientoProducto> getProductos() {
        if (productos == null) {
            try {
                productos = DAO_PRODUCTO.listarTodos();
            } catch (SQLException e) {
                Mensajes.error("No se pudieron leer los movimientos de productos.");
                productos = List.of();
            }
        }
        return productos;
    }

    public List<MovimientoIngrediente> getIngredientes() {
        if (ingredientes == null) {
            try {
                ingredientes = DAO_INGREDIENTE.listarTodos();
            } catch (SQLException e) {
                Mensajes.error("No se pudieron leer los movimientos de ingredientes.");
                ingredientes = List.of();
            }
        }
        return ingredientes;
    }

    public void anteriorProducto() { primeroProducto = Math.max(0, primeroProducto - filas); }
    public void siguienteProducto() { if (isHaySiguienteProducto()) { primeroProducto += filas; } }
    public void anteriorIngrediente() { primeroIngrediente = Math.max(0, primeroIngrediente - filas); }
    public void siguienteIngrediente() { if (isHaySiguienteIngrediente()) { primeroIngrediente += filas; } }

    public boolean isHayAnteriorProducto() { return primeroProducto > 0; }
    public boolean isHaySiguienteProducto() { return primeroProducto + filas < getProductos().size(); }
    public boolean isHayAnteriorIngrediente() { return primeroIngrediente > 0; }
    public boolean isHaySiguienteIngrediente() { return primeroIngrediente + filas < getIngredientes().size(); }

    public int getDesdeProducto() { return getProductos().isEmpty() ? 0 : primeroProducto + 1; }
    public int getHastaProducto() { return Math.min(primeroProducto + filas, getProductos().size()); }
    public int getDesdeIngrediente() { return getIngredientes().isEmpty() ? 0 : primeroIngrediente + 1; }
    public int getHastaIngrediente() { return Math.min(primeroIngrediente + filas, getIngredientes().size()); }

    public int getFilas() { return filas; }
    public int getPrimeroProducto() { return primeroProducto; }
    public int getPrimeroIngrediente() { return primeroIngrediente; }
}
