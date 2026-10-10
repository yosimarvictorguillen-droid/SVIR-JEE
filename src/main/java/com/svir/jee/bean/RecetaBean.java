package com.svir.jee.bean;

import com.svir.jee.dao.IngredienteDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.dao.RecetaDAO;
import com.svir.jee.model.Ingrediente;
import com.svir.jee.model.Producto;
import com.svir.jee.model.RecetaItem;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Managed Bean de Recetas: lista los productos y permite configurar, en un modal,
 * los ingredientes y la cantidad que consume UNA unidad de cada producto.
 */
@Named
@ViewScoped
public class RecetaBean extends ListadoBase<Producto> {

    private static final long serialVersionUID = 1L;
    private static final RecetaDAO DAO_RECETA = new RecetaDAO();
    private static final ProductoDAO DAO_PRODUCTO = new ProductoDAO();
    private static final IngredienteDAO DAO_INGREDIENTE = new IngredienteDAO();

    /** Una fila editable de la receta (ingrediente + cantidad). */
    public static class Linea implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer ingredienteId;
        private BigDecimal cantidad;

        public Integer getIngredienteId() { return ingredienteId; }
        public void setIngredienteId(Integer ingredienteId) { this.ingredienteId = ingredienteId; }
        public BigDecimal getCantidad() { return cantidad; }
        public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    }

    private boolean editando;
    private Producto producto;
    private List<Linea> lineas = new ArrayList<>();
    private transient List<Ingrediente> ingredientes;

    @Override
    protected List<Producto> cargar() throws SQLException {
        return DAO_PRODUCTO.listarTodos();
    }

    @Override
    protected String textoBusqueda(Producto p) {
        return texto(p.getNombre());
    }

    public List<Ingrediente> getIngredientes() {
        if (ingredientes == null) {
            try {
                ingredientes = DAO_INGREDIENTE.listarTodos();
            } catch (SQLException e) {
                Mensajes.error("No se pudo leer la lista de ingredientes.");
                ingredientes = List.of();
            }
        }
        return ingredientes;
    }

    public void editar(Producto p) {
        Formularios.resetear("formReceta");
        producto = p;
        lineas = new ArrayList<>();
        try {
            for (RecetaItem item : DAO_RECETA.listarPorProducto(p.getId())) {
                Linea l = new Linea();
                l.setIngredienteId(item.getIngredienteId());
                l.setCantidad(item.getCantidad());
                lineas.add(l);
            }
        } catch (SQLException e) {
            Mensajes.error("No se pudo leer la receta del producto.");
        }
        if (lineas.isEmpty()) {
            lineas.add(new Linea());
        }
        editando = true;
    }

    public void agregarLinea() {
        lineas.add(new Linea());
    }

    public void quitarLinea(Linea linea) {
        lineas.remove(linea);
        if (lineas.isEmpty()) {
            lineas.add(new Linea());
        }
    }

    public void cancelar() {
        Formularios.resetear("formReceta");
        editando = false;
        producto = null;
    }

    public void guardar() {
        // Las filas incompletas se ignoran y un ingrediente repetido suma sus cantidades.
        Map<Integer, BigDecimal> porIngrediente = new LinkedHashMap<>();
        for (Linea l : lineas) {
            if (l.getIngredienteId() == null || l.getCantidad() == null
                    || l.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            porIngrediente.merge(l.getIngredienteId(), l.getCantidad(), BigDecimal::add);
        }

        List<RecetaItem> items = new ArrayList<>();
        porIngrediente.forEach((ingredienteId, cantidad) -> {
            RecetaItem item = new RecetaItem();
            item.setIngredienteId(ingredienteId);
            item.setCantidad(cantidad);
            items.add(item);
        });

        try {
            DAO_RECETA.reemplazar(producto.getId(), items);
            Mensajes.info("Receta de \"" + producto.getNombre() + "\" guardada con " + items.size() + " ingrediente(s).");
            Formularios.resetear("formReceta");
            editando = false;
            producto = null;
        } catch (SQLException e) {
            Mensajes.error("No se pudo guardar la receta.");
        }
    }

    public boolean isEditando() { return editando; }
    public Producto getProducto() { return producto; }
    public List<Linea> getLineas() { return lineas; }
}
