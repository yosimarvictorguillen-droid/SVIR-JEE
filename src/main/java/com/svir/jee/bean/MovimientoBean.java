package com.svir.jee.bean;

import com.svir.jee.dao.MovimientoIngredienteDAO;
import com.svir.jee.dao.MovimientoProductoDAO;
import com.svir.jee.model.MovimientoIngrediente;
import com.svir.jee.model.MovimientoProducto;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Managed Bean de la auditoria de movimientos de stock (productos e ingredientes), con paginacion. */
@Named
@ViewScoped
public class MovimientoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final MovimientoProductoDAO DAO_PRODUCTO = new MovimientoProductoDAO();
    private static final MovimientoIngredienteDAO DAO_INGREDIENTE = new MovimientoIngredienteDAO();

    // filtros: valores del formulario (q, tipo, ver, desde, hasta) y los ya aplicados
    private String q = "";
    private String tipo = "";
    private String ver = "";
    private String desde = "";
    private String hasta = "";
    private String aq = "";
    private String atipo = "";
    private String aver = "";
    private String adesde = "";
    private String ahasta = "";

    private final int filas = 10;
    private int primeroProducto = 0;
    private int primeroIngrediente = 0;

    private transient List<MovimientoProducto> productos;
    private transient List<MovimientoIngrediente> ingredientes;

    public List<MovimientoProducto> getProductos() {
        if (productos == null) {
            try {
                productos = verProductos() ? DAO_PRODUCTO.buscar(aq, atipo, fecha(adesde), fecha(ahasta), Integer.MAX_VALUE, 0) : List.<MovimientoProducto>of();
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
                ingredientes = verIngredientes() ? DAO_INGREDIENTE.buscar(aq, atipo, fecha(adesde), fecha(ahasta), Integer.MAX_VALUE, 0) : List.<MovimientoIngrediente>of();
            } catch (SQLException e) {
                Mensajes.error("No se pudieron leer los movimientos de ingredientes.");
                ingredientes = List.of();
            }
        }
        return ingredientes;
    }

    // ---- filtros ----

    private static LocalDate fecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean verProductos() { return !"INGREDIENTES".equals(aver); }
    private boolean verIngredientes() { return !"PRODUCTOS".equals(aver); }
    public boolean isMostrarProductos() { return verProductos(); }
    public boolean isMostrarIngredientes() { return verIngredientes(); }

    public void buscar() {
        aq = com.svir.jee.util.Textos.limpiar(q, 100);
        atipo = tipo == null ? "" : tipo;
        aver = ver == null ? "" : ver;
        adesde = desde == null ? "" : desde;
        ahasta = hasta == null ? "" : hasta;
        primeroProducto = 0;
        primeroIngrediente = 0;
        productos = null;
        ingredientes = null;
    }

    public void limpiar() {
        q = "";
        tipo = "";
        ver = "";
        desde = "";
        hasta = "";
        buscar();
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getVer() { return ver; }
    public void setVer(String ver) { this.ver = ver; }
    public String getDesde() { return desde; }
    public void setDesde(String desde) { this.desde = desde; }
    public String getHasta() { return hasta; }
    public void setHasta(String hasta) { this.hasta = hasta; }

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
