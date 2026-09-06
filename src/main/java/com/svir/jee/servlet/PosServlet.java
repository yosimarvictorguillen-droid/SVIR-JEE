package com.svir.jee.servlet;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;
import com.svir.jee.model.TipoOrigenPedido;
import com.svir.jee.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Punto de venta presencial: carrito rapido + emision de comprobante + ticket imprimible. */
@WebServlet("/app/pos")
public class PosServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            if ("ticket".equals(req.getParameter("action"))) {
                int id = Integer.parseInt(req.getParameter("id"));
                pedidoDAO.buscarPorId(id).ifPresentOrElse(
                        p -> req.setAttribute("pedido", p),
                        () -> req.setAttribute("error", "Pedido no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/pos-ticket.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("productos", productoDAO.listarActivos());
            req.getRequestDispatcher("/WEB-INF/views/pos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo al POS", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");

        if (productoIds == null || productoIds.length == 0) {
            req.setAttribute("error", "El carrito esta vacio.");
            try {
                req.setAttribute("productos", productoDAO.listarActivos());
            } catch (SQLException ignored) {
                // si tampoco se puede listar, se muestra el error igual sin el grid
            }
            req.getRequestDispatcher("/WEB-INF/views/pos.jsp").forward(req, resp);
            return;
        }

        try {
            Map<Integer, Producto> productosPorId = new HashMap<>();
            for (Producto p : productoDAO.listarActivos()) {
                productosPorId.put(p.getId(), p);
            }

            Pedido pedido = new Pedido();
            pedido.setTipoOrigen(TipoOrigenPedido.PRESENCIAL);

            String tipoComprobante = req.getParameter("tipoComprobante");
            String documento = req.getParameter("documento");
            String razonSocial = req.getParameter("razonSocial");
            StringBuilder obs = new StringBuilder("Comprobante: ").append(tipoComprobante);
            if (documento != null && !documento.isBlank()) {
                obs.append(" | Doc: ").append(documento.trim());
            }
            if (razonSocial != null && !razonSocial.isBlank()) {
                obs.append(" | Cliente: ").append(razonSocial.trim());
            }
            pedido.setObservacion(obs.toString());

            for (int i = 0; i < productoIds.length; i++) {
                int cantidad = Integer.parseInt(cantidades[i]);
                if (cantidad <= 0) {
                    continue;
                }
                Producto producto = productosPorId.get(Integer.parseInt(productoIds[i]));
                if (producto == null) {
                    continue;
                }
                DetallePedido linea = new DetallePedido();
                linea.setProductoId(producto.getId());
                linea.setCantidad(cantidad);
                linea.setPrecioUnitario(producto.getPrecio());
                pedido.getDetalles().add(linea);
            }

            if (pedido.getDetalles().isEmpty()) {
                req.setAttribute("error", "El carrito esta vacio.");
                req.setAttribute("productos", productoDAO.listarActivos());
                req.getRequestDispatcher("/WEB-INF/views/pos.jsp").forward(req, resp);
                return;
            }

            Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
            Pedido creado = pedidoDAO.crear(pedido, usuario.getId());
            resp.sendRedirect(req.getContextPath() + "/app/pos?action=ticket&id=" + creado.getId());
        } catch (SQLException e) {
            throw new ServletException("Error procesando la venta", e);
        }
    }
}
