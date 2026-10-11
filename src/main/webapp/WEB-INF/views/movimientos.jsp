<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.MovimientoProducto" %>
<%@ page import="com.svir.jee.model.MovimientoIngrediente" %>
<%@ page import="java.util.List" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%
    request.setAttribute("tituloPagina", "Movimientos");
    request.setAttribute("paginaActiva", "movimientos");
    List<MovimientoProducto> movimientosProducto = (List<MovimientoProducto>) request.getAttribute("movimientosProducto");
    List<MovimientoIngrediente> movimientosIngrediente = (List<MovimientoIngrediente>) request.getAttribute("movimientosIngrediente");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<form method="get" action="<%= ctx %>/app/movimientos" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-4">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= Filtros.attr(request, "fq") %>" placeholder="Nombre del producto o ingrediente">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Tipo</label>
            <select class="form-select form-select-sm" name="tipo">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "ftipo"), "") %>>Todos</option>
                <option value="ENTRADA" <%= Filtros.sel(Filtros.attr(request, "ftipo"), "ENTRADA") %>>Entrada</option>
                <option value="SALIDA" <%= Filtros.sel(Filtros.attr(request, "ftipo"), "SALIDA") %>>Salida</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Mostrar</label>
            <select class="form-select form-select-sm" name="ver">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "fver"), "") %>>Productos e ingredientes</option>
                <option value="PRODUCTOS" <%= Filtros.sel(Filtros.attr(request, "fver"), "PRODUCTOS") %>>Solo productos</option>
                <option value="INGREDIENTES" <%= Filtros.sel(Filtros.attr(request, "fver"), "INGREDIENTES") %>>Solo ingredientes</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Desde</label>
            <input type="date" class="form-control form-control-sm" name="desde" value="<%= Filtros.attr(request, "fdesde") %>">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Hasta</label>
            <input type="date" class="form-control form-control-sm" name="hasta" value="<%= Filtros.attr(request, "fhasta") %>">
        </div>
        <div class="col-md-3 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-sm btn-dark"><i class="bi bi-search"></i> Buscar</button>
            <a href="<%= ctx %>/app/movimientos" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<% if (movimientosProducto != null) { %>
<h5 class="mb-3">Movimientos de productos</h5>
<div class="table-responsive mb-3">
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
<% request.setAttribute("pgSufijoActual", "P"); %>
<jsp:include page="/WEB-INF/views/common/paginacion.jsp"/>

<% } %>

<% if (movimientosIngrediente != null) { %>
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
<% request.setAttribute("pgSufijoActual", "I"); %>
<jsp:include page="/WEB-INF/views/common/paginacion.jsp"/>

<% } %>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
