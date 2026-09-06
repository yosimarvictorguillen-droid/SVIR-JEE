<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Recetas");
    request.setAttribute("paginaActiva", "recetas");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Recetas por producto</h5>
<p class="text-muted">Selecciona un producto para ver o editar cuanto de cada ingrediente lleva.</p>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead><tr><th>Producto</th><th>Precio</th><th>Estado</th><th></th></tr></thead>
        <tbody>
        <%
            for (Producto p : productos) {
        %>
        <tr>
            <td><%= p.getNombre() %></td>
            <td>S/ <%= p.getPrecio() %></td>
            <td>
                <% if (p.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td>
                <a href="<%= ctx %>/app/recetas?productoId=<%= p.getId() %>" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-journal-text"></i> Ver / editar receta
                </a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
