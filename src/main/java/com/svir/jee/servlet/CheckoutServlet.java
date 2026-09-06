package com.svir.jee.servlet;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;
import com.svir.jee.model.TipoOrigenPedido;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Checkout de la tienda web: recojo en tienda o delivery con direccion/GPS. */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Map<Integer, Integer> carrito = CarritoServlet.obtenerCarrito(req.getSession(true));
            if (carrito.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/carrito");
                return;
            }
            Map<Producto, Integer> lineas = new LinkedHashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<Integer, Integer> e : carrito.entrySet()) {
                var producto = productoDAO.buscarPorId(e.getKey());
                if (producto.isPresent()) {
                    lineas.put(producto.get(), e.getValue());
                    total = total.add(producto.get().getPrecio().multiply(BigDecimal.valueOf(e.getValue())));
                }
            }
            req.setAttribute("lineas", lineas);
            req.setAttribute("total", total);
            req.setAttribute("rutaActual", "/checkout");
            req.getRequestDispatcher("/WEB-INF/views/tienda/checkout.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error preparando el checkout", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(true);
        Map<Integer, Integer> carrito = CarritoServlet.obtenerCarrito(session);
        if (carrito.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/carrito");
            return;
        }

        try {
            Pedido pedido = new Pedido();
            Cliente cliente = (Cliente) session.getAttribute("cliente");
            if (cliente != null) {
                pedido.setClienteId(cliente.getId());
            }

            String modoEntrega = req.getParameter("modoEntrega");
            if ("delivery".equals(modoEntrega)) {
                pedido.setTipoOrigen(TipoOrigenPedido.DELIVERY);
                String direccion = req.getParameter("direccion");
                String telefono = req.getParameter("telefono");
                String referencia = req.getParameter("referencia");
                String gps = req.getParameter("gps");
                StringBuilder obs = new StringBuilder();
                obs.append("Dir: ").append(direccion == null ? "" : direccion.trim());
                obs.append(" | Tel: ").append(telefono == null ? "" : telefono.trim());
                if (referencia != null && !referencia.isBlank()) {
                    obs.append(" | Ref: ").append(referencia.trim());
                }
                if (gps != null && !gps.isBlank()) {
                    obs.append(" | GPS: ").append(gps.trim());
                }
                if (cliente != null) {
                    obs.append(" | ").append(cliente.getNombre());
                } else {
                    String nombreInvitado = req.getParameter("nombre");
                    obs.append(" | ").append(nombreInvitado == null ? "Invitado" : nombreInvitado.trim());
                }
                pedido.setObservacion(obs.toString());
            } else {
                pedido.setTipoOrigen(TipoOrigenPedido.TIENDA);
                String nombreInvitado = req.getParameter("nombre");
                if (cliente == null && nombreInvitado != null && !nombreInvitado.isBlank()) {
                    pedido.setObservacion("Recojo en tienda | " + nombreInvitado.trim());
                } else {
                    pedido.setObservacion("Recojo en tienda");
                }
            }

            Map<Integer, Producto> productosPorId = new LinkedHashMap<>();
            for (Producto p : productoDAO.listarActivos()) {
                productosPorId.put(p.getId(), p);
            }
            for (Map.Entry<Integer, Integer> e : carrito.entrySet()) {
                Producto producto = productosPorId.get(e.getKey());
                if (producto == null) {
                    continue;
                }
                DetallePedido linea = new DetallePedido();
                linea.setProductoId(producto.getId());
                linea.setCantidad(e.getValue());
                linea.setPrecioUnitario(producto.getPrecio());
                pedido.getDetalles().add(linea);
            }

            if (pedido.getDetalles().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/carrito");
                return;
            }

            Pedido creado = pedidoDAO.crear(pedido, null);
            carrito.clear();
            resp.sendRedirect(req.getContextPath() + "/mis-pedidos?id=" + creado.getId());
        } catch (SQLException e) {
            throw new ServletException("Error registrando el pedido", e);
        }
    }
}
