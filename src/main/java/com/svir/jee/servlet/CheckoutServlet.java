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

import static com.svir.jee.util.Textos.limpiar;

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
            // Comprador sin cuenta ("invitado"): se guarda el nombre que escribio (ya limpio de caracteres peligrosos).
            String nombreInvitado = cliente == null ? limpiar(req.getParameter("nombre"), 150) : "";
            if (cliente != null) {
                pedido.setClienteId(cliente.getId());
            } else if (!nombreInvitado.isEmpty()) {
                pedido.setNombreInvitado(nombreInvitado);
            }

            String modoEntrega = req.getParameter("modoEntrega");
            if ("delivery".equals(modoEntrega)) {
                pedido.setTipoOrigen(TipoOrigenPedido.DELIVERY);
                String direccion = limpiar(req.getParameter("direccion"), 255);
                String telefono = limpiar(req.getParameter("telefono"), 30);
                String referencia = limpiar(req.getParameter("referencia"), 255);
                String gps = limpiar(req.getParameter("gps"), 60);
                StringBuilder obs = new StringBuilder();
                obs.append("Dir: ").append(direccion);
                obs.append(" | Tel: ").append(telefono);
                if (!referencia.isEmpty()) {
                    obs.append(" | Ref: ").append(referencia);
                }
                if (!gps.isEmpty()) {
                    obs.append(" | GPS: ").append(gps);
                }
                if (cliente != null) {
                    obs.append(" | ").append(limpiar(cliente.getNombre(), 150));
                } else {
                    obs.append(" | ").append(nombreInvitado.isEmpty() ? "Invitado" : nombreInvitado);
                }
                pedido.setObservacion(obs.toString());
            } else {
                pedido.setTipoOrigen(TipoOrigenPedido.TIENDA);
                if (cliente == null && !nombreInvitado.isEmpty()) {
                    pedido.setObservacion("Recojo en tienda | " + nombreInvitado);
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
