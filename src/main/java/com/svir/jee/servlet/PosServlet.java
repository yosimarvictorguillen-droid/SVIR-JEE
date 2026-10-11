package com.svir.jee.servlet;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;
import com.svir.jee.model.TipoOrigenPedido;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.Textos;

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
    private final ClienteDAO clienteDAO = new ClienteDAO();

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

            mostrarFormulario(req, resp, null);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo al POS", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");

        try {
            if (productoIds == null || productoIds.length == 0) {
                mostrarFormulario(req, resp, "El carrito esta vacio.");
                return;
            }

            // El comprador es obligatorio: un cliente registrado o el nombre de un invitado.
            String clienteIdParam = req.getParameter("clienteId");
            boolean hayCliente = clienteIdParam != null && !clienteIdParam.isBlank();
            String nombreInvitado = Textos.limpiar(req.getParameter("nombreComprador"), 150);
            if (!hayCliente && nombreInvitado.isEmpty()) {
                mostrarFormulario(req, resp, "El nombre del comprador es obligatorio (elige un cliente registrado o escribe el nombre del invitado).");
                return;
            }

            // Boleta con DNI exige 8 digitos y factura 11 (RUC); se valida antes de registrar la venta.
            String tipoComprobante = req.getParameter("tipoComprobante");
            String documento = Textos.limpiar(req.getParameter("documento"), 20);
            String errorComprobante = PedidoDAO.validarComprobante(tipoComprobante, documento);
            if (errorComprobante != null) {
                mostrarFormulario(req, resp, errorComprobante);
                return;
            }

            Map<Integer, Producto> productosPorId = new HashMap<>();
            for (Producto p : productoDAO.listarActivos()) {
                productosPorId.put(p.getId(), p);
            }

            Pedido pedido = new Pedido();
            pedido.setTipoOrigen(TipoOrigenPedido.PRESENCIAL);
            if (hayCliente) {
                pedido.setClienteId(Integer.parseInt(clienteIdParam));
            } else {
                pedido.setNombreInvitado(nombreInvitado);
            }

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
                mostrarFormulario(req, resp, "El carrito esta vacio.");
                return;
            }

            Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
            Pedido creado = pedidoDAO.crear(pedido, usuario.getId());
            pedidoDAO.emitirComprobante(creado.getId(), tipoComprobante, documento);
            resp.sendRedirect(req.getContextPath() + "/app/pos?action=ticket&id=" + creado.getId());
        } catch (SQLException e) {
            throw new ServletException("Error procesando la venta", e);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, String error)
            throws ServletException, IOException, SQLException {
        if (error != null) {
            req.setAttribute("error", error);
        }
        req.setAttribute("productos", productoDAO.listarActivos());
        req.setAttribute("clientes", clienteDAO.listarActivos());
        req.getRequestDispatcher("/WEB-INF/views/pos.jsp").forward(req, resp);
    }
}
