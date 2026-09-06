package com.svir.jee.servlet;

import com.svir.jee.dao.ProductoDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Landing publica de la tienda web (sin autenticacion). */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<com.svir.jee.model.Producto> productos = productoDAO.listarActivos();
            req.setAttribute("productosDestacados", productos.size() > 4 ? productos.subList(0, 4) : productos);
            req.setAttribute("rutaActual", "/home");
            req.getRequestDispatcher("/WEB-INF/views/tienda/home.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error cargando la pagina de inicio", e);
        }
    }
}
