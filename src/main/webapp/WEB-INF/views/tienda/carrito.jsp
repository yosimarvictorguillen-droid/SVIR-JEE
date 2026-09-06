<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.math.BigDecimal" %>
<%
    request.setAttribute("tituloPagina", "Carrito");
    Map<Producto, Integer> lineas = (Map<Producto, Integer>) request.getAttribute("lineas");
    BigDecimal total = (BigDecimal) request.getAttribute("total");
%>
<%@ include file="/WEB-INF/views/tienda/common/tienda-header.jspf" %>

<h3 class="mb-4">Tu carrito</h3>

<% if (lineas == null || lineas.isEmpty()) { %>
    <p class="text-muted">Tu carrito esta vacio. <a href="<%= ctx %>/catalogo">Ver catalogo</a></p>
<% } else { %>

<table class="table bg-white">
    <thead><tr><th>Producto</th><th>Precio</th><th>Cantidad</th><th>Subtotal</th><th></th></tr></thead>
    <tbody>
    <%
        for (Map.Entry<Producto, Integer> e : lineas.entrySet()) {
            Producto p = e.getKey();
            int cantidad = e.getValue();
    %>
    <tr>
        <td><%= p.getNombre() %></td>
        <td>S/ <%= p.getPrecio() %></td>
        <td>
            <form method="post" action="<%= ctx %>/carrito" class="d-flex align-items-center gap-1">
                <input type="hidden" name="action" value="actualizar">
                <input type="hidden" name="productoId" value="<%= p.getId() %>">
                <input type="number" name="cantidad" value="<%= cantidad %>" min="0" max="<%= p.getStock() %>"
                       class="form-control form-control-sm" style="width:80px;">
                <button type="submit" class="btn btn-sm btn-outline-secondary">Actualizar</button>
            </form>
        </td>
        <td>S/ <%= p.getPrecio().multiply(BigDecimal.valueOf(cantidad)) %></td>
        <td>
            <form method="post" action="<%= ctx %>/carrito">
                <input type="hidden" name="action" value="eliminar">
                <input type="hidden" name="productoId" value="<%= p.getId() %>">
                <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button>
            </form>
        </td>
    </tr>
    <% } %>
    </tbody>
    <tfoot>
    <tr><td colspan="3" class="text-end"><strong>Total</strong></td><td colspan="2"><strong>S/ <%= total %></strong></td></tr>
    </tfoot>
</table>

<div class="d-flex justify-content-between">
    <a href="<%= ctx %>/catalogo" class="btn btn-outline-secondary">Seguir comprando</a>
    <a href="<%= ctx %>/checkout" class="btn btn-amber">Ir a pagar</a>
</div>

<% } %>

<%@ include file="/WEB-INF/views/tienda/common/tienda-footer.jspf" %>
