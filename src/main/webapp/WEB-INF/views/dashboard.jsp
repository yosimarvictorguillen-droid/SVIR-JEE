<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.DashboardResumen" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%
    request.setAttribute("tituloPagina", "Dashboard");
    request.setAttribute("paginaActiva", "dashboard");
    DashboardResumen resumen = (DashboardResumen) request.getAttribute("resumen");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="stat-card">
            <div class="text-muted small">Ventas de hoy</div>
            <div class="stat-value">S/ <%= resumen.getVentasHoy() %></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card">
            <div class="text-muted small">Ventas del mes</div>
            <div class="stat-value">S/ <%= resumen.getVentasMes() %></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card">
            <div class="text-muted small">Pedidos de hoy</div>
            <div class="stat-value"><%= resumen.getPedidosHoy() %></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card">
            <div class="text-muted small">Pedidos pendientes / en preparacion</div>
            <div class="stat-value"><%= resumen.getPedidosPendientes() + resumen.getPedidosPreparacion() %></div>
        </div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-md-3">
        <div class="stat-card <%= resumen.getProductosStockBajo() > 0 ? "stock-bajo" : "" %>">
            <div class="text-muted small">Productos con stock bajo</div>
            <div class="stat-value"><%= resumen.getProductosStockBajo() %> / <%= resumen.getTotalProductos() %></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card <%= resumen.getIngredientesStockBajo() > 0 ? "stock-bajo" : "" %>">
            <div class="text-muted small">Ingredientes con stock bajo</div>
            <div class="stat-value"><%= resumen.getIngredientesStockBajo() %> / <%= resumen.getTotalIngredientes() %></div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card">
            <div class="text-muted small">Pedidos listos</div>
            <div class="stat-value"><%= resumen.getPedidosListo() %></div>
        </div>
    </div>
</div>

<h5 class="mb-3">Pedidos recientes</h5>
<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>#</th>
            <th>Cliente</th>
            <th>Canal</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Fecha</th>
        </tr>
        </thead>
        <tbody>
        <%
            for (Pedido p : resumen.getPedidosRecientes()) {
                String cliente = p.getClienteNombre() != null ? p.getClienteNombre() : "Cliente general";
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= cliente %></td>
            <td><%= p.getTipoOrigen() %></td>
            <td>S/ <%= p.getTotal() %></td>
            <td><span class="badge bg-secondary badge-estado"><%= p.getEstado() %></span></td>
            <td><%= p.getCreatedAt() %></td>
        </tr>
        <% } %>
        <% if (resumen.getPedidosRecientes().isEmpty()) { %>
        <tr><td colspan="6" class="text-center text-muted">Aun no hay pedidos registrados.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
