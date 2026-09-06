<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Cliente" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Clientes");
    request.setAttribute("paginaActiva", "clientes");
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Clientes</h5>
    <a href="<%= ctx %>/app/clientes?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo cliente
    </a>
</div>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Nombre</th>
            <th>DNI</th>
            <th>Telefono</th>
            <th>Direccion</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            for (Cliente c : clientes) {
        %>
        <tr>
            <td><%= c.getNombre() %></td>
            <td><%= c.getDni() != null ? c.getDni() : "-" %></td>
            <td><%= c.getTelefono() != null ? c.getTelefono() : "-" %></td>
            <td><%= c.getDireccion() != null ? c.getDireccion() : "-" %></td>
            <td>
                <% if (c.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/clientes?action=editar&id=<%= c.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/clientes" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= c.getId() %>">
                    <input type="hidden" name="activo" value="<%= !c.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= c.isActivo() ? "danger" : "success" %>">
                        <%= c.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
            </td>
        </tr>
        <% } %>
        <% if (clientes.isEmpty()) { %>
        <tr><td colspan="6" class="text-center text-muted">No hay clientes registrados.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
