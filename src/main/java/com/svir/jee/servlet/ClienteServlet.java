package com.svir.jee.servlet;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.model.Cliente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/app/clientes")
public class ClienteServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("nuevo".equals(action)) {
                req.getRequestDispatcher("/WEB-INF/views/cliente-form.jsp").forward(req, resp);
                return;
            }
            if ("editar".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                clienteDAO.buscarPorId(id).ifPresentOrElse(
                        c -> req.setAttribute("cliente", c),
                        () -> req.setAttribute("error", "Cliente no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/cliente-form.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("clientes", clienteDAO.listarTodos());
            req.getRequestDispatcher("/WEB-INF/views/clientes.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a clientes", e);
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
                clienteDAO.cambiarActivo(id, activo);
                resp.sendRedirect(req.getContextPath() + "/app/clientes");
                return;
            }

            String idParam = req.getParameter("id");
            Cliente c = new Cliente();
            if (idParam != null && !idParam.isBlank()) {
                c.setId(Integer.parseInt(idParam));
            }
            c.setNombre(req.getParameter("nombre"));
            c.setDni(vacioANull(req.getParameter("dni")));
            c.setRuc(vacioANull(req.getParameter("ruc")));
            c.setTelefono(vacioANull(req.getParameter("telefono")));
            c.setDireccion(vacioANull(req.getParameter("direccion")));
            c.setEmail(vacioANull(req.getParameter("email")));
            c.setActivo(true);

            if (c.getId() == null) {
                clienteDAO.crear(c);
            } else {
                clienteDAO.actualizar(c);
            }
            resp.sendRedirect(req.getContextPath() + "/app/clientes");
        } catch (SQLException e) {
            throw new ServletException("Error guardando el cliente", e);
        }
    }

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
