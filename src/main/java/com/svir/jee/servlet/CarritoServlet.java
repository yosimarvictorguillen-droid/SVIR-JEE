package com.svir.jee.servlet;

import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Producto;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carrito de compras de la tienda web: vive en la sesion HTTP como
 * Map&lt;productoId, cantidad&gt; hasta que el cliente hace checkout.
 */
@WebServlet("/carrito")
public class CarritoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @SuppressWarnings("unchecked")
    public static Map<Integer, Integer> obtenerCarrito(HttpSession session) {
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Map<Integer, Integer> carrito = obtenerCarrito(req.getSession(true));
            Map<Producto, Integer> lineas = new LinkedHashMap<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (Map.Entry<Integer, Integer> e : carrito.entrySet()) {
                var producto = productoDAO.buscarPorId(e.getKey());
                if (producto.isPresent()) {
                    lineas.put(producto.get(), e.getValue());
                    total = total.add(producto.get().getPrecio().multiply(java.math.BigDecimal.valueOf(e.getValue())));
                }
            }
            req.setAttribute("lineas", lineas);
            req.setAttribute("total", total);
            req.setAttribute("rutaActual", "/carrito");
            req.getRequestDispatcher("/WEB-INF/views/tienda/carrito.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo al carrito", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<Integer, Integer> carrito = obtenerCarrito(req.getSession(true));
        String action = req.getParameter("action");
        int productoId = Integer.parseInt(req.getParameter("productoId"));

        switch (action == null ? "" : action) {
            case "actualizar" -> {
                int cantidad = Integer.parseInt(req.getParameter("cantidad"));
                if (cantidad <= 0) {
                    carrito.remove(productoId);
                } else {
                    carrito.put(productoId, cantidad);
                }
            }
            case "eliminar" -> carrito.remove(productoId);
            case "vaciar" -> carrito.clear();
            default -> { // agregar
                int cantidad = Integer.parseInt(req.getParameter("cantidad"));
                carrito.merge(productoId, cantidad, Integer::sum);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/carrito");
    }
}
