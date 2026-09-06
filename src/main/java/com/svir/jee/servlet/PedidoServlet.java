package com.svir.jee.servlet;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.EstadoPedido;
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

/**
 * Gestion de pedidos: alta con multiples lineas, cambio de estado y
 * cancelacion con devolucion de stock. La creacion y la cancelacion
 * delegan en PedidoDAO, que las ejecuta como una unica transaccion JDBC.
 */
@WebServlet("/app/pedidos")
public class PedidoServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("nuevo".equals(action)) {
                req.setAttribute("productos", productoDAO.listarActivos());
                req.setAttribute("clientes", clienteDAO.listarActivos());
                req.getRequestDispatcher("/WEB-INF/views/pedido-form.jsp").forward(req, resp);
                return;
            }
            if ("ver".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                pedidoDAO.buscarPorId(id).ifPresentOrElse(
                        p -> req.setAttribute("pedido", p),
                        () -> req.setAttribute("error", "Pedido no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/pedido-detalle.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("pedidos", pedidoDAO.listarTodos());
            req.getRequestDispatcher("/WEB-INF/views/pedidos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a pedidos", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "cambiarEstado" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    EstadoPedido estado = EstadoPedido.valueOf(req.getParameter("estado"));
                    pedidoDAO.cambiarEstado(id, estado);
                    resp.sendRedirect(req.getContextPath() + "/app/pedidos");
                }
                case "cancelar" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
                    pedidoDAO.cancelar(id, usuario.getId());
                    resp.sendRedirect(req.getContextPath() + "/app/pedidos");
                }
                default -> guardarNuevoPedido(req, resp);
            }
        } catch (SQLException e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("pedidos", pedidoDAO.listarTodos());
            } catch (SQLException ignored) {
                // si tampoco se puede listar, se muestra el error igual sin la tabla
            }
            req.getRequestDispatcher("/WEB-INF/views/pedidos.jsp").forward(req, resp);
        }
    }

    private void guardarNuevoPedido(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");

        if (productoIds == null || productoIds.length == 0) {
            req.setAttribute("error", "Debes agregar al menos un producto al pedido.");
            req.setAttribute("productos", productoDAO.listarActivos());
            req.setAttribute("clientes", clienteDAO.listarActivos());
            req.getRequestDispatcher("/WEB-INF/views/pedido-form.jsp").forward(req, resp);
            return;
        }

        Map<Integer, Producto> productosPorId = new HashMap<>();
        for (Producto p : productoDAO.listarActivos()) {
            productosPorId.put(p.getId(), p);
        }

        Pedido pedido = new Pedido();
        String clienteIdParam = req.getParameter("clienteId");
        if (clienteIdParam != null && !clienteIdParam.isBlank()) {
            pedido.setClienteId(Integer.parseInt(clienteIdParam));
        }
        pedido.setTipoOrigen(TipoOrigenPedido.valueOf(req.getParameter("tipoOrigen")));
        pedido.setObservacion(req.getParameter("observacion"));

        for (int idx = 0; idx < productoIds.length; idx++) {
            if (productoIds[idx].isBlank()) {
                continue;
            }
            int productoId = Integer.parseInt(productoIds[idx]);
            int cantidad = Integer.parseInt(cantidades[idx]);
            if (cantidad <= 0) {
                continue;
            }
            Producto producto = productosPorId.get(productoId);
            if (producto == null) {
                continue;
            }

            DetallePedido linea = new DetallePedido();
            linea.setProductoId(productoId);
            linea.setCantidad(cantidad);
            linea.setPrecioUnitario(producto.getPrecio());
            pedido.getDetalles().add(linea);
        }

        if (pedido.getDetalles().isEmpty()) {
            req.setAttribute("error", "Debes agregar al menos un producto valido al pedido.");
            req.setAttribute("productos", productoDAO.listarActivos());
            req.setAttribute("clientes", clienteDAO.listarActivos());
            req.getRequestDispatcher("/WEB-INF/views/pedido-form.jsp").forward(req, resp);
            return;
        }

        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
        Pedido creado = pedidoDAO.crear(pedido, usuario.getId());
        resp.sendRedirect(req.getContextPath() + "/app/pedidos?action=ver&id=" + creado.getId());
    }
}
