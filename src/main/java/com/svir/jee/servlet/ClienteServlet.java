package com.svir.jee.servlet;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.model.Cliente;

import com.svir.jee.util.Filtros;
import com.svir.jee.util.Documentos;
import com.svir.jee.util.Textos;
import com.svir.jee.util.Pagina;

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
    private final com.svir.jee.dao.InvitadoDAO invitadoDAO = new com.svir.jee.dao.InvitadoDAO();

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

            String q = Filtros.texto(req, "q");
            String estado = Filtros.opcion(req, "estado", "ACTIVO", "INACTIVO");
            final String fq = q.toLowerCase();
            final String festado = estado;
            var filtrados = clienteDAO.listarTodos().stream()
                    .filter(c -> fq.isEmpty()
                            || (c.getNombre() != null && c.getNombre().toLowerCase().contains(fq))
                            || (c.getDni() != null && c.getDni().toLowerCase().contains(fq))
                            || (c.getTelefono() != null && c.getTelefono().toLowerCase().contains(fq))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(fq)))
                    .filter(c -> festado.isEmpty() || c.isActivo() == "ACTIVO".equals(festado))
                    .toList();
            Pagina<com.svir.jee.model.Cliente> pg = Pagina.de(filtrados, req.getParameter("pagina"));
            pg.publicar(req, req.getContextPath() + "/app/clientes?" + Filtros.query("q", q, "estado", estado));
            req.setAttribute("fq", q);
            req.setAttribute("festado", estado);
            req.setAttribute("clientes", pg.getItems());
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
            c.setNombre(Textos.limpiar(req.getParameter("nombre"), 100));
            c.setTipoDocumento(Documentos.tipoValido(req.getParameter("tipoDocumento")));
            c.setDni(Documentos.normalizar(Textos.limpiar(req.getParameter("documento"), 20)));
            c.setRuc(vacioANull(Textos.limpiar(req.getParameter("ruc"), 11)));
            c.setTelefono(vacioANull(Textos.limpiar(req.getParameter("telefono"), 30)));
            c.setDireccion(vacioANull(Textos.limpiar(req.getParameter("direccion"), 255)));
            c.setEmail(vacioANull(Textos.limpiar(req.getParameter("email"), 150)));
            c.setActivo(true);

            // El documento es obligatorio y unico en todo el sistema (clientes e invitados).
            String errorDoc = c.getNombre().isEmpty() ? "El nombre es obligatorio." : Documentos.validar(c.getTipoDocumento(), c.getDni());
            if (errorDoc == null && clienteDAO.existeDocumento(c.getDni(), c.getId())) {
                errorDoc = "Ese documento ya pertenece a otro cliente.";
            }
            if (errorDoc == null && c.getId() != null
                    && invitadoDAO.buscarPorDocumento(c.getDni()).filter(i -> !i.isConvertido()).isPresent()) {
                errorDoc = "Ese documento pertenece a un invitado con pedidos. Registralo como cliente nuevo para vincular sus pedidos.";
            }
            if (errorDoc != null) {
                req.setAttribute("error", errorDoc);
                req.setAttribute("cliente", c);
                req.getRequestDispatcher("/WEB-INF/views/cliente-form.jsp").forward(req, resp);
                return;
            }

            if (c.getId() == null) {
                int vinculados = clienteDAO.altaConVinculo(c, false, "PERSONAL");
                resp.sendRedirect(req.getContextPath() + "/app/clientes" + (vinculados > 0 ? "?vinculados=" + vinculados : ""));
                return;
            }
            clienteDAO.actualizar(c);
            resp.sendRedirect(req.getContextPath() + "/app/clientes");
        } catch (SQLException e) {
            throw new ServletException("Error guardando el cliente", e);
        }
    }

    private String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
