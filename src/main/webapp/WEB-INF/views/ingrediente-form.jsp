<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Ingrediente" %>
<%
    request.setAttribute("tituloPagina", "Ingredientes");
    request.setAttribute("paginaActiva", "ingredientes");
    Ingrediente ingrediente = (Ingrediente) request.getAttribute("ingrediente");
    boolean esEdicion = ingrediente != null;
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3"><%= esEdicion ? "Editar ingrediente" : "Nuevo ingrediente" %></h5>

<div class="card" style="max-width:600px;">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/ingredientes">
            <% if (esEdicion) { %>
                <input type="hidden" name="id" value="<%= ingrediente.getId() %>">
            <% } %>
            <div class="mb-3">
                <label class="form-label">Nombre</label>
                <input type="text" class="form-control" name="nombre" required
                       value="<%= esEdicion ? ingrediente.getNombre() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Unidad de medida</label>
                <input type="text" class="form-control" name="unidadMedida" placeholder="kg, litro, unidad..." required
                       value="<%= esEdicion ? ingrediente.getUnidadMedida() : "" %>">
            </div>
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">Stock</label>
                    <input type="number" step="0.01" min="0" class="form-control" name="stock" required
                           value="<%= esEdicion ? ingrediente.getStock() : "0" %>">
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label">Stock minimo</label>
                    <input type="number" step="0.01" min="0" class="form-control" name="stockMinimo" required
                           value="<%= esEdicion ? ingrediente.getStockMinimo() : "0" %>">
                </div>
            </div>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Guardar</button>
            <a href="<%= ctx %>/app/ingredientes" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
