<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Produccion" %>
<%@ page import="com.svir.jee.model.ProduccionDetalle" %>
<%@ page import="com.svir.jee.model.EstadoProduccion" %>
<%
    request.setAttribute("tituloPagina", "Produccion");
    request.setAttribute("paginaActiva", "producciones");
    Produccion produccion = (Produccion) request.getAttribute("produccion");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<% if (produccion == null) { %>
    <div class="alert alert-danger">Orden de produccion no encontrada.</div>
<% } else {
    boolean puedeGestionar = produccion.getEstado() == EstadoProduccion.PENDIENTE
            || produccion.getEstado() == EstadoProduccion.EN_PROCESO;
%>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Orden de produccion #<%= produccion.getId() %></h5>
    <a href="<%= ctx %>/app/producciones" class="btn btn-sm btn-outline-secondary">Volver</a>
</div>

<div class="card mb-3">
    <div class="card-body">
        <p class="mb-1"><strong>Tipo:</strong> <%= produccion.getTipo() %></p>
        <p class="mb-1"><strong>Pedido origen:</strong> <%= produccion.getPedidoId() != null ? "#" + produccion.getPedidoId() : "-" %></p>
        <p class="mb-1"><strong>Responsable:</strong> <%= produccion.getUsuarioNombre() %></p>
        <p class="mb-1"><strong>Estado:</strong> <span class="badge bg-secondary"><%= produccion.getEstado() %></span></p>
        <p class="mb-0"><strong>Observacion:</strong> <%= produccion.getObservacion() %></p>
    </div>
</div>

<form method="post" action="<%= ctx %>/app/producciones">
    <input type="hidden" name="action" value="terminar">
    <input type="hidden" name="id" value="<%= produccion.getId() %>">
    <table class="table bg-white">
        <thead><tr><th>Producto</th><th>Planificado</th><th>Producido</th></tr></thead>
        <tbody>
        <%
            for (ProduccionDetalle d : produccion.getDetalles()) {
        %>
        <tr>
            <td><%= d.getProductoNombre() %></td>
            <td><%= d.getCantidadPlanificada() %></td>
            <td>
                <% if (puedeGestionar) { %>
                    <input type="hidden" name="detalleId" value="<%= d.getId() %>">
                    <input type="number" name="cantidadProducida" min="0" max="<%= d.getCantidadPlanificada() %>"
                           value="<%= d.getCantidadProducida() > 0 ? d.getCantidadProducida() : d.getCantidadPlanificada() %>"
                           class="form-control form-control-sm" style="max-width:120px;">
                <% } else { %>
                    <%= d.getCantidadProducida() %>
                <% } %>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% if (puedeGestionar) { %>
        <button type="submit" class="btn text-white" style="background-color:#d97706;">Terminar orden</button>
    <% } %>
</form>

<% if (puedeGestionar) { %>
<form method="post" action="<%= ctx %>/app/producciones" class="d-inline"
      onsubmit="return confirm('Se devolveran los ingredientes descontados. Cancelar esta orden?');">
    <input type="hidden" name="action" value="cancelar">
    <input type="hidden" name="id" value="<%= produccion.getId() %>">
    <button type="submit" class="btn btn-outline-danger">Cancelar orden</button>
</form>
<% } %>

<% } %>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
