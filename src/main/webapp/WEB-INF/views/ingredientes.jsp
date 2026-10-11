<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Ingrediente" %>
<%@ page import="java.util.List" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%
    request.setAttribute("tituloPagina", "Ingredientes");
    request.setAttribute("paginaActiva", "ingredientes");
    List<Ingrediente> ingredientes = (List<Ingrediente>) request.getAttribute("ingredientes");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Ingredientes (materia prima)</h5>
    <a href="<%= ctx %>/app/ingredientes?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo ingrediente
    </a>
</div>

<form method="get" action="<%= ctx %>/app/ingredientes" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-4">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= Filtros.attr(request, "fq") %>" placeholder="Nombre o unidad">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Estado</label>
            <select class="form-select form-select-sm" name="estado">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "festado"), "") %>>Todos</option>
                <option value="ACTIVO" <%= Filtros.sel(Filtros.attr(request, "festado"), "ACTIVO") %>>Activo</option>
                <option value="INACTIVO" <%= Filtros.sel(Filtros.attr(request, "festado"), "INACTIVO") %>>Inactivo</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Stock</label>
            <select class="form-select form-select-sm" name="stock">
                <option value="" <%= Filtros.sel(Filtros.attr(request, "fstock"), "") %>>Todos</option>
                <option value="BAJO" <%= Filtros.sel(Filtros.attr(request, "fstock"), "BAJO") %>>Solo stock bajo</option>
            </select>
        </div>
        <div class="col-md-3 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-sm btn-dark"><i class="bi bi-search"></i> Buscar</button>
            <a href="<%= ctx %>/app/ingredientes" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Nombre</th>
            <th>Unidad</th>
            <th>Stock</th>
            <th>Stock minimo</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            if (ingredientes != null) {
                for (Ingrediente i : ingredientes) {
        %>
        <tr>
            <td>
                <%= i.getNombre() %>
                <% if (i.isStockBajo()) { %>
                    <span class="badge bg-danger ms-1">Stock bajo</span>
                <% } %>
            </td>
            <td><%= i.getUnidadMedida() %></td>
            <td><%= i.getStock() %></td>
            <td><%= i.getStockMinimo() %></td>
            <td>
                <% if (i.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/ingredientes?action=movimiento&id=<%= i.getId() %>" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-arrow-left-right"></i> Movimiento
                </a>
                <a href="<%= ctx %>/app/ingredientes?action=editar&id=<%= i.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/ingredientes" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= i.getId() %>">
                    <input type="hidden" name="activo" value="<%= !i.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= i.isActivo() ? "danger" : "success" %>">
                        <%= i.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
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
