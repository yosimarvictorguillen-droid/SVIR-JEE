package com.svir.jee.servlet;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.util.PasswordUtil;
import com.svir.jee.util.Textos;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/** Autenticacion de clientes en la tienda web publica: registro, login y logout (sesion separada de "usuario"). */
@WebServlet({"/cliente/login", "/cliente/registro", "/cliente/logout"})
public class ClienteAuthServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getServletPath().endsWith("/logout")) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute("cliente");
            }
            resp.sendRedirect(req.getContextPath() + "/home");
        } else {
            resp.sendRedirect(req.getContextPath() + "/catalogo");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        String volverA = req.getParameter("volverA");
        // Solo se permite volver a una ruta relativa propia del sitio, nunca a una URL externa.
        if (volverA == null || volverA.isBlank() || !volverA.startsWith("/") || volverA.startsWith("//")
                || volverA.contains("://")) {
            volverA = "/catalogo";
        }

        try {
            if (path.endsWith("/registro")) {
                registrar(req, resp, volverA);
            } else if (path.endsWith("/login")) {
                login(req, resp, volverA);
            }
        } catch (SQLException e) {
            throw new ServletException("Error de autenticacion de cliente", e);
        }
    }

    private void registrar(HttpServletRequest req, HttpServletResponse resp, String volverA)
            throws IOException, SQLException {
        // Texto libre de la persona: se limpia para que no pueda inyectar HTML en las pantallas del personal.
        String nombre = Textos.limpiar(req.getParameter("nombre"), 150);
        String dni = Textos.limpiar(req.getParameter("dni"), 20);
        String password = req.getParameter("password");

        if (nombre.isEmpty()) {
            redirigirConError(req, resp, volverA, "El nombre es obligatorio.");
            return;
        }
        if (dni.isEmpty()) {
            redirigirConError(req, resp, volverA, "El DNI es obligatorio.");
            return;
        }
        if (clienteDAO.existeDni(dni)) {
            redirigirConError(req, resp, volverA, "Ya existe una cuenta con ese DNI. Intenta iniciar sesion.");
            return;
        }
        if (password == null || password.length() < 6) {
            redirigirConError(req, resp, volverA, "La contrasena debe tener al menos 6 caracteres.");
            return;
        }

        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setDni(dni);
        c.setTelefono(Textos.limpiar(req.getParameter("telefono"), 30));
        c.setDireccion(Textos.limpiar(req.getParameter("direccion"), 255));
        c.setEmail(Textos.limpiar(req.getParameter("email"), 150));
        c.setPasswordHash(PasswordUtil.hash(password));
        clienteDAO.registrar(c);

        req.getSession(true).setAttribute("cliente", c);
        resp.sendRedirect(req.getContextPath() + volverA);
    }

    private void login(HttpServletRequest req, HttpServletResponse resp, String volverA)
            throws IOException, SQLException {
        String dni = req.getParameter("dni");
        String password = req.getParameter("password");

        Optional<Cliente> encontrado = clienteDAO.buscarPorDni(dni);
        if (encontrado.isEmpty() || encontrado.get().getPasswordHash() == null
                || !PasswordUtil.verificar(password, encontrado.get().getPasswordHash())
                || !encontrado.get().isActivo()) {
            redirigirConError(req, resp, volverA, "DNI o contrasena incorrectos.");
            return;
        }

        req.getSession(true).setAttribute("cliente", encontrado.get());
        resp.sendRedirect(req.getContextPath() + volverA);
    }

    private void redirigirConError(HttpServletRequest req, HttpServletResponse resp, String volverA, String mensaje)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + volverA + (volverA.contains("?") ? "&" : "?")
                + "authError=" + java.net.URLEncoder.encode(mensaje, java.nio.charset.StandardCharsets.UTF_8));
    }
}
