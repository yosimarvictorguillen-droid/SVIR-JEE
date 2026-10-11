package com.svir.jee.servlet;

import com.svir.jee.dao.InvitadoDAO;
import com.svir.jee.model.Invitado;
import com.svir.jee.util.Filtros;
import com.svir.jee.util.Pagina;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Invitados: compradores sin cuenta identificados por su documento (unico), con su resumen de compras y, para los que
 * ya se registraron como cliente, la fecha de conversion (trazabilidad). Pantalla de solo lectura.
 */
@WebServlet("/app/invitados")
public class InvitadoServlet extends HttpServlet {

    private final InvitadoDAO invitadoDAO = new InvitadoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String q = Filtros.texto(req, "q");
            String estado = Filtros.opcion(req, "estado", "ACTIVO", "CONVERTIDO");
            final String fq = q.toLowerCase();
            final String festado = estado;
            var filtrados = invitadoDAO.listarConResumen().stream()
                    .filter(i -> fq.isEmpty()
                            || (i.getNombre() != null && i.getNombre().toLowerCase().contains(fq))
                            || (i.getDocumento() != null && i.getDocumento().toLowerCase().contains(fq))
                            || (i.getTelefono() != null && i.getTelefono().toLowerCase().contains(fq)))
                    .filter(i -> festado.isEmpty() || i.getEstado().equals(festado))
                    .toList();
            Pagina<Invitado> pg = Pagina.de(filtrados, req.getParameter("pagina"));
            pg.publicar(req, req.getContextPath() + "/app/invitados?" + Filtros.query("q", q, "estado", estado));
            req.setAttribute("fq", q);
            req.setAttribute("festado", estado);
            req.setAttribute("invitados", pg.getItems());
            req.getRequestDispatcher("/WEB-INF/views/invitados.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a invitados", e);
        }
    }
}
