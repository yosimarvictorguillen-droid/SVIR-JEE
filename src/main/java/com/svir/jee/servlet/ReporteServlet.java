package com.svir.jee.servlet;

import com.svir.jee.dao.IngredienteDAO;
import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.dao.ReporteDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Reporte de ventas por rango de fechas (parametros por GET: desde, hasta)
 * y reporte de stock bajo de productos e ingredientes.
 */
@WebServlet("/app/reportes")
public class ReporteServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();
    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            LocalDate hoy = LocalDate.now();
            LocalDate desde = parseFecha(req.getParameter("desde"), hoy.withDayOfMonth(1));
            LocalDate hasta = parseFecha(req.getParameter("hasta"), hoy);

            req.setAttribute("desde", desde);
            req.setAttribute("hasta", hasta);
            req.setAttribute("pedidosRango", pedidoDAO.listarPorRangoFechas(desde, hasta));
            req.setAttribute("totalVentasRango", reporteDAO.totalVentasEnRango(desde, hasta));
            req.setAttribute("productosStockBajo", productoDAO.listarStockBajo());
            req.setAttribute("ingredientesStockBajo", ingredienteDAO.listarStockBajo());

            req.getRequestDispatcher("/WEB-INF/views/reportes.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error generando los reportes", e);
        }
    }

    private LocalDate parseFecha(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(valor);
        } catch (Exception e) {
            return porDefecto;
        }
    }
}
