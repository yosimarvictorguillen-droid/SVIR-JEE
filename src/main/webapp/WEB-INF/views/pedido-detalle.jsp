<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.DetallePedido" %>
<%
    request.setAttribute("tituloPagina", "Pedidos");
    request.setAttribute("paginaActiva", "pedidos");
    Pedido pedido = (Pedido) request.getAttribute("pedido");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<% if (pedido == null) { %>
    <div class="alert alert-danger">Pedido no encontrado.</div>
<% } else { %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Pedido #<%= pedido.getId() %></h5>
    <a href="<%= ctx %>/app/pedidos" class="btn btn-sm btn-outline-secondary">Volver</a>
</div>

<div class="card mb-3">
    <div class="card-body">
        <p class="mb-1"><strong>Cliente:</strong> <%= pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "Cliente general" %></p>
        <p class="mb-1"><strong>Canal:</strong> <%= pedido.getTipoOrigen() %></p>
        <p class="mb-1"><strong>Estado:</strong> <span class="badge bg-secondary"><%= pedido.getEstado() %></span></p>
        <p class="mb-1"><strong>Atendido por:</strong> <%= pedido.getUsuarioNombre() != null ? pedido.getUsuarioNombre() : "-" %></p>
        <p class="mb-1"><strong>Fecha:</strong> <%= pedido.getCreatedAt() %></p>
        <% if (pedido.getObservacion() != null && !pedido.getObservacion().isBlank()) { %>
        <p class="mb-0"><strong>Observacion:</strong> <%= pedido.getObservacion() %></p>
        <% } %>
    </div>
</div>

<table class="table bg-white">
    <thead>
    <tr>
        <th>Producto</th>
        <th>Cantidad</th>
        <th>Atendido</th>
        <th>Precio unit.</th>
        <th>Subtotal</th>
        <th>Estado</th>
    </tr>
    </thead>
    <tbody>
    <%
        for (DetallePedido d : pedido.getDetalles()) {
    %>
    <tr>
        <td><%= d.getProductoNombre() %></td>
        <td><%= d.getCantidad() %></td>
        <td><%= d.getCantidadAtendida() %></td>
        <td>S/ <%= d.getPrecioUnitario() %></td>
        <td>S/ <%= d.getSubtotal() %></td>
        <td><span class="badge bg-light text-dark"><%= d.getEstado() %></span></td>
    </tr>
    <% } %>
    </tbody>
    <tfoot>
    <tr>
        <td colspan="4" class="text-end"><strong>Total</strong></td>
        <td colspan="2"><strong>S/ <%= pedido.getTotal() %></strong></td>
    </tr>
    </tfoot>
</table>

<% } %>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
