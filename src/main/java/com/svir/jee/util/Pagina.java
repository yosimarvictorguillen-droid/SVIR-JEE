package com.svir.jee.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagina de resultados para las pantallas JSP con filtros. Sirve tanto para una lista ya filtrada en memoria
 * ({@link #de}) como para una consulta SQL con LIMIT/OFFSET ({@link #deTotal} + {@link #setItems}).
 */
public final class Pagina<T> {

    public static final int POR_PAGINA = 10;

    private List<T> items = new ArrayList<>();
    private final int pagina;
    private final int totalPaginas;
    private final int total;

    private Pagina(int total, String paginaParam) {
        this.total = total;
        this.totalPaginas = Math.max(1, (total + POR_PAGINA - 1) / POR_PAGINA);
        int p = 1;
        try {
            p = Integer.parseInt(paginaParam);
        } catch (NumberFormatException ignored) {
            // sin parametro o invalido: primera pagina
        }
        this.pagina = Math.min(Math.max(1, p), totalPaginas);
    }

    /** Pagina de una lista que ya esta completa en memoria. */
    public static <T> Pagina<T> de(List<T> todos, String paginaParam) {
        Pagina<T> pg = new Pagina<>(todos.size(), paginaParam);
        int desde = (pg.pagina - 1) * POR_PAGINA;
        pg.items = new ArrayList<>(todos.subList(desde, Math.min(desde + POR_PAGINA, todos.size())));
        return pg;
    }

    /** Pagina para una consulta SQL: primero se calcula con el total y luego se cargan los items del OFFSET. */
    public static <T> Pagina<T> deTotal(int total, String paginaParam) {
        return new Pagina<>(total, paginaParam);
    }

    public int getOffset() {
        return (pagina - 1) * POR_PAGINA;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public int getTotal() {
        return total;
    }

    /**
     * Deja en la peticion lo que necesita common/paginacion.jsp. {@code urlBase} ya incluye los filtros
     * (ej. "/app/clientes?q=ana"); {@code param} es el nombre del parametro de pagina.
     */
    public void publicar(HttpServletRequest req, String urlBase, String param, String sufijo) {
        String union = urlBase.contains("?") ? (urlBase.endsWith("?") || urlBase.endsWith("&") ? "" : "&") : "?";
        req.setAttribute("pgPagina" + sufijo, pagina);
        req.setAttribute("pgTotalPaginas" + sufijo, totalPaginas);
        req.setAttribute("pgTotal" + sufijo, total);
        req.setAttribute("pgPorPagina" + sufijo, POR_PAGINA);
        req.setAttribute("pgUrl" + sufijo, urlBase + union + param + "=");
    }

    public void publicar(HttpServletRequest req, String urlBase) {
        publicar(req, urlBase, "pagina", "");
    }
}
