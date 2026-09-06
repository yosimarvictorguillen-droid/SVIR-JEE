<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Catalogo");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<%@ include file="/WEB-INF/views/tienda/common/tienda-header.jspf" %>

<h3 class="mb-4">Nuestro catalogo</h3>

<div class="row g-4">
    <%
        for (Producto p : productos) {
    %>
    <div class="col-6 col-md-4 col-lg-3">
        <div class="producto-card">
            <% if (p.getImagenUrl() != null) { %>
                <div class="img-wrap" style="background:none;">
                    <img src="<%= ctx %><%= p.getImagenUrl() %>" alt="<%= p.getNombre() %>"
                         style="width:100%;height:100%;object-fit:cover;">
                </div>
            <% } else { %>
                <div class="img-wrap"><%= p.getNombre().substring(0, 1) %></div>
            <% } %>
            <div class="body">
                <h6 class="mb-1"><%= p.getNombre() %></h6>
                <p class="text-muted small mb-2"><%= p.getDescripcion() != null ? p.getDescripcion() : "" %></p>
                <p class="fw-bold mb-2">S/ <%= p.getPrecio() %></p>
                <% if (p.getStock() > 0) { %>
                <form method="post" action="<%= ctx %>/carrito">
                    <input type="hidden" name="productoId" value="<%= p.getId() %>">
                    <input type="hidden" name="cantidad" value="1">
                    <button type="submit" class="btn btn-amber btn-sm w-100">
                        <i class="bi bi-cart-plus"></i> Agregar
                    </button>
                </form>
                <% } else { %>
                    <span class="badge bg-secondary">Agotado</span>
                <% } %>
            </div>
        </div>
    </div>
    <% } %>
</div>

<%@ include file="/WEB-INF/views/tienda/common/tienda-footer.jspf" %>
