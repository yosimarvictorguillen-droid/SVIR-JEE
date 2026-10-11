<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Produccion" %>
<%@ page import="java.util.List" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%
    request.setAttribute("tituloPagina", "Produccion");
    request.setAttribute("paginaActiva", "producciones");
    List<Produccion> producciones = (List<Produccion>) request.getAttribute("producciones");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Ordenes de produccion</h5>
    <div>
        <a href="<%= ctx %>/app/producciones?action=porPedido" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-receipt"></i> Por pedido
        </a>
        <a href="<%= ctx %>/app/producciones?action=nueva" class="btn btn-sm text-white" style="background-color:#d97706;">
            <i class="bi bi-plus-lg"></i> Para stock
        </a>
    </div>
</div>

<form method="get" action="<%= ctx %>/app/producciones" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-3">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= Filtros.attr(request, "fq") %>" placeholder="NÂ° de produccion, pedido o responsable">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Tipo</label>
            <select class="form-select form-select-sm" name="tipo">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "ftipo"), "") %>>Todos</option>
                <option value="PEDIDO" <%= Filtros.sel(Filtros.attr(request, "ftipo"), "PEDIDO") %>>Por pedido</option>
                <option value="STOCK" <%= Filtros.sel(Filtros.attr(request, "ftipo"), "STOCK") %>>Para stock</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Estado</label>
            <select class="form-select form-select-sm" name="estado">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "festado"), "") %>>Todos</option>
                <option value="PENDIENTE" <%= Filtros.sel(Filtros.attr(request, "festado"), "PENDIENTE") %>>Pendiente</option>
                <option value="EN_PROCESO" <%= Filtros.sel(Filtros.attr(request, "festado"), "EN_PROCESO") %>>En proceso</option>
                <option value="TERMINADO" <%= Filtros.sel(Filtros.attr(request, "festado"), "TERMINADO") %>>Terminado</option>
                <option value="CANCELADO" <%= Filtros.sel(Filtros.attr(request, "festado"), "CANCELADO") %>>Cancelado</option>
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
            <a href="<%= ctx %>/app/producciones" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead><tr><th>#</th><th>Tipo</th><th>Pedido</th><th>Responsable</th><th>Estado</th><th>Fecha</th><th></th></tr></thead>
        <tbody>
        <%
            if (producciones != null) {
                for (Produccion p : producciones) {
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= p.getTipo() %></td>
            <td><%= p.getPedidoId() != null ? "#" + p.getPedidoId() : "-" %></td>
            <td><%= p.getUsuarioNombre() %></td>
            <td><span class="badge bg-secondary"><%= p.getEstado() %></span></td>
            <td><%= p.getCreatedAt() %></td>
            <td>
                <a href="<%= ctx %>/app/producciones?action=ver&id=<%= p.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-eye"></i>
                </a>
            </td>
        </tr>
        <%
                }
            }
        %>
        </tbody>
    </table>
</div>

<% request.removeAttribute("pgSufijoActual"); %>
<jsp:include page="/WEB-INF/views/common/paginacion.jsp"/>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
