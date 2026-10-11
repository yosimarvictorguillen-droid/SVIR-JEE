package com.svir.jee.servlet;

import com.svir.jee.dao.MovimientoIngredienteDAO;
import com.svir.jee.dao.MovimientoProductoDAO;

import com.svir.jee.util.Filtros;
import com.svir.jee.util.Pagina;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Vista consolidada (solo lectura) de movimientos de stock de productos e ingredientes. */
@WebServlet("/app/movimientos")
public class MovimientoServlet extends HttpServlet {

    private final MovimientoProductoDAO movimientoProductoDAO = new MovimientoProductoDAO();
    private final MovimientoIngredienteDAO movimientoIngredienteDAO = new MovimientoIngredienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String q = Filtros.texto(req, "q");
            String tipo = Filtros.opcion(req, "tipo", "ENTRADA", "SALIDA");
            String ver = Filtros.opcion(req, "ver", "PRODUCTOS", "INGREDIENTES");
            java.time.LocalDate desde = Filtros.fecha(req, "desde");
            java.time.LocalDate hasta = Filtros.fecha(req, "hasta");
            String filtros = Filtros.query("q", q, "tipo", tipo, "ver", ver,
                    "desde", desde == null ? "" : desde.toString(), "hasta", hasta == null ? "" : hasta.toString());

            if (!"INGREDIENTES".equals(ver)) {
                Pagina<com.svir.jee.model.MovimientoProducto> pgP = Pagina.deTotal(
                        movimientoProductoDAO.contar(q, tipo, desde, hasta), req.getParameter("pagP"));
                pgP.setItems(movimientoProductoDAO.buscar(q, tipo, desde, hasta, Pagina.POR_PAGINA, pgP.getOffset()));
                pgP.publicar(req, req.getContextPath() + "/app/movimientos?" + filtros, "pagP", "P");
                req.setAttribute("movimientosProducto", pgP.getItems());
            }
            if (!"PRODUCTOS".equals(ver)) {
                Pagina<com.svir.jee.model.MovimientoIngrediente> pgI = Pagina.deTotal(
                        movimientoIngredienteDAO.contar(q, tipo, desde, hasta), req.getParameter("pagI"));
                pgI.setItems(movimientoIngredienteDAO.buscar(q, tipo, desde, hasta, Pagina.POR_PAGINA, pgI.getOffset()));
                pgI.publicar(req, req.getContextPath() + "/app/movimientos?" + filtros, "pagI", "I");
                req.setAttribute("movimientosIngrediente", pgI.getItems());
            }
            req.setAttribute("fq", q);
            req.setAttribute("ftipo", tipo);
            req.setAttribute("fver", ver);
            req.setAttribute("fdesde", desde == null ? "" : desde.toString());
            req.setAttribute("fhasta", hasta == null ? "" : hasta.toString());
            req.getRequestDispatcher("/WEB-INF/views/movimientos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a movimientos", e);
        }
    }
}
