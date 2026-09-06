package com.svir.jee.servlet;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.model.EstadoPedido;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Panel de reparto: pedidos delivery listos para recoger, en camino y entregados hoy. */
@WebServlet("/app/repartidor")
public class RepartidorServlet extends HttpServlet {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("pedidosDelivery", pedidoDAO.listarDelivery());
            req.getRequestDispatcher("/WEB-INF/views/repartidor.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo al panel de repartidor", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            EstadoPedido nuevoEstado = EstadoPedido.valueOf(req.getParameter("estado"));
            pedidoDAO.cambiarEstado(id, nuevoEstado);
            resp.sendRedirect(req.getContextPath() + "/app/repartidor");
        } catch (SQLException e) {
            throw new ServletException("Error actualizando el pedido", e);
        }
    }
}
