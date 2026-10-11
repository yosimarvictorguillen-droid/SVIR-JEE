<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Invitado" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Invitados");
    request.setAttribute("paginaActiva", "invitados");
    List<Invitado> invitados = (List<Invitado>) request.getAttribute("invitados");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Invitados</h5>
    <span class="text-muted small">Compradores sin cuenta, identificados por su documento. Al registrarse pasan a Clientes.</span>
</div>

<form method="get" action="<%= ctx %>/app/invitados" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-4">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= Filtros.attr(request, "fq") %>" placeholder="Nombre, documento o telefono">
        </div>
        <div class="col-md-3">
            <label class="form-label small mb-1">Estado</label>
            <select class="form-select form-select-sm" name="estado">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "festado"), "") %>>Todos</option>
                <option value="ACTIVO" <%= Filtros.sel(Filtros.attr(request, "festado"), "ACTIVO") %>>Invitado (sin cuenta)</option>
                <option value="CONVERTIDO" <%= Filtros.sel(Filtros.attr(request, "festado"), "CONVERTIDO") %>>Convertido a cliente</option>
            </select>
        </div>
        <div class="col-md-3 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-sm btn-dark"><i class="bi bi-search"></i> Buscar</button>
            <a href="<%= ctx %>/app/invitados" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Documento</th>
            <th>Nombre</th>
            <th>Telefono</th>
            <th>Pedidos</th>
            <th>Total comprado</th>
            <th>Ultimo pedido</th>
            <th>Estado</th>
            <th>Se hizo cliente</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <% if (invitados != null) { for (Invitado i : invitados) { %>
        <tr>
            <td><span class="small text-muted"><%= i.getTipoDocumento() %></span> <%= i.getDocumento() %></td>
            <td><%= i.getNombre() %></td>
            <td><%= i.getTelefono() != null ? i.getTelefono() : "-" %></td>
            <td><%= i.getTotalPedidos() %></td>
            <td>S/ <%= i.getTotalComprado() %></td>
            <td><%= i.getUltimoPedido() != null ? i.getUltimoPedido() : "-" %></td>
            <td>
                <% if (i.isConvertido()) { %>
                <span class="badge bg-secondary">Convertido</span>
                <% } else { %>
                <span class="badge bg-warning text-dark">Invitado</span>
                <% } %>
            </td>
            <td>
                <% if (i.isConvertido()) { %>
                <%= i.getFechaConversion() %><br>
                <span class="small text-muted">Cliente: <%= i.getClienteNombre() %>
                    <% if (i.getPedidosVinculados() != null) { %>(<%= i.getPedidosVinculados() %> pedido(s) vinculados)<% } %></span>
                <% } else { %>-<% } %>
            </td>
            <td>
                <a href="<%= ctx %>/app/pedidos?q=<%= java.net.URLEncoder.encode(i.getDocumento(), "UTF-8") %>" class="btn btn-sm btn-outline-secondary" title="Ver pedidos">
                    <i class="bi bi-receipt"></i>
                </a>
            </td>
        </tr>
        <% } } %>
        <% if (invitados == null || invitados.isEmpty()) { %>
        <tr><td colspan="9" class="text-center text-muted py-4">No se encontraron invitados.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>

<% request.removeAttribute("pgSufijoActual"); %>
<jsp:include page="/WEB-INF/views/common/paginacion.jsp"/>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
