package com.svir.jee.servlet;

import com.svir.jee.dao.MovimientoIngredienteDAO;
import com.svir.jee.dao.MovimientoProductoDAO;

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
            req.setAttribute("movimientosProducto", movimientoProductoDAO.listarTodos());
            req.setAttribute("movimientosIngrediente", movimientoIngredienteDAO.listarTodos());
            req.getRequestDispatcher("/WEB-INF/views/movimientos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a movimientos", e);
        }
    }
}
