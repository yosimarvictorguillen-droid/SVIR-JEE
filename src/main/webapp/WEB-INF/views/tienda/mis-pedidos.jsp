<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.DetallePedido" %>
<%@ page import="com.svir.jee.model.EstadoPedido" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Mis pedidos");
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    Pedido pedidoBuscado = (Pedido) request.getAttribute("pedidoBuscado");
    List<Pedido> resultados = (List<Pedido>) request.getAttribute("resultados");
    String mensajeBusqueda = (String) request.getAttribute("mensajeBusqueda");
    String textoBuscado = (String) request.getAttribute("q");
%>
<%@ include file="/WEB-INF/views/tienda/common/tienda-header.jspf" %>

<h3 class="mb-4">Mis pedidos</h3>

<div class="card mb-4">
    <div class="card-body">
        <form method="get" action="<%= ctx %>/mis-pedidos" class="row g-2">
            <div class="col-12 col-md-6">
                <input type="text" class="form-control" name="q" required maxlength="100"
                       value="<%= textoBuscado != null ? textoBuscado.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;") : "" %>"
                       placeholder="Numero de pedido, tu nombre o tu telefono">
            </div>
            <div class="col-auto"><button class="btn btn-amber">Buscar</button></div>
        </form>
        <div class="form-text mt-2">Ejemplos: 12 (numero de pedido), Juan Perez (nombre) o 987654321 (telefono del delivery).</div>
    </div>
</div>

<% if (mensajeBusqueda != null) { %>
<div class="alert alert-warning"><%= mensajeBusqueda %></div>
<% } %>

<% if (resultados != null && !resultados.isEmpty()) { %>
<h5 class="mb-3">Resultados de la busqueda</h5>
<table class="table bg-white mb-4">
    <thead><tr><th>#</th><th>Nombre</th><th>Tipo de cliente</th><th>Canal</th><th>Estado</th><th>Total</th><th>Fecha</th><th></th></tr></thead>
    <tbody>
    <% for (Pedido r : resultados) { %>
    <tr>
        <td>#<%= r.getId() %></td>
        <td><%= r.getClienteNombre() %></td>
        <td><span class="badge <%= "Cliente".equals(r.getTipoComprador()) ? "bg-info text-dark" : "bg-warning text-dark" %>"><%= r.getTipoComprador() %></span></td>
        <td><%= r.getTipoOrigen() %></td>
        <td><span class="badge bg-secondary"><%= r.getEstado() %></span></td>
        <td>S/ <%= r.getTotal() %></td>
        <td><%= r.getCreatedAt() %></td>
        <td><a class="btn btn-sm btn-outline-secondary" href="<%= ctx %>/mis-pedidos?id=<%= r.getId() %>">Ver</a></td>
    </tr>
    <% } %>
    </tbody>
</table>
<% } %>

<% if (pedidoBuscado != null) { %>
<div class="card mb-4">
    <div class="card-body">
        <h5>Pedido #<%= pedidoBuscado.getId() %></h5>
        <% if (pedidoBuscado.getClienteNombre() != null) { %>
        <p class="mb-1"><strong>A nombre de:</strong> <%= pedidoBuscado.getClienteNombre() %>
            <span class="badge <%= "Cliente".equals(pedidoBuscado.getTipoComprador()) ? "bg-info text-dark" : "bg-warning text-dark" %>"><%= pedidoBuscado.getTipoComprador() %></span></p>
        <% } %>
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
