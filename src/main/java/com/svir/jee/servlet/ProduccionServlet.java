package com.svir.jee.servlet;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProduccionDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.ProduccionDetalle;
import com.svir.jee.model.Usuario;

import com.svir.jee.util.Filtros;
import com.svir.jee.util.Pagina;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Ordenes de produccion de cocina: para reponer stock o para atender un pedido con faltante. */
@WebServlet("/app/producciones")
public class ProduccionServlet extends HttpServlet {

    private final ProduccionDAO produccionDAO = new ProduccionDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "nueva" -> {
                    req.setAttribute("productos", productoDAO.listarActivos());
                    req.getRequestDispatcher("/WEB-INF/views/produccion-form.jsp").forward(req, resp);
                }
                case "porPedido" -> {
                    req.setAttribute("pedidosConFaltante", pedidoDAO.listarConFaltante());
                    req.getRequestDispatcher("/WEB-INF/views/produccion-por-pedido.jsp").forward(req, resp);
                }
                case "ver" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    produccionDAO.buscarPorId(id).ifPresentOrElse(
                            p -> req.setAttribute("produccion", p),
                            () -> req.setAttribute("error", "Orden de produccion no encontrada"));
                    req.getRequestDispatcher("/WEB-INF/views/produccion-detalle.jsp").forward(req, resp);
                }
                default -> {
                    String q = Filtros.texto(req, "q");
                    String tipo = Filtros.opcion(req, "tipo", "PEDIDO", "STOCK");
                    String estado = Filtros.opcion(req, "estado", "PENDIENTE", "EN_PROCESO", "TERMINADO", "CANCELADO");
                    java.time.LocalDate desde = Filtros.fecha(req, "desde");
                    java.time.LocalDate hasta = Filtros.fecha(req, "hasta");
                    final String fq = q.toLowerCase().replace("#", "");
                    final String ftipo = tipo;
                    final String festado = estado;
                    var filtrados = produccionDAO.listarTodos().stream()
                            .filter(p -> fq.isEmpty() || String.valueOf(p.getId()).equals(fq)
                                    || (p.getPedidoId() != null && String.valueOf(p.getPedidoId()).equals(fq))
                                    || (p.getUsuarioNombre() != null && p.getUsuarioNombre().toLowerCase().contains(fq)))
                            .filter(p -> ftipo.isEmpty() || p.getTipo().name().equals(ftipo))
                            .filter(p -> festado.isEmpty() || p.getEstado().name().equals(festado))
                            .filter(p -> desde == null || (p.getCreatedAt() != null && !p.getCreatedAt().toLocalDate().isBefore(desde)))
                            .filter(p -> hasta == null || (p.getCreatedAt() != null && !p.getCreatedAt().toLocalDate().isAfter(hasta)))
                            .toList();
                    Pagina<com.svir.jee.model.Produccion> pg = Pagina.de(filtrados, req.getParameter("pagina"));
                    pg.publicar(req, req.getContextPath() + "/app/producciones?" + Filtros.query("q", q, "tipo", tipo,
                            "estado", estado, "desde", desde == null ? "" : desde.toString(),
                            "hasta", hasta == null ? "" : hasta.toString()));
                    req.setAttribute("fq", q);
                    req.setAttribute("ftipo", tipo);
                    req.setAttribute("festado", estado);
                    req.setAttribute("fdesde", desde == null ? "" : desde.toString());
                    req.setAttribute("fhasta", hasta == null ? "" : hasta.toString());
                    req.setAttribute("producciones", pg.getItems());
                    req.getRequestDispatcher("/WEB-INF/views/producciones.jsp").forward(req, resp);
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a producciones", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
        try {
            switch (action == null ? "" : action) {
                case "crearPorPedido" -> {
                    int pedidoId = Integer.parseInt(req.getParameter("pedidoId"));
                    var creada = produccionDAO.crearPorPedido(pedidoId, usuario.getId());
                    resp.sendRedirect(req.getContextPath() + "/app/producciones?action=ver&id=" + creada.getId());
                }
                case "terminar" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    String[] detalleIds = req.getParameterValues("detalleId");
                    String[] producidas = req.getParameterValues("cantidadProducida");
                    Map<Integer, Integer> cantidades = new HashMap<>();
                    if (detalleIds != null) {
                        for (int i = 0; i < detalleIds.length; i++) {
                            int cantidad = producidas[i].isBlank() ? 0 : Integer.parseInt(producidas[i]);
                            cantidades.put(Integer.parseInt(detalleIds[i]), cantidad);
                        }
                    }
                    produccionDAO.terminar(id, cantidades, usuario.getId());
                    resp.sendRedirect(req.getContextPath() + "/app/producciones?action=ver&id=" + id);
                }
                case "cancelar" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    produccionDAO.cancelar(id, usuario.getId());
                    resp.sendRedirect(req.getContextPath() + "/app/producciones");
                }
                default -> crearParaStock(req, resp, usuario.getId());
            }
        } catch (SQLException e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("producciones", produccionDAO.listarTodos());
            } catch (SQLException ignored) {
                // si tampoco se puede listar, se muestra el error igual sin la tabla
            }
            req.getRequestDispatcher("/WEB-INF/views/producciones.jsp").forward(req, resp);
        }
    }

    private void crearParaStock(HttpServletRequest req, HttpServletResponse resp, int usuarioId)
            throws ServletException, IOException, SQLException {
        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");

        List<ProduccionDetalle> detalles = new ArrayList<>();
        if (productoIds != null) {
            for (int i = 0; i < productoIds.length; i++) {
                if (productoIds[i].isBlank() || cantidades[i].isBlank()) {
                    continue;
                }
                int cantidad = Integer.parseInt(cantidades[i]);
                if (cantidad <= 0) {
                    continue;
                }
                ProduccionDetalle pd = new ProduccionDetalle();
                pd.setProductoId(Integer.parseInt(productoIds[i]));
                pd.setCantidadPlanificada(cantidad);
                detalles.add(pd);
            }
        }

        if (detalles.isEmpty()) {
            req.setAttribute("error", "Debes indicar al menos un producto con cantidad a producir.");
            req.setAttribute("productos", productoDAO.listarActivos());
            req.getRequestDispatcher("/WEB-INF/views/produccion-form.jsp").forward(req, resp);
            return;
        }

        var creada = produccionDAO.crearParaStock(detalles, usuarioId);
        resp.sendRedirect(req.getContextPath() + "/app/producciones?action=ver&id=" + creada.getId());
    }
}
