package com.svir.jee.servlet;

import com.svir.jee.dao.ProductoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Catalogo publico de productos (tienda web, sin autenticacion). */
@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("productos", productoDAO.listarActivos());
            req.setAttribute("rutaActual", "/catalogo");
            req.getRequestDispatcher("/WEB-INF/views/tienda/catalogo.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo al catalogo", e);
        }
    }
}
