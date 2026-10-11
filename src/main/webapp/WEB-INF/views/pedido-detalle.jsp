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
        <p class="mb-1"><strong>Cliente:</strong> <%= pedido.getClienteNombre() != null ? pedido.getClienteNombre() : "Sin nombre" %>
            <span class="badge <%= "Cliente".equals(pedido.getTipoComprador()) ? "bg-info text-dark" : "bg-warning text-dark" %>"><%= pedido.getTipoComprador() %></span></p>
        <% if (pedido.getDocumentoComprador() != null) { %>
        <p class="mb-1"><strong>Documento:</strong> <%= pedido.getDocumentoComprador() %>
            <% if (pedido.isAntesInvitado()) { %><span class="badge bg-secondary">Compro como invitado, hoy es cliente</span><% } %></p>
        <% } %>
        <% if (pedido.getTelefonoComprador() != null) { %>
        <p class="mb-1"><strong>Telefono:</strong> <%= pedido.getTelefonoComprador() %></p>
        <% } %>
        <p class="mb-1"><strong>Canal:</strong> <%= pedido.getTipoOrigen() %></p>
        <p class="mb-1"><strong>Estado:</strong> <span class="badge bg-secondary"><%= pedido.getEstado() %></span></p>
        <p class="mb-1"><strong>Atendido por:</strong> <%= pedido.getUsuarioNombre() != null ? pedido.getUsuarioNombre() : "-" %></p>
        <p class="mb-1"><strong>Fecha:</strong> <%= pedido.getCreatedAt() %></p>
        <% if (pedido.getObservacion() != null && !pedido.getObservacion().isBlank()) { %>
        <p class="mb-0"><strong>Observacion:</strong> <%= pedido.getObservacion() %></p>
        <% } %>
    </div>
</div>

<div class="card mb-3">
    <div class="card-body">
        <h6 class="mb-2">Comprobante</h6>
        <% if (pedido.isTieneComprobante()) { %>
        <p class="mb-1"><strong><%= pedido.getComprobanteNombre() %> <%= pedido.getComprobanteNumero() %></strong>
            <span class="text-muted small">emitida el <%= pedido.getComprobanteFecha() %></span></p>
        <% if (pedido.getComprobanteDocumento() != null) { %>
        <p class="mb-2"><%= "FACTURA".equals(pedido.getComprobanteTipo()) ? "RUC" : "DNI" %>: <%= pedido.getComprobanteDocumento() %></p>
        <% } %>
        <a href="<%= ctx %>/app/pos?action=ticket&id=<%= pedido.getId() %>" class="btn btn-sm btn-outline-dark">
            <i class="bi bi-printer"></i> Ver / imprimir
        </a>
        <% } else if (pedido.getEstado().name().equals("CANCELADO")) { %>
        <p class="mb-0 text-muted">Un pedido cancelado no tiene comprobante.</p>
        <% } else { %>
        <form method="post" action="<%= ctx %>/app/pedidos" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="emitirComprobante">
            <input type="hidden" name="id" value="<%= pedido.getId() %>">
            <div class="col-md-4">
                <label class="form-label small mb-1">Tipo</label>
                <select class="form-select" name="tipoComprobante" id="tipoComprobante" onchange="mostrarDocumento()">
                    <option value="BOLETA_SIMPLE">Boleta simple</option>
                    <option value="BOLETA_DNI">Boleta con DNI</option>
                    <option value="FACTURA">Factura con RUC</option>
                </select>
            </div>
            <div class="col-md-4" id="campoDocumento" style="display:none;">
                <label class="form-label small mb-1" id="etiquetaDocumento">DNI</label>
                <input type="text" class="form-control" name="documento" maxlength="11" inputmode="numeric">
            </div>
            <div class="col-md-4">
                <button type="submit" class="btn text-white" style="background-color:#d97706;">Emitir comprobante</button>
            </div>
        </form>
        <script>
            function mostrarDocumento() {
                var tipo = document.getElementById("tipoComprobante").value;
                document.getElementById("campoDocumento").style.display = tipo === "BOLETA_SIMPLE" ? "none" : "block";
                document.getElementById("etiquetaDocumento").textContent = tipo === "FACTURA" ? "RUC (11 digitos)" : "DNI (8 digitos)";
            }
        </script>
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
