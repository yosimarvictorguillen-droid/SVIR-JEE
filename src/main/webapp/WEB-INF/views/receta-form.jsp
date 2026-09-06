<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="com.svir.jee.model.Ingrediente" %>
<%@ page import="com.svir.jee.model.RecetaItem" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%
    request.setAttribute("tituloPagina", "Recetas");
    request.setAttribute("paginaActiva", "recetas");
    Producto producto = (Producto) request.getAttribute("producto");
    List<Ingrediente> ingredientes = (List<Ingrediente>) request.getAttribute("ingredientes");
    List<RecetaItem> items = (List<RecetaItem>) request.getAttribute("items");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<% if (producto == null) { %>
    <div class="alert alert-danger">Producto no encontrado.</div>
<% } else { %>

<h5 class="mb-1">Receta de: <%= producto.getNombre() %></h5>
<p class="text-muted">Indica cuanto de cada ingrediente lleva <strong>una unidad</strong> de este producto. Deja en 0 los que no se usan.</p>

<% if ("1".equals(request.getParameter("guardado"))) { %>
    <div class="alert alert-success">Receta guardada correctamente.</div>
<% } %>

<form method="post" action="<%= ctx %>/app/recetas">
    <input type="hidden" name="productoId" value="<%= producto.getId() %>">
    <table class="table bg-white" style="max-width:700px;">
        <thead><tr><th>Ingrediente</th><th>Unidad</th><th>Cantidad por unidad</th></tr></thead>
        <tbody>
        <%
            for (Ingrediente ing : ingredientes) {
                BigDecimal cantidadActual = BigDecimal.ZERO;
                for (RecetaItem item : items) {
                    if (item.getIngredienteId().equals(ing.getId())) {
                        cantidadActual = item.getCantidad();
                        break;
                    }
                }
        %>
        <tr>
            <td>
                <%= ing.getNombre() %>
                <input type="hidden" name="ingredienteId" value="<%= ing.getId() %>">
            </td>
            <td><%= ing.getUnidadMedida() %></td>
            <td>
                <input type="number" step="0.01" min="0" name="cantidad" class="form-control form-control-sm"
                       value="<%= cantidadActual %>" style="max-width:140px;">
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <button type="submit" class="btn text-white" style="background-color:#d97706;">Guardar receta</button>
    <a href="<%= ctx %>/app/recetas" class="btn btn-outline-secondary">Volver</a>
</form>

<% } %>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
