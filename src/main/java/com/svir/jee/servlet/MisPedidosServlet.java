package com.svir.jee.servlet;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.Pedido;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Seguimiento de pedidos en la tienda. Si hay sesion de cliente muestra su historial.
 * Para todos (clientes e invitados) hay una busqueda por numero de pedido, por nombre
 * o, si son 7 o mas digitos, por telefono. El detalle de un pedido no muestra direccion
 * ni telefono.
 */
@WebServlet("/mis-pedidos")
public class MisPedidosServlet extends HttpServlet {

    private static final int MIN_CARACTERES_NOMBRE = 3;

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Cliente cliente = (Cliente) req.getSession(true).getAttribute("cliente");
            if (cliente != null) {
                req.setAttribute("pedidos", pedidoDAO.listarPorCliente(cliente.getId()));
            }

            // "id" llega desde el checkout y desde los enlaces "Ver" de los resultados; "q" es el cuadro de busqueda.
            String idParam = req.getParameter("id");
            String q = req.getParameter("q");

            if (idParam != null && !idParam.isBlank()) {
                buscarPorNumero(req, idParam.trim());
            } else if (q != null && !q.isBlank()) {
                req.setAttribute("q", q.trim());
                buscarLibre(req, q.trim());
            }

            req.setAttribute("rutaActual", "/mis-pedidos");
            req.getRequestDispatcher("/WEB-INF/views/tienda/mis-pedidos.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error consultando tus pedidos", e);
        }
    }

    private void buscarLibre(HttpServletRequest req, String q) throws SQLException {
        String texto = q.startsWith("#") ? q.substring(1).trim() : q;

        if (texto.matches("\\d{1,6}")) {
            buscarPorNumero(req, texto);
            return;
        }
        if (texto.length() < MIN_CARACTERES_NOMBRE) {
            req.setAttribute("mensajeBusqueda", "Escribe al menos " + MIN_CARACTERES_NOMBRE
                    + " letras de tu nombre, tu telefono o el numero de pedido.");
            return;
        }
        List<Pedido> resultados = pedidoDAO.buscarPorTexto(texto);
        req.setAttribute("resultados", resultados);
        if (resultados.isEmpty()) {
            req.setAttribute("mensajeBusqueda", "No encontramos pedidos con ese dato.");
        }
    }

    private void buscarPorNumero(HttpServletRequest req, String numero) throws SQLException {
        if (!numero.matches("\\d{1,9}")) {
            req.setAttribute("mensajeBusqueda", "El numero de pedido no es valido.");
            return;
        }
        Optional<Pedido> encontrado = pedidoDAO.buscarPorId(Integer.parseInt(numero));
        if (encontrado.isPresent()) {
            req.setAttribute("pedidoBuscado", encontrado.get());
        } else {
            req.setAttribute("mensajeBusqueda", "No existe el pedido #" + numero + ".");
        }
    }
}
