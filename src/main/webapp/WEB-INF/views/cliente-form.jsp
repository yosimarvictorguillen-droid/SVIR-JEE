<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Cliente" %>
<%
    request.setAttribute("tituloPagina", "Clientes");
    request.setAttribute("paginaActiva", "clientes");
    Cliente cliente = (Cliente) request.getAttribute("cliente");
    boolean esEdicion = cliente != null;
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3"><%= esEdicion ? "Editar cliente" : "Nuevo cliente" %></h5>

<div class="card" style="max-width:600px;">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/clientes">
            <% if (esEdicion) { %>
                <input type="hidden" name="id" value="<%= cliente.getId() %>">
            <% } %>
            <div class="mb-3">
                <label class="form-label">Nombre</label>
                <input type="text" class="form-control" name="nombre" required
                       value="<%= esEdicion ? cliente.getNombre() : "" %>">
            </div>
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">DNI</label>
                    <input type="text" class="form-control" name="dni" maxlength="8"
                           value="<%= esEdicion && cliente.getDni() != null ? cliente.getDni() : "" %>">
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">RUC</label>
                    <input type="text" class="form-control" name="ruc" maxlength="11"
                           value="<%= esEdicion && cliente.getRuc() != null ? cliente.getRuc() : "" %>">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Telefono</label>
                <input type="text" class="form-control" name="telefono"
                       value="<%= esEdicion && cliente.getTelefono() != null ? cliente.getTelefono() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Direccion</label>
                <input type="text" class="form-control" name="direccion"
                       value="<%= esEdicion && cliente.getDireccion() != null ? cliente.getDireccion() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" name="email"
                       value="<%= esEdicion && cliente.getEmail() != null ? cliente.getEmail() : "" %>">
            </div>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Guardar</button>
            <a href="<%= ctx %>/app/clientes" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
