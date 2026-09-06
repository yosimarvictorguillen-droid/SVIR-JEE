<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Ingrediente" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Ingredientes");
    request.setAttribute("paginaActiva", "ingredientes");
    List<Ingrediente> ingredientes = (List<Ingrediente>) request.getAttribute("ingredientes");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Ingredientes (materia prima)</h5>
    <a href="<%= ctx %>/app/ingredientes?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo ingrediente
    </a>
</div>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Nombre</th>
            <th>Unidad</th>
            <th>Stock</th>
            <th>Stock minimo</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            if (ingredientes != null) {
                for (Ingrediente i : ingredientes) {
        %>
        <tr>
            <td>
                <%= i.getNombre() %>
                <% if (i.isStockBajo()) { %>
                    <span class="badge bg-danger ms-1">Stock bajo</span>
                <% } %>
            </td>
            <td><%= i.getUnidadMedida() %></td>
            <td><%= i.getStock() %></td>
            <td><%= i.getStockMinimo() %></td>
            <td>
                <% if (i.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/ingredientes?action=movimiento&id=<%= i.getId() %>" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-arrow-left-right"></i> Movimiento
                </a>
                <a href="<%= ctx %>/app/ingredientes?action=editar&id=<%= i.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/ingredientes" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= i.getId() %>">
                    <input type="hidden" name="activo" value="<%= !i.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= i.isActivo() ? "danger" : "success" %>">
                        <%= i.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
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
