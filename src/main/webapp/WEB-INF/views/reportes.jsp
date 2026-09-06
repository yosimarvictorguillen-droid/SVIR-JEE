<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="com.svir.jee.model.Ingrediente" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%
    request.setAttribute("tituloPagina", "Reportes");
    request.setAttribute("paginaActiva", "reportes");
    LocalDate desde = (LocalDate) request.getAttribute("desde");
    LocalDate hasta = (LocalDate) request.getAttribute("hasta");
    List<Pedido> pedidosRango = (List<Pedido>) request.getAttribute("pedidosRango");
    List<Producto> productosStockBajo = (List<Producto>) request.getAttribute("productosStockBajo");
    List<Ingrediente> ingredientesStockBajo = (List<Ingrediente>) request.getAttribute("ingredientesStockBajo");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Reporte de ventas por rango de fechas</h5>

<form method="get" action="<%= ctx %>/app/reportes" class="row g-2 align-items-end mb-4">
    <div class="col-auto">
        <label class="form-label">Desde</label>
        <input type="date" class="form-control" name="desde" value="<%= desde %>">
    </div>
    <div class="col-auto">
        <label class="form-label">Hasta</label>
        <input type="date" class="form-control" name="hasta" value="<%= hasta %>">
    </div>
    <div class="col-auto">
        <button type="submit" class="btn text-white" style="background-color:#d97706;">Filtrar</button>
    </div>
</form>

<p><strong>Total vendido en el rango:</strong> S/ <%= request.getAttribute("totalVentasRango") %>
   &nbsp;(<%= pedidosRango != null ? pedidosRango.size() : 0 %> pedidos)</p>

<div class="table-responsive mb-5">
    <table class="table table-hover bg-white">
        <thead>
        <tr><th>#</th><th>Cliente</th><th>Canal</th><th>Total</th><th>Estado</th><th>Fecha</th></tr>
        </thead>
        <tbody>
        <%
            if (pedidosRango != null) {
                for (Pedido p : pedidosRango) {
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= p.getClienteNombre() != null ? p.getClienteNombre() : "Cliente general" %></td>
            <td><%= p.getTipoOrigen() %></td>
            <td>S/ <%= p.getTotal() %></td>
            <td><span class="badge bg-secondary"><%= p.getEstado() %></span></td>
            <td><%= p.getCreatedAt() %></td>
        </tr>
        <%
                }
                if (pedidosRango.isEmpty()) {
        %>
        <tr><td colspan="6" class="text-center text-muted">No hay ventas en ese rango.</td></tr>
        <% }
            }
        %>
        </tbody>
    </table>
</div>

<div class="row">
    <div class="col-md-6">
        <h5 class="mb-3">Productos con stock bajo</h5>
        <table class="table table-sm bg-white">
            <thead><tr><th>Producto</th><th>Stock</th><th>Minimo</th></tr></thead>
            <tbody>
            <%
                if (productosStockBajo != null) {
                    for (Producto p : productosStockBajo) {
            %>
            <tr><td><%= p.getNombre() %></td><td><%= p.getStock() %></td><td><%= p.getStockMinimo() %></td></tr>
            <%
                    }
                    if (productosStockBajo.isEmpty()) {
            %>
            <tr><td colspan="3" class="text-center text-muted">Sin alertas.</td></tr>
            <% }
                }
            %>
            </tbody>
        </table>
    </div>
    <div class="col-md-6">
        <h5 class="mb-3">Ingredientes con stock bajo</h5>
        <table class="table table-sm bg-white">
            <thead><tr><th>Ingrediente</th><th>Stock</th><th>Minimo</th></tr></thead>
            <tbody>
            <%
                if (ingredientesStockBajo != null) {
                    for (Ingrediente i : ingredientesStockBajo) {
            %>
            <tr><td><%= i.getNombre() %></td><td><%= i.getStock() %> <%= i.getUnidadMedida() %></td><td><%= i.getStockMinimo() %></td></tr>
            <%
                    }
                    if (ingredientesStockBajo.isEmpty()) {
            %>
            <tr><td colspan="3" class="text-center text-muted">Sin alertas.</td></tr>
            <% }
                }
            %>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
