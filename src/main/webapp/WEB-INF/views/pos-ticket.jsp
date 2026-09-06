<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.DetallePedido" %>
<%
    request.setAttribute("tituloPagina", "Punto de Venta");
    request.setAttribute("paginaActiva", "pos");
    Pedido pedido = (Pedido) request.getAttribute("pedido");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<% if (pedido == null) { %>
    <div class="alert alert-danger">Pedido no encontrado.</div>
<% } else { %>

<div class="d-flex justify-content-between align-items-center mb-3 no-print">
    <h5 class="mb-0">Venta registrada</h5>
    <div>
        <button onclick="window.print()" class="btn btn-sm text-white" style="background-color:#d97706;">
            <i class="bi bi-printer"></i> Imprimir
        </button>
        <a href="<%= ctx %>/app/pos" class="btn btn-sm btn-outline-secondary">Nueva venta</a>
    </div>
</div>

<div class="card" style="max-width:420px;">
    <div class="card-body" style="font-family: 'Courier New', monospace;">
        <div class="text-center mb-2">
            <strong>DULCE MOMENTO</strong><br>
            <span class="small"><%= (pedido.getObservacion() != null && pedido.getObservacion().startsWith("Comprobante: FACTURA")) ? "FACTURA" : "BOLETA DE VENTA" %></span><br>
            <span class="small">Pedido #<%= pedido.getId() %> - <%= pedido.getCreatedAt() %></span>
        </div>
        <hr>
        <div class="small mb-2"><%= pedido.getObservacion() %></div>
        <hr>
        <table class="table table-sm mb-2">
            <thead><tr><th>Cant.</th><th>Producto</th><th class="text-end">Subtotal</th></tr></thead>
            <tbody>
            <%
                for (DetallePedido d : pedido.getDetalles()) {
            %>
            <tr>
                <td><%= d.getCantidad() %></td>
                <td><%= d.getProductoNombre() %></td>
                <td class="text-end">S/ <%= d.getSubtotal() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <hr>
        <div class="text-end"><strong>TOTAL: S/ <%= pedido.getTotal() %></strong></div>
        <p class="text-center small mt-3">Gracias por su compra</p>
    </div>
</div>

<style>
    @media print {
        .svir-sidebar, .svir-topbar, .no-print { display: none !important; }
    }
</style>

<% } %>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
