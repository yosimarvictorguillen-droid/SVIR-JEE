package com.svir.jee.bean;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Comportamiento comun de los listados con filtro por texto y paginacion
 * (h:dataTable con first/rows). Cada modulo solo indica como cargar los datos
 * (via su DAO/JDBC) y sobre que texto se filtra.
 */
public abstract class ListadoBase<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String filtro = "";
    private String filtroAplicado = "";
    private int primero = 0;
    private final int filas = 5;
    private transient List<T> cache;

    protected abstract List<T> cargar() throws SQLException;

    protected abstract String textoBusqueda(T item);

    public List<T> getItems() {
        if (cache == null) {
            String f = filtroAplicado.toLowerCase(Locale.ROOT);
            try {
                cache = cargar().stream()
                        .filter(i -> f.isEmpty() || textoBusqueda(i).toLowerCase(Locale.ROOT).contains(f))
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                Mensajes.error("No se pudo leer la lista desde la base de datos.");
                cache = List.of();
            }
        }
        return cache;
    }

    /** Fuerza a releer los datos de la base en el siguiente acceso. */
    protected void recargar() {
        cache = null;
    }

    protected static String texto(String valor) {
        return valor == null ? "" : valor;
    }

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
    public boolean isHaySiguiente() { return primero + filas < getItems().size(); }
    public int getTotal() { return getItems().size(); }
    public int getDesde() { return getTotal() == 0 ? 0 : primero + 1; }
    public int getHasta() { return Math.min(primero + filas, getTotal()); }

    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }
    public int getPrimero() { return primero; }
    public int getFilas() { return filas; }
}
