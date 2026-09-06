<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Usuario" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Usuarios");
    request.setAttribute("paginaActiva", "usuarios");
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Personal interno</h5>
    <a href="<%= ctx %>/app/usuarios?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo usuario
    </a>
</div>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead><tr><th>Nombre</th><th>Email</th><th>Rol</th><th>Telefono</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
        <%
            for (Usuario u : usuarios) {
        %>
        <tr>
            <td><%= u.getNombre() %></td>
            <td><%= u.getEmail() %></td>
            <td><span class="badge bg-info text-dark"><%= u.getRol() %></span></td>
            <td><%= u.getTelefono() != null ? u.getTelefono() : "-" %></td>
            <td>
                <% if (u.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/usuarios?action=editar&id=<%= u.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/usuarios" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= u.getId() %>">
                    <input type="hidden" name="activo" value="<%= !u.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= u.isActivo() ? "danger" : "success" %>">
                        <%= u.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
