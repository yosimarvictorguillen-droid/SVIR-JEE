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
import com.svir.jee.util.Textos;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
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

            // Filtros que llegan desde las tarjetas del dashboard (?filtro=hoy|mes|pendientes|listos).
            List<Pedido> lista = pedidoDAO.listarTodos();
            String filtro = req.getParameter("filtro");
            LocalDate hoy = LocalDate.now();
            String titulo = null;
            if ("hoy".equals(filtro)) {
                titulo = "Pedidos de hoy";
                lista = lista.stream().filter(p -> p.getEstado() != EstadoPedido.CANCELADO
                        && p.getCreatedAt().toLocalDate().equals(hoy)).toList();
            } else if ("mes".equals(filtro)) {
                titulo = "Pedidos del mes";
                lista = lista.stream().filter(p -> p.getEstado() != EstadoPedido.CANCELADO
                        && p.getCreatedAt().getYear() == hoy.getYear()
                        && p.getCreatedAt().getMonth() == hoy.getMonth()).toList();
            } else if ("pendientes".equals(filtro)) {
                titulo = "Pedidos pendientes / en preparacion";
                lista = lista.stream().filter(p -> p.getEstado() == EstadoPedido.PENDIENTE
                        || p.getEstado() == EstadoPedido.PREPARACION).toList();
            } else if ("listos".equals(filtro)) {
                titulo = "Pedidos listos";
                lista = lista.stream().filter(p -> p.getEstado() == EstadoPedido.LISTO).toList();
            }
            req.setAttribute("filtroTitulo", titulo);
            req.setAttribute("pedidos", lista);
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
                case "emitirComprobante" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    String tipo = req.getParameter("tipoComprobante");
                    String documento = Textos.limpiar(req.getParameter("documento"), 20);
                    String error = PedidoDAO.validarComprobante(tipo, documento);
                    if (error == null) {
                        try {
                            pedidoDAO.emitirComprobante(id, tipo, documento);
                            resp.sendRedirect(req.getContextPath() + "/app/pos?action=ticket&id=" + id);
                            return;
                        } catch (SQLException e) {
                            error = e.getMessage();
                        }
                    }
                    pedidoDAO.buscarPorId(id).ifPresent(p -> req.setAttribute("pedido", p));
                    req.setAttribute("error", error);
                    req.getRequestDispatcher("/WEB-INF/views/pedido-detalle.jsp").forward(req, resp);
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
        } else {
            // Sin cliente registrado el nombre del invitado es obligatorio.
            String nombre = com.svir.jee.util.Textos.limpiar(req.getParameter("nombreInvitado"), 150);
            if (nombre.isEmpty()) {
                req.setAttribute("error", "El nombre del comprador es obligatorio (elige un cliente registrado o escribe el nombre del invitado).");
                req.setAttribute("productos", productoDAO.listarActivos());
                req.setAttribute("clientes", clienteDAO.listarActivos());
                req.getRequestDispatcher("/WEB-INF/views/pedido-form.jsp").forward(req, resp);
                return;
            }
            pedido.setNombreInvitado(nombre);
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
