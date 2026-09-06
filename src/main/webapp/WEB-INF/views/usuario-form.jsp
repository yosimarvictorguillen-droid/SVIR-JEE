<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Usuario" %>
<%@ page import="com.svir.jee.model.RolUsuario" %>
<%
    request.setAttribute("tituloPagina", "Usuarios");
    request.setAttribute("paginaActiva", "usuarios");
    Usuario usuarioEditar = (Usuario) request.getAttribute("usuarioEditar");
    boolean esEdicion = usuarioEditar != null;
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3"><%= esEdicion ? "Editar usuario" : "Nuevo usuario" %></h5>

<div class="card" style="max-width:500px;">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/usuarios">
            <% if (esEdicion) { %>
                <input type="hidden" name="id" value="<%= usuarioEditar.getId() %>">
            <% } %>
            <div class="mb-3">
                <label class="form-label">Nombre</label>
                <input type="text" class="form-control" name="nombre" required
                       value="<%= esEdicion ? usuarioEditar.getNombre() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" name="email" required
                       value="<%= esEdicion ? usuarioEditar.getEmail() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Rol</label>
                <select class="form-select" name="rol" required>
                    <%
                        for (RolUsuario r : RolUsuario.values()) {
                            boolean selected = esEdicion && usuarioEditar.getRol() == r;
                    %>
                    <option value="<%= r %>" <%= selected ? "selected" : "" %>><%= r %></option>
                    <% } %>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">Telefono</label>
                <input type="text" class="form-control" name="telefono"
                       value="<%= esEdicion && usuarioEditar.getTelefono() != null ? usuarioEditar.getTelefono() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label"><%= esEdicion ? "Nueva contrasena (opcional)" : "Contrasena" %></label>
                <input type="password" class="form-control" name="password" <%= esEdicion ? "" : "required" %>>
            </div>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Guardar</button>
            <a href="<%= ctx %>/app/usuarios" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
