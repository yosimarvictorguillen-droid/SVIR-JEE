<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%@ page import="com.svir.jee.util.Filtros" %>
<%
    request.setAttribute("tituloPagina", "Productos");
    request.setAttribute("paginaActiva", "productos");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Catalogo de productos</h5>
    <a href="<%= ctx %>/app/productos?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo producto
    </a>
</div>

<form method="get" action="<%= ctx %>/app/productos" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-4">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= Filtros.attr(request, "fq") %>" placeholder="Nombre del producto">
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
            <a href="<%= ctx %>/app/productos" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>Foto</th>
            <th>Nombre</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Stock minimo</th>
            <th>Estado</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            for (Producto p : productos) {
        %>
        <tr>
            <td>
                <% if (p.getImagenUrl() != null) { %>
                    <img src="<%= ctx %><%= p.getImagenUrl() %>" alt="<%= p.getNombre() %>"
                         style="width:48px;height:48px;object-fit:cover;border-radius:.4rem;">
                <% } else { %>
                    <span class="text-muted small">Sin foto</span>
                <% } %>
            </td>
            <td>
                <%= p.getNombre() %>
                <% if (p.isStockBajo()) { %>
                    <span class="badge bg-danger ms-1">Stock bajo</span>
                <% } %>
            </td>
            <td>S/ <%= p.getPrecio() %></td>
            <td><%= p.getStock() %></td>
            <td><%= p.getStockMinimo() %></td>
            <td>
                <% if (p.isActivo()) { %>
                    <span class="badge bg-success">Activo</span>
                <% } else { %>
                    <span class="badge bg-secondary">Inactivo</span>
                <% } %>
            </td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/productos?action=editar&id=<%= p.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-pencil"></i>
                </a>
                <form method="post" action="<%= ctx %>/app/productos" class="d-inline">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="activo" value="<%= !p.isActivo() %>">
                    <button type="submit" class="btn btn-sm btn-outline-<%= p.isActivo() ? "danger" : "success" %>">
                        <%= p.isActivo() ? "Desactivar" : "Activar" %>
                    </button>
                </form>
            </td>
        </tr>
        <% } %>
        <% if (productos.isEmpty()) { %>
        <tr><td colspan="7" class="text-center text-muted">No hay productos registrados.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>

<% request.removeAttribute("pgSufijoActual"); %>
<jsp:include page="/WEB-INF/views/common/paginacion.jsp"/>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
