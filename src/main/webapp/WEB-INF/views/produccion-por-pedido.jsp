<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Produccion");
    request.setAttribute("paginaActiva", "producciones");
    List<Pedido> pedidosConFaltante = (List<Pedido>) request.getAttribute("pedidosConFaltante");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Crear orden a partir de un pedido con faltante</h5>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead><tr><th>#</th><th>Cliente</th><th>Total</th><th>Estado</th><th></th></tr></thead>
        <tbody>
        <%
            if (pedidosConFaltante != null) {
                for (Pedido p : pedidosConFaltante) {
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= p.getClienteNombre() != null ? p.getClienteNombre() : "Cliente general" %></td>
            <td>S/ <%= p.getTotal() %></td>
            <td><span class="badge bg-secondary"><%= p.getEstado() %></span></td>
            <td>
                <form method="post" action="<%= ctx %>/app/producciones" class="d-inline">
                    <input type="hidden" name="action" value="crearPorPedido">
                    <input type="hidden" name="pedidoId" value="<%= p.getId() %>">
                    <button type="submit" class="btn btn-sm text-white" style="background-color:#d97706;">
                        Crear orden
                    </button>
                </form>
            </td>
        </tr>
        <%
                }
                if (pedidosConFaltante.isEmpty()) {
        %>
        <tr><td colspan="5" class="text-center text-muted">No hay pedidos con productos pendientes de atender.</td></tr>
        <% }
            }
        %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
