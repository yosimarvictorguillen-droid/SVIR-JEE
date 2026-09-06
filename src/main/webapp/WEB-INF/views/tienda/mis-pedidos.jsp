<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.DetallePedido" %>
<%@ page import="com.svir.jee.model.EstadoPedido" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Mis pedidos");
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    Pedido pedidoBuscado = (Pedido) request.getAttribute("pedidoBuscado");
%>
<%@ include file="/WEB-INF/views/tienda/common/tienda-header.jspf" %>

<h3 class="mb-4">Mis pedidos</h3>

<div class="card mb-4">
    <div class="card-body">
        <form method="get" action="<%= ctx %>/mis-pedidos" class="row g-2">
            <div class="col-auto">
                <input type="number" class="form-control" name="id" placeholder="Numero de pedido" required>
            </div>
            <div class="col-auto"><button class="btn btn-amber">Buscar</button></div>
        </form>
    </div>
</div>

<% if (pedidoBuscado != null) { %>
<div class="card mb-4">
    <div class="card-body">
        <h5>Pedido #<%= pedidoBuscado.getId() %></h5>
        <p class="mb-1"><strong>Estado:</strong> <span class="badge bg-secondary"><%= pedidoBuscado.getEstado() %></span></p>
        <p class="mb-1"><strong>Total:</strong> S/ <%= pedidoBuscado.getTotal() %></p>
        <p class="mb-3"><strong>Fecha:</strong> <%= pedidoBuscado.getCreatedAt() %></p>

        <% if (pedidoBuscado.getEstado() == EstadoPedido.CANCELADO) { %>
            <span class="badge bg-danger">Pedido cancelado</span>
        <% } else { %>
            <%
                EstadoPedido[] pasos = pedidoBuscado.getTipoOrigen().name().equals("DELIVERY")
                        ? new EstadoPedido[]{EstadoPedido.PENDIENTE, EstadoPedido.PREPARACION, EstadoPedido.LISTO, EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO}
                        : new EstadoPedido[]{EstadoPedido.PENDIENTE, EstadoPedido.PREPARACION, EstadoPedido.LISTO, EstadoPedido.ENTREGADO};
                boolean pasoActivo = true;
                for (EstadoPedido paso : pasos) {
                    boolean hecho = pasoActivo;
                    if (paso == pedidoBuscado.getEstado()) pasoActivo = false;
            %>
            <div class="timeline-step <%= hecho ? "done" : "" %>">
                <span class="dot"></span> <%= paso %>
            </div>
            <% } %>
        <% } %>

        <table class="table table-sm mt-3">
            <thead><tr><th>Producto</th><th>Cantidad</th><th>Subtotal</th></tr></thead>
            <tbody>
            <% for (DetallePedido d : pedidoBuscado.getDetalles()) { %>
                <tr><td><%= d.getProductoNombre() %></td><td><%= d.getCantidad() %></td><td>S/ <%= d.getSubtotal() %></td></tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
<% } %>

<% if (pedidos != null) { %>
<h5 class="mb-3">Historial</h5>
<table class="table bg-white">
    <thead><tr><th>#</th><th>Total</th><th>Estado</th><th>Fecha</th></tr></thead>
    <tbody>
    <%
        for (Pedido p : pedidos) {
    %>
    <tr>
        <td>#<%= p.getId() %></td>
        <td>S/ <%= p.getTotal() %></td>
        <td><span class="badge bg-secondary"><%= p.getEstado() %></span></td>
        <td><%= p.getCreatedAt() %></td>
    </tr>
    <% } %>
    <% if (pedidos.isEmpty()) { %>
    <tr><td colspan="4" class="text-center text-muted">Aun no tienes pedidos.</td></tr>
    <% } %>
    </tbody>
</table>
<% } %>

<%@ include file="/WEB-INF/views/tienda/common/tienda-footer.jspf" %>
