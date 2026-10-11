package com.svir.jee.servlet;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.InvitadoDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.Invitado;
import com.svir.jee.util.Documentos;
import com.svir.jee.util.LimiteIntentos;
import com.svir.jee.util.PasswordUtil;
import com.svir.jee.util.Textos;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Autenticacion de clientes en la tienda web publica: registro, login y logout (sesion separada de "usuario").
 * El documento (DNI, carne de extranjeria o pasaporte) es el usuario y es unico. Si quien se registra ya habia
 * comprado como invitado con ese documento, debe demostrar que esas compras son suyas (numero y total de uno de sus
 * pedidos) y entonces sus pedidos pasan a su cuenta.
 */
@WebServlet({"/cliente/login", "/cliente/registro", "/cliente/logout"})
public class ClienteAuthServlet extends HttpServlet {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final InvitadoDAO invitadoDAO = new InvitadoDAO();

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
        String tipo = Documentos.tipoValido(req.getParameter("tipoDocumento"));
        String documento = Documentos.normalizar(Textos.limpiar(req.getParameter("documento"), 20));
        String password = req.getParameter("password");

        if (nombre.isEmpty()) {
            redirigirConError(req, resp, volverA, "El nombre es obligatorio.");
            return;
        }
        String errorDocumento = Documentos.validar(tipo, documento);
        if (errorDocumento != null) {
            redirigirConError(req, resp, volverA, errorDocumento);
            return;
        }
        if (clienteDAO.existeDni(documento)) {
            redirigirConError(req, resp, volverA, "Ya existe una cuenta con ese documento. Intenta iniciar sesion.");
            return;
        }
        if (password == null || password.length() < 6) {
            redirigirConError(req, resp, volverA, "La contrasena debe tener al menos 6 caracteres.");
            return;
        }

        // Si ya compro como invitado, debe demostrar que esas compras son suyas antes de vincularlas a la cuenta.
        String validacion = "SIN_HISTORIAL";
        Optional<Invitado> invitado = invitadoDAO.buscarPorDocumento(documento);
        if (invitado.isPresent() && !invitado.get().isConvertido()) {
            int pedidosPrevios = invitadoDAO.contarPedidos(invitado.get().getId());
            if (pedidosPrevios > 0) {
                String clave = "registro:" + documento;
                if (LimiteIntentos.bloqueado(clave)) {
                    redirigirConError(req, resp, volverA,
                            "Demasiados intentos. Espera unos minutos o comunicate con la tienda.");
                    return;
                }
                Integer pedidoRef = parsearEntero(req.getParameter("pedidoRef"));
                BigDecimal totalRef = parsearMonto(req.getParameter("totalRef"));
                if (pedidoRef == null || totalRef == null
                        || !invitadoDAO.verificarPedido(invitado.get().getId(), pedidoRef, totalRef)) {
                    LimiteIntentos.fallo(clave);
                    redirigirConError(req, resp, volverA, "Ya compraste como invitado con este documento ("
                            + pedidosPrevios + " pedido(s)). Para vincularlos a tu cuenta escribe el numero y el total "
                            + "de uno de tus pedidos, tal como figuran en tu comprobante.");
                    return;
                }
                LimiteIntentos.limpiar(clave);
                validacion = "PEDIDO_ANTERIOR";
            }
        }

        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setTipoDocumento(tipo);
        c.setDni(documento);
        c.setTelefono(Textos.limpiar(req.getParameter("telefono"), 30));
        c.setDireccion(Textos.limpiar(req.getParameter("direccion"), 255));
        c.setEmail(Textos.limpiar(req.getParameter("email"), 150));
        c.setPasswordHash(PasswordUtil.hash(password));
        int vinculados = clienteDAO.altaConVinculo(c, true, validacion);

        HttpSession session = req.getSession(true);
        session.setAttribute("cliente", c);
        session.removeAttribute("invitadoReciente");
        resp.sendRedirect(req.getContextPath() + volverA + (volverA.contains("?") ? "&" : "?") + "bienvenida=" + vinculados);
    }

    private void login(HttpServletRequest req, HttpServletResponse resp, String volverA)
            throws IOException, SQLException {
        String documento = Documentos.normalizar(req.getParameter("documento"));
        String password = req.getParameter("password");

        Optional<Cliente> encontrado = clienteDAO.buscarPorDni(documento);
        if (encontrado.isEmpty() || encontrado.get().getPasswordHash() == null
                || !PasswordUtil.verificar(password, encontrado.get().getPasswordHash())
                || !encontrado.get().isActivo()) {
            redirigirConError(req, resp, volverA, "Documento o contrasena incorrectos.");
            return;
        }

        req.getSession(true).setAttribute("cliente", encontrado.get());
        resp.sendRedirect(req.getContextPath() + volverA);
    }

    private static Integer parsearEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parsearMonto(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : new BigDecimal(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void redirigirConError(HttpServletRequest req, HttpServletResponse resp, String volverA, String mensaje)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + volverA + (volverA.contains("?") ? "&" : "?")
                + "authError=" + java.net.URLEncoder.encode(mensaje, java.nio.charset.StandardCharsets.UTF_8));
    }
}
