package com.svir.jee.servlet;

import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Producto;
import com.svir.jee.util.AlmacenImagenes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

/**
 * CRUD de productos. Un solo servlet resuelve todas las variantes de la
 * pantalla a traves del parametro "action" (patron Front Controller simple,
 * tipico de aplicaciones JSP/Servlet clasicas sin framework MVC).
 */
@WebServlet("/app/productos")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class ProductoServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("nuevo".equals(action)) {
                req.getRequestDispatcher("/WEB-INF/views/producto-form.jsp").forward(req, resp);
                return;
            }
            if ("editar".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                productoDAO.buscarPorId(id).ifPresentOrElse(
                        p -> req.setAttribute("producto", p),
                        () -> req.setAttribute("error", "Producto no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/producto-form.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("productos", productoDAO.listarTodos());
            req.getRequestDispatcher("/WEB-INF/views/productos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a productos", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("toggle".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                boolean activo = Boolean.parseBoolean(req.getParameter("activo"));
                productoDAO.cambiarActivo(id, activo);
                resp.sendRedirect(req.getContextPath() + "/app/productos");
                return;
            }

            String idParam = req.getParameter("id");
            Producto p = new Producto();
            boolean esNuevo = idParam == null || idParam.isBlank();
            if (!esNuevo) {
                p.setId(Integer.parseInt(idParam));
            }
            p.setNombre(req.getParameter("nombre"));
            p.setDescripcion(req.getParameter("descripcion"));
            p.setPrecio(new BigDecimal(req.getParameter("precio")));
            p.setStock(Integer.parseInt(req.getParameter("stock")));
            p.setStockMinimo(Integer.parseInt(req.getParameter("stockMinimo")));

            if (esNuevo) {
                p.setActivo(true);
                productoDAO.crear(p);
            } else {
                // Preserva el estado activo/inactivo actual: este formulario no lo edita.
                Optional<Producto> actual = productoDAO.buscarPorId(p.getId());
                p.setActivo(actual.map(Producto::isActivo).orElse(true));
                productoDAO.actualizar(p);
            }

            Part imagen = req.getPart("imagen");
            if (imagen != null && imagen.getSize() > 0) {
                String tipo = imagen.getContentType();
                if (tipo != null && tipo.startsWith("image/")) {
                    if (!esNuevo) {
                        productoDAO.buscarPorId(p.getId())
                                .map(Producto::getImagenUrl)
                                .ifPresent(AlmacenImagenes::borrarPorUrl);
                    }
                    String nombreArchivo = AlmacenImagenes.guardar(imagen, p.getId());
                    productoDAO.actualizarImagen(p.getId(), "/uploads/productos/" + nombreArchivo);
                }
            }

            resp.sendRedirect(req.getContextPath() + "/app/productos");
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Precio, stock y stock minimo deben ser numeros validos.");
            req.getRequestDispatcher("/WEB-INF/views/producto-form.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error guardando el producto", e);
        }
    }
}
