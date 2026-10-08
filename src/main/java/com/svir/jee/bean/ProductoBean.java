package com.svir.jee.bean;

import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Producto;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Managed Bean (@ViewScoped) del modulo Productos: listado con filtro y
 * paginacion para h:dataTable, y alta/edicion en un modal Bootstrap.
 * Toda la persistencia pasa por ProductoDAO (JDBC).
 */
@Named
@ViewScoped
public class ProductoBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ProductoDAO DAO = new ProductoDAO();

    private String filtro = "";
    private String filtroAplicado = "";
    private int primero = 0;
    private final int filas = 5;

    private boolean editando;
    private Producto producto;

    private transient List<Producto> cache;

    public List<Producto> getProductos() {
        if (cache == null) {
            String f = filtroAplicado.toLowerCase(Locale.ROOT);
            try {
                cache = DAO.listarTodos().stream()
                        .filter(p -> f.isEmpty() || p.getNombre().toLowerCase(Locale.ROOT).contains(f))
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo leer la lista de productos.");
                cache = List.of();
            }
        }
        return cache;
    }

    // ---- filtro y paginacion ----

    public void buscar() {
        filtroAplicado = filtro == null ? "" : filtro.trim();
        primero = 0;
        cache = null;
    }

    public void limpiar() {
        filtro = "";
        buscar();
    }

    public void anterior() {
        primero = Math.max(0, primero - filas);
    }

    public void siguiente() {
        if (isHaySiguiente()) {
            primero += filas;
        }
    }

    public boolean isHayAnterior() { return primero > 0; }
    public boolean isHaySiguiente() { return primero + filas < getProductos().size(); }
    public int getTotal() { return getProductos().size(); }
    public int getDesde() { return getTotal() == 0 ? 0 : primero + 1; }
    public int getHasta() { return Math.min(primero + filas, getTotal()); }

    // ---- alta / edicion (modal) ----

    public void nuevo() {
        producto = new Producto();
        producto.setActivo(true);
        editando = true;
    }

    public void editar(Producto p) {
        Producto copia = new Producto();
        copia.setId(p.getId());
        copia.setNombre(p.getNombre());
        copia.setDescripcion(p.getDescripcion());
        copia.setPrecio(p.getPrecio());
        copia.setStock(p.getStock());
        copia.setStockMinimo(p.getStockMinimo());
        copia.setActivo(p.isActivo());
        copia.setImagenUrl(p.getImagenUrl());
        producto = copia;
        editando = true;
    }

    public void cancelar() {
        editando = false;
        producto = null;
    }

    public void guardar() {
        try {
            boolean esNuevo = producto.getId() == null;
            if (esNuevo) {
                DAO.crear(producto);
            } else {
                DAO.actualizar(producto);
            }
            mensaje(FacesMessage.SEVERITY_INFO,
                    "Producto \"" + producto.getNombre() + "\" " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
            editando = false;
            producto = null;
            cache = null;
        } catch (SQLException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo guardar el producto.");
        }
    }

    public void alternarActivo(Producto p) {
        try {
            DAO.cambiarActivo(p.getId(), !p.isActivo());
            mensaje(FacesMessage.SEVERITY_INFO,
                    "Producto \"" + p.getNombre() + "\" " + (p.isActivo() ? "desactivado" : "activado") + ".");
            cache = null;
        } catch (SQLException e) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se pudo cambiar el estado del producto.");
        }
    }

    private static void mensaje(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, texto, null));
    }

    // ---- getters / setters para el binding EL ----

    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }
    public int getPrimero() { return primero; }
    public int getFilas() { return filas; }
    public boolean isEditando() { return editando; }
    public Producto getProducto() { return producto; }
}
