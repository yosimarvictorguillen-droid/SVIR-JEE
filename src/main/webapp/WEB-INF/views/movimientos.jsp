<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.MovimientoProducto" %>
<%@ page import="com.svir.jee.model.MovimientoIngrediente" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Movimientos");
    request.setAttribute("paginaActiva", "movimientos");
    List<MovimientoProducto> movimientosProducto = (List<MovimientoProducto>) request.getAttribute("movimientosProducto");
    List<MovimientoIngrediente> movimientosIngrediente = (List<MovimientoIngrediente>) request.getAttribute("movimientosIngrediente");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Movimientos de productos</h5>
<div class="table-responsive mb-5">
    <table class="table table-sm bg-white">
        <thead><tr><th>Producto</th><th>Tipo</th><th>Motivo</th><th>Cantidad</th><th>Stock ant.</th><th>Stock nuevo</th><th>Referencia</th><th>Usuario</th><th>Fecha</th></tr></thead>
        <tbody>
        <%
            if (movimientosProducto != null) {
                for (MovimientoProducto m : movimientosProducto) {
        %>
        <tr>
            <td><%= m.getProductoNombre() %></td>
            <td><span class="badge bg-<%= m.getTipo().name().equals("ENTRADA") ? "success" : "danger" %>"><%= m.getTipo() %></span></td>
            <td><%= m.getMotivo() %></td>
            <td><%= m.getCantidad() %></td>
            <td><%= m.getStockAnterior() %></td>
            <td><%= m.getStockNuevo() %></td>
            <td><%= m.getReferenciaTipo() %><%= m.getReferenciaId() != null ? " #" + m.getReferenciaId() : "" %></td>
            <td><%= m.getUsuarioNombre() != null ? m.getUsuarioNombre() : "-" %></td>
            <td><%= m.getCreatedAt() %></td>
        </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</div>

<h5 class="mb-3">Movimientos de ingredientes</h5>
<div class="table-responsive">
    <table class="table table-sm bg-white">
        <thead><tr><th>Ingrediente</th><th>Tipo</th><th>Motivo</th><th>Cantidad</th><th>Stock ant.</th><th>Stock nuevo</th><th>Referencia</th><th>Usuario</th><th>Fecha</th></tr></thead>
        <tbody>
        <%
            if (movimientosIngrediente != null) {
                for (MovimientoIngrediente m : movimientosIngrediente) {
        %>
        <tr>
            <td><%= m.getIngredienteNombre() %></td>
            <td><span class="badge bg-<%= m.getTipo().name().equals("ENTRADA") ? "success" : "danger" %>"><%= m.getTipo() %></span></td>
            <td><%= m.getMotivo() %></td>
            <td><%= m.getCantidad() %></td>
            <td><%= m.getStockAnterior() %></td>
            <td><%= m.getStockNuevo() %></td>
            <td><%= m.getReferenciaTipo() %><%= m.getReferenciaId() != null ? " #" + m.getReferenciaId() : "" %></td>
            <td><%= m.getUsuarioNombre() != null ? m.getUsuarioNombre() : "-" %></td>
            <td><%= m.getCreatedAt() %></td>
        </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
