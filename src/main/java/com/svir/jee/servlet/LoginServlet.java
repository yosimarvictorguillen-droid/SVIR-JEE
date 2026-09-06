package com.svir.jee.servlet;

import com.svir.jee.dao.UsuarioDAO;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.PasswordUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/** Muestra el formulario de login y valida credenciales contra la tabla usuarios. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            resp.sendRedirect(req.getContextPath() + "/app/dashboard");
            return;
        }
        RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/views/login.jsp");
        rd.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorEmail(email);
            if (encontrado.isEmpty()
                    || !encontrado.get().isActivo()
                    || !PasswordUtil.verificar(password, encontrado.get().getPasswordHash())) {
                req.setAttribute("error", "Correo o contrasena incorrectos.");
                req.setAttribute("email", email);
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            Usuario usuario = encontrado.get();
            HttpSession session = req.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setMaxInactiveInterval(60 * 60); // 1 hora

            resp.sendRedirect(req.getContextPath() + "/app/dashboard");
        } catch (SQLException e) {
            req.setAttribute("error", "Error de conexion con la base de datos: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
