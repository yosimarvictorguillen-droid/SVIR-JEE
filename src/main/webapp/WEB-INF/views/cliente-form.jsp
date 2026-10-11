<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Cliente" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%@ page import="com.svir.jee.util.Textos" %>
<%
    request.setAttribute("tituloPagina", "Clientes");
    request.setAttribute("paginaActiva", "clientes");
    // "cliente" llega con datos al editar, o tras un error de validacion (en ese caso puede ser un alta sin id).
    Cliente cliente = (Cliente) request.getAttribute("cliente");
    boolean esEdicion = cliente != null && cliente.getId() != null;
    boolean hayDatos = cliente != null;
    String tipoDoc = hayDatos && cliente.getTipoDocumento() != null ? cliente.getTipoDocumento() : "DNI";
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
                <input type="text" class="form-control" name="nombre" required maxlength="100"
                       value="<%= hayDatos && cliente.getNombre() != null ? Textos.escapar(cliente.getNombre()) : "" %>">
            </div>
            <div class="row">
                <div class="col-md-4 mb-3">
                    <label class="form-label">Tipo de documento</label>
                    <select class="form-select" name="tipoDocumento">
                        <option value="DNI" <%= Filtros.sel(tipoDoc, "DNI") %>>DNI</option>
                        <option value="CE" <%= Filtros.sel(tipoDoc, "CE") %>>Carne de extranjeria</option>
                        <option value="PASAPORTE" <%= Filtros.sel(tipoDoc, "PASAPORTE") %>>Pasaporte</option>
                    </select>
                </div>
                <div class="col-md-4 mb-3">
                    <label class="form-label">Documento (unico)</label>
                    <input type="text" class="form-control" name="documento" maxlength="12" required
                           value="<%= hayDatos && cliente.getDni() != null ? Textos.escapar(cliente.getDni()) : "" %>">
                </div>
                <div class="col-md-4 mb-3">
                    <label class="form-label">RUC</label>
                    <input type="text" class="form-control" name="ruc" maxlength="11"
                           value="<%= hayDatos && cliente.getRuc() != null ? Textos.escapar(cliente.getRuc()) : "" %>">
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Telefono</label>
                <input type="text" class="form-control" name="telefono"
                       value="<%= hayDatos && cliente.getTelefono() != null ? Textos.escapar(cliente.getTelefono()) : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Direccion</label>
                <input type="text" class="form-control" name="direccion"
                       value="<%= hayDatos && cliente.getDireccion() != null ? Textos.escapar(cliente.getDireccion()) : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" name="email"
                       value="<%= hayDatos && cliente.getEmail() != null ? Textos.escapar(cliente.getEmail()) : "" %>">
            </div>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Guardar</button>
            <a href="<%= ctx %>/app/clientes" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
