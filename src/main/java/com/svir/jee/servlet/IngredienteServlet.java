package com.svir.jee.servlet;

import com.svir.jee.dao.IngredienteDAO;
import com.svir.jee.model.Ingrediente;
import com.svir.jee.model.MotivoMovimientoIngrediente;
import com.svir.jee.model.TipoMovimiento;
import com.svir.jee.model.Usuario;

import com.svir.jee.util.Filtros;
import com.svir.jee.util.Pagina;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/app/ingredientes")
public class IngredienteServlet extends HttpServlet {

    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("nuevo".equals(action)) {
                req.getRequestDispatcher("/WEB-INF/views/ingrediente-form.jsp").forward(req, resp);
                return;
            }
            if ("editar".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                ingredienteDAO.buscarPorId(id).ifPresentOrElse(
                        i -> req.setAttribute("ingrediente", i),
                        () -> req.setAttribute("error", "Ingrediente no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/ingrediente-form.jsp").forward(req, resp);
                return;
            }
            if ("movimiento".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                ingredienteDAO.buscarPorId(id).ifPresentOrElse(
                        i -> req.setAttribute("ingrediente", i),
                        () -> req.setAttribute("error", "Ingrediente no encontrado"));
                req.getRequestDispatcher("/WEB-INF/views/ingrediente-movimiento.jsp").forward(req, resp);
                return;
            }

            String q = Filtros.texto(req, "q");
            String estado = Filtros.opcion(req, "estado", "ACTIVO", "INACTIVO");
            String stock = Filtros.opcion(req, "stock", "BAJO");
            if ("stockbajo".equals(req.getParameter("filtro"))) { // atajo desde el dashboard
                estado = "ACTIVO";
                stock = "BAJO";
            }
            final String fq = q.toLowerCase();
            final String festado = estado;
            final String fstock = stock;
            var filtrados = ingredienteDAO.listarTodos().stream()
                    .filter(i -> fq.isEmpty() || i.getNombre().toLowerCase().contains(fq)
                            || (i.getUnidadMedida() != null && i.getUnidadMedida().toString().toLowerCase().contains(fq)))
                    .filter(i -> festado.isEmpty() || i.isActivo() == "ACTIVO".equals(festado))
                    .filter(i -> fstock.isEmpty() || i.isStockBajo())
                    .toList();
            Pagina<com.svir.jee.model.Ingrediente> pg = Pagina.de(filtrados, req.getParameter("pagina"));
            pg.publicar(req, req.getContextPath() + "/app/ingredientes?" + Filtros.query("q", q, "estado", estado, "stock", stock));
            req.setAttribute("fq", q);
            req.setAttribute("festado", estado);
            req.setAttribute("fstock", stock);
            req.setAttribute("ingredientes", pg.getItems());
            req.getRequestDispatcher("/WEB-INF/views/ingredientes.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error accediendo a ingredientes", e);
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
                ingredienteDAO.cambiarActivo(id, activo);
                resp.sendRedirect(req.getContextPath() + "/app/ingredientes");
                return;
            }

            if ("movimiento".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                TipoMovimiento tipo = TipoMovimiento.valueOf(req.getParameter("tipo"));
                MotivoMovimientoIngrediente motivo = MotivoMovimientoIngrediente.valueOf(req.getParameter("motivo"));
                BigDecimal cantidad = new BigDecimal(req.getParameter("cantidad"));
                Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");

                ingredienteDAO.registrarMovimiento(id, tipo, motivo, cantidad, usuario.getId());
                resp.sendRedirect(req.getContextPath() + "/app/ingredientes");
                return;
            }

            String idParam = req.getParameter("id");
            Ingrediente i = new Ingrediente();
            if (idParam != null && !idParam.isBlank()) {
                i.setId(Integer.parseInt(idParam));
            }
            i.setNombre(req.getParameter("nombre"));
            i.setUnidadMedida(req.getParameter("unidadMedida"));
            i.setStock(new BigDecimal(req.getParameter("stock")));
            i.setStockMinimo(new BigDecimal(req.getParameter("stockMinimo")));
            i.setActivo(true);

            if (i.getId() == null) {
                ingredienteDAO.crear(i);
            } else {
                ingredienteDAO.actualizar(i);
            }
            resp.sendRedirect(req.getContextPath() + "/app/ingredientes");
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Stock y stock minimo deben ser numeros validos.");
            req.getRequestDispatcher("/WEB-INF/views/ingrediente-form.jsp").forward(req, resp);
        } catch (SQLException e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("ingredientes", ingredienteDAO.listarTodos());
            } catch (SQLException ignored) {
                // si tampoco se puede listar, se muestra el error igual sin la tabla
            }
            req.getRequestDispatcher("/WEB-INF/views/ingredientes.jsp").forward(req, resp);
        }
    }
}
