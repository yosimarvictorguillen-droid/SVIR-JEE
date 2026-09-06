<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Produccion" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Produccion");
    request.setAttribute("paginaActiva", "producciones");
    List<Produccion> producciones = (List<Produccion>) request.getAttribute("producciones");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Ordenes de produccion</h5>
    <div>
        <a href="<%= ctx %>/app/producciones?action=porPedido" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-receipt"></i> Por pedido
        </a>
        <a href="<%= ctx %>/app/producciones?action=nueva" class="btn btn-sm text-white" style="background-color:#d97706;">
            <i class="bi bi-plus-lg"></i> Para stock
        </a>
    </div>
</div>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead><tr><th>#</th><th>Tipo</th><th>Pedido</th><th>Responsable</th><th>Estado</th><th>Fecha</th><th></th></tr></thead>
        <tbody>
        <%
            if (producciones != null) {
                for (Produccion p : producciones) {
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= p.getTipo() %></td>
            <td><%= p.getPedidoId() != null ? "#" + p.getPedidoId() : "-" %></td>
            <td><%= p.getUsuarioNombre() %></td>
            <td><span class="badge bg-secondary"><%= p.getEstado() %></span></td>
            <td><%= p.getCreatedAt() %></td>
            <td>
                <a href="<%= ctx %>/app/producciones?action=ver&id=<%= p.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-eye"></i>
                </a>
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
