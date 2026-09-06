package com.svir.jee.servlet;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.model.Cliente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Seguimiento de pedidos para clientes: si hay sesion de cliente, muestra su
 * historial completo; si no, permite buscar un pedido puntual por numero
 * (para invitados que guardaron el link de seguimiento).
 */
@WebServlet("/mis-pedidos")
public class MisPedidosServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Cliente cliente = (Cliente) req.getSession(true).getAttribute("cliente");
            if (cliente != null) {
                req.setAttribute("pedidos", pedidoDAO.listarPorCliente(cliente.getId()));
            }

            String idParam = req.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                pedidoDAO.buscarPorId(Integer.parseInt(idParam))
                        .ifPresent(p -> req.setAttribute("pedidoBuscado", p));
            }

            req.setAttribute("rutaActual", "/mis-pedidos");
            req.getRequestDispatcher("/WEB-INF/views/tienda/mis-pedidos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error consultando tus pedidos", e);
        }
    }
}
