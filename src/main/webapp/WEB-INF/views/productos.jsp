<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Productos");
    request.setAttribute("paginaActiva", "productos");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Catalogo de productos</h5>
    <a href="<%= ctx %>/app/productos?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo producto
    </a>
</div>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Foto</th>
            <th>Nombre</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Stock minimo</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            for (Producto p : productos) {
        %>
        <tr>
            <td>
                <% if (p.getImagenUrl() != null) { %>
                    <img src="<%= ctx %><%= p.getImagenUrl() %>" alt="<%= p.getNombre() %>"
                         style="width:48px;height:48px;object-fit:cover;border-radius:.4rem;">
                <% } else { %>
                    <span class="text-muted small">Sin foto</span>
                <% } %>
            </td>
            <td>
                <%= p.getNombre() %>
                <% if (p.isStockBajo()) { %>
                    <span class="badge bg-danger ms-1">Stock bajo</span>
                <% } %>
            </td>
            <td>S/ <%= p.getPrecio() %></td>
            <td><%= p.getStock() %></td>
            <td><%= p.getStockMinimo() %></td>
            <td>
                <% if (p.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/productos?action=editar&id=<%= p.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/productos" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="activo" value="<%= !p.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= p.isActivo() ? "danger" : "success" %>">
                        <%= p.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
            </td>
        </tr>
        <% } %>
        <% if (productos.isEmpty()) { %>
        <tr><td colspan="7" class="text-center text-muted">No hay productos registrados.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
