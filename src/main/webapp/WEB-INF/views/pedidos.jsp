<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.EstadoPedido" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Pedidos");
    request.setAttribute("paginaActiva", "pedidos");
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Pedidos</h5>
    <a href="<%= ctx %>/app/pedidos?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo pedido
    </a>
</div>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>#</th>
            <th>Cliente</th>
            <th>Canal</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Fecha</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            if (pedidos != null) {
                for (Pedido p : pedidos) {
                    String cliente = p.getClienteNombre() != null ? p.getClienteNombre() : "Cliente general";
                    boolean esTerminal = p.getEstado() == EstadoPedido.ENTREGADO || p.getEstado() == EstadoPedido.CANCELADO;
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= cliente %></td>
            <td><%= p.getTipoOrigen() %></td>
            <td>S/ <%= p.getTotal() %></td>
            <td><span class="badge bg-secondary badge-estado"><%= p.getEstado() %></span></td>
            <td><%= p.getCreatedAt() %></td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/pedidos?action=ver&id=<%= p.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-eye"></i>
                </a>
                <% if (!esTerminal) { %>
                <% if (p.getTipoOrigen().name().equals("DELIVERY") && p.getEstado() == EstadoPedido.LISTO) { %>
                <form method="post" action="<%= ctx %>/app/pedidos" class="d-inline">
                    <input type="hidden" name="action" value="cambiarEstado">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="estado" value="EN_CAMINO">
                    <button type="submit" class="btn btn-sm btn-outline-primary">En camino</button>
                </form>
                <% } %>
                <form method="post" action="<%= ctx %>/app/pedidos" class="d-inline">
                    <input type="hidden" name="action" value="cambiarEstado">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="estado" value="ENTREGADO">
                    <button type="submit" class="btn btn-sm btn-outline-success">Entregar</button>
                </form>
                <form method="post" action="<%= ctx %>/app/pedidos" class="d-inline"
                      onsubmit="return confirm('Se devolvera el stock atendido. Cancelar este pedido?');">
                    <input type="hidden" name="action" value="cancelar">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <button type="submit" class="btn btn-sm btn-outline-danger">Cancelar</button>
                </form>
                <% } %>
            </td>
        </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
