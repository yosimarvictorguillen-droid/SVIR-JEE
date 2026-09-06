<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%
    request.setAttribute("tituloPagina", "Productos");
    request.setAttribute("paginaActiva", "productos");
    Producto producto = (Producto) request.getAttribute("producto");
    boolean esEdicion = producto != null;
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3"><%= esEdicion ? "Editar producto" : "Nuevo producto" %></h5>

<div class="card" style="max-width:600px;">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/productos" enctype="multipart/form-data">
            <% if (esEdicion) { %>
                <input type="hidden" name="id" value="<%= producto.getId() %>">
            <% } %>
            <div class="mb-3">
                <label class="form-label">Foto del producto</label>
                <% if (esEdicion && producto.getImagenUrl() != null) { %>
                    <div class="mb-2">
                        <img src="<%= ctx %><%= producto.getImagenUrl() %>" alt="<%= producto.getNombre() %>"
                             style="width:100px;height:100px;object-fit:cover;border-radius:.5rem;">
                    </div>
                <% } %>
                <input type="file" class="form-control" name="imagen" accept="image/*">
                <div class="form-text">JPG o PNG, maximo 5 MB. <%= esEdicion ? "Deja vacio para mantener la foto actual." : "" %></div>
            </div>
            <div class="mb-3">
                <label class="form-label">Nombre</label>
                <input type="text" class="form-control" name="nombre" required
                       value="<%= esEdicion ? producto.getNombre() : "" %>">
            </div>
            <div class="mb-3">
                <label class="form-label">Descripcion</label>
                <textarea class="form-control" name="descripcion" rows="2"><%= esEdicion && producto.getDescripcion() != null ? producto.getDescripcion() : "" %></textarea>
            </div>
            <div class="row">
                <div class="col-md-4 mb-3">
                    <label class="form-label">Precio (S/)</label>
                    <input type="number" step="0.01" min="0" class="form-control" name="precio" required
                           value="<%= esEdicion ? producto.getPrecio() : "" %>">
                </div>
                <div class="col-md-4 mb-3">
                    <label class="form-label">Stock</label>
                    <input type="number" min="0" class="form-control" name="stock" required
                           value="<%= esEdicion ? producto.getStock() : 0 %>">
                </div>
                <div class="col-md-4 mb-3">
                    <label class="form-label">Stock minimo</label>
                    <input type="number" min="0" class="form-control" name="stockMinimo" required
                           value="<%= esEdicion ? producto.getStockMinimo() : 0 %>">
                </div>
            </div>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Guardar</button>
            <a href="<%= ctx %>/app/productos" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
