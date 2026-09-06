package com.svir.jee.servlet;

import com.svir.jee.dao.UsuarioDAO;
import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** CRUD de personal interno (usuarios del sistema, no clientes). Solo ADMIN. */
@WebServlet("/app/usuarios")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("nuevo".equals(action)) {
                req.getRequestDispatcher("/WEB-INF/views/usuario-form.jsp").forward(req, resp);
                return;
            }
            if ("editar".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                usuarioDAO.buscarPorId(id).ifPresentOrElse(
                        u -> req.setAttribute("usuarioEditar", u),
                        () -> req.setAttribute("error", "Usuario no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/usuario-form.jsp").forward(req, resp);
                return;
            }

            req.setAttribute("usuarios", usuarioDAO.listarTodos());
            req.getRequestDispatcher("/WEB-INF/views/usuarios.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a usuarios", e);
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
                usuarioDAO.cambiarActivo(id, activo);
                resp.sendRedirect(req.getContextPath() + "/app/usuarios");
                return;
            }

            String idParam = req.getParameter("id");
            String password = req.getParameter("password");

            if (idParam != null && !idParam.isBlank()) {
                Usuario u = new Usuario();
                u.setId(Integer.parseInt(idParam));
                u.setNombre(req.getParameter("nombre"));
                u.setEmail(req.getParameter("email"));
                u.setRol(RolUsuario.valueOf(req.getParameter("rol")));
                u.setTelefono(req.getParameter("telefono"));
                usuarioDAO.actualizar(u);
                if (password != null && !password.isBlank()) {
                    usuarioDAO.actualizarPassword(u.getId(), PasswordUtil.hash(password));
                }
            } else {
                Usuario u = new Usuario();
                u.setNombre(req.getParameter("nombre"));
                u.setEmail(req.getParameter("email"));
                u.setPasswordHash(PasswordUtil.hash(password));
                u.setRol(RolUsuario.valueOf(req.getParameter("rol")));
                u.setTelefono(req.getParameter("telefono"));
                u.setActivo(true);
                usuarioDAO.crear(u);
            }
            resp.sendRedirect(req.getContextPath() + "/app/usuarios");
        } catch (SQLException e) {
            throw new ServletException("Error guardando el usuario", e);
        }
    }
}
