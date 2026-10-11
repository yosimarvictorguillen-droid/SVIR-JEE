<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Cliente" %>
<%@ page import="java.util.List" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%@ page import="com.svir.jee.util.Textos" %>
<%
    request.setAttribute("tituloPagina", "Clientes");
    request.setAttribute("paginaActiva", "clientes");
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Clientes</h5>
    <a href="<%= ctx %>/app/clientes?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo cliente
    </a>
</div>

<%
    int vinculadosAlta = 0;
    try {
        vinculadosAlta = Integer.parseInt(request.getParameter("vinculados"));
    } catch (NumberFormatException ignored) {
        // sin parametro
    }
%>
<% if (vinculadosAlta > 0) { %>
<div class="alert alert-success">Cliente creado. Se vincularon <%= vinculadosAlta %> pedido(s) de cuando compraba como invitado.</div>
<% } %>

<form method="get" action="<%= ctx %>/app/clientes" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-4">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= Filtros.attr(request, "fq") %>" placeholder="Nombre, documento, telefono o email">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Estado</label>
            <select class="form-select form-select-sm" name="estado">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "festado"), "") %>>Todos</option>
                <option value="ACTIVO" <%= Filtros.sel(Filtros.attr(request, "festado"), "ACTIVO") %>>Activo</option>
                <option value="INACTIVO" <%= Filtros.sel(Filtros.attr(request, "festado"), "INACTIVO") %>>Inactivo</option>
            </select>
        </div>
        <div class="col-md-3 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-sm btn-dark"><i class="bi bi-search"></i> Buscar</button>
            <a href="<%= ctx %>/app/clientes" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Nombre</th>
            <th>Documento</th>
            <th>Telefono</th>
            <th>Direccion</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            for (Cliente c : clientes) {
        %>
        <tr>
            <td><%= c.getNombre() %>
                <% if (c.isAntesInvitado()) { %>
                <span class="badge bg-secondary" title="Compraba como invitado y se registro el <%= c.getFechaConversionInvitado().toLocalDate() %>">Antes invitado <%= c.getFechaConversionInvitado().toLocalDate() %></span>
                <% } %>
            </td>
            <td><% if (c.getDni() != null) { %><span class="small text-muted"><%= c.getTipoDocumento() %></span> <%= c.getDni() %><% } else { %>-<% } %></td>
            <td><%= c.getTelefono() != null ? c.getTelefono() : "-" %></td>
            <td><%= c.getDireccion() != null ? c.getDireccion() : "-" %></td>
            <td>
                <% if (c.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/clientes?action=editar&id=<%= c.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/clientes" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= c.getId() %>">
                    <input type="hidden" name="activo" value="<%= !c.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= c.isActivo() ? "danger" : "success" %>">
                        <%= c.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
            </td>
        </tr>
        <% } %>
        <% if (clientes.isEmpty()) { %>
        <tr><td colspan="6" class="text-center text-muted">No hay clientes registrados.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>

<% request.removeAttribute("pgSufijoActual"); %>
<jsp:include page="/WEB-INF/views/common/paginacion.jsp"/>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
