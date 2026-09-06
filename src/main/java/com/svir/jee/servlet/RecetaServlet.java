package com.svir.jee.servlet;

import com.svir.jee.dao.IngredienteDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.dao.RecetaDAO;
import com.svir.jee.model.RecetaItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Administra la receta (ingredientes + cantidad) de cada producto. */
@WebServlet("/app/recetas")
public class RecetaServlet extends HttpServlet {

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String productoIdParam = req.getParameter("productoId");
            if (productoIdParam != null && !productoIdParam.isBlank()) {
                int productoId = Integer.parseInt(productoIdParam);
                productoDAO.buscarPorId(productoId).ifPresentOrElse(
                        p -> req.setAttribute("producto", p),
                        () -> req.setAttribute("error", "Producto no encontrado"));
                req.setAttribute("items", recetaDAO.listarPorProducto(productoId));
                req.setAttribute("ingredientes", ingredienteDAO.listarTodos());
                req.getRequestDispatcher("/WEB-INF/views/receta-form.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("productos", productoDAO.listarTodos());
            req.getRequestDispatcher("/WEB-INF/views/recetas.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a recetas", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int productoId = Integer.parseInt(req.getParameter("productoId"));
        String[] ingredienteIds = req.getParameterValues("ingredienteId");
        String[] cantidades = req.getParameterValues("cantidad");

        List<RecetaItem> items = new ArrayList<>();
        if (ingredienteIds != null) {
            for (int i = 0; i < ingredienteIds.length; i++) {
                if (ingredienteIds[i].isBlank() || cantidades[i].isBlank()) {
                    continue;
                }
                BigDecimal cantidad = new BigDecimal(cantidades[i]);
                if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                RecetaItem item = new RecetaItem();
                item.setIngredienteId(Integer.parseInt(ingredienteIds[i]));
                item.setCantidad(cantidad);
                items.add(item);
            }
        }

        try {
            recetaDAO.reemplazar(productoId, items);
            resp.sendRedirect(req.getContextPath() + "/app/recetas?productoId=" + productoId + "&guardado=1");
        } catch (SQLException e) {
            throw new ServletException("Error guardando la receta", e);
        }
    }
}
