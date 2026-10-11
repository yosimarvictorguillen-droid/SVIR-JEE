<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.EstadoPedido" %>
<%@ page import="com.svir.jee.model.FiltroPedido" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Pedidos");
    request.setAttribute("paginaActiva", "pedidos");
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h5 class="mb-0">Pedidos</h5>
    <a href="<%= ctx %>/app/pedidos?action=nuevo" class="btn btn-sm text-white" style="background-color:#d97706;">
        <i class="bi bi-plus-lg"></i> Nuevo pedido
    </a>
</div>

<%!
    private static String sel(String actual, String valor) {
        return actual != null && actual.equals(valor) ? "selected" : "";
    }
%>
<%
    // Si llegamos aqui desde un error (sin pasar por la busqueda) se usan valores por defecto.
    FiltroPedido f = request.getAttribute("filtroPedido") != null ? (FiltroPedido) request.getAttribute("filtroPedido") : new FiltroPedido();
    int totalPedidos = request.getAttribute("totalPedidos") != null ? (Integer) request.getAttribute("totalPedidos") : (pedidos == null ? 0 : pedidos.size());
    int pagina = request.getAttribute("pagina") != null ? (Integer) request.getAttribute("pagina") : 1;
    int totalPaginas = request.getAttribute("totalPaginas") != null ? (Integer) request.getAttribute("totalPaginas") : 1;
    int porPagina = request.getAttribute("porPagina") != null ? (Integer) request.getAttribute("porPagina") : Math.max(1, totalPedidos);
    String queryFiltros = request.getAttribute("queryFiltros") != null ? (String) request.getAttribute("queryFiltros") : "";
    String[][] estados = {
        {"", "Todos"}, {"ACTIVOS", "Activos (sin cancelados)"}, {"ABIERTOS", "Pendientes / en preparacion"},
        {"PENDIENTE", "Pendiente"}, {"PARCIAL", "Parcial"}, {"PREPARACION", "En preparacion"},
        {"LISTO", "Listo"}, {"EN_CAMINO", "En camino"}, {"ENTREGADO", "Entregado"}, {"CANCELADO", "Cancelado"}
    };
    String[][] canales = {
        {"", "Todos"}, {"PRESENCIAL", "Presencial"}, {"WHATSAPP", "WhatsApp"}, {"TIENDA", "Tienda"},
        {"WEB", "Web"}, {"DELIVERY", "Delivery"}
    };
%>
<form method="get" action="<%= ctx %>/app/pedidos" class="card card-body mb-3">
    <div class="row g-2">
        <div class="col-md-4">
            <label class="form-label small mb-1">Buscar</label>
            <input type="text" class="form-control form-control-sm" name="q" maxlength="100" value="<%= f.getTexto() %>"
                   placeholder="NÂ° de pedido, nombre, telefono o comprobante">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Estado</label>
            <select class="form-select form-select-sm" name="estado">
                <% for (String[] e : estados) { %><option value="<%= e[0] %>" <%= sel(f.getEstado(), e[0]) %>><%= e[1] %></option><% } %>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Canal</label>
            <select class="form-select form-select-sm" name="origen">
                <% for (String[] c : canales) { %><option value="<%= c[0] %>" <%= sel(f.getOrigen(), c[0]) %>><%= c[1] %></option><% } %>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Tipo de cliente</label>
            <select class="form-select form-select-sm" name="tipoCliente">
                <option value="">Todos</option>
                <option value="Cliente" <%= sel(f.getTipoCliente(), "Cliente") %>>Cliente</option>
                <option value="Invitado" <%= sel(f.getTipoCliente(), "Invitado") %>>Invitado</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Comprobante</label>
            <select class="form-select form-select-sm" name="comprobante">
                <option value="">Todos</option>
                <option value="CON" <%= sel(f.getComprobante(), "CON") %>>Con comprobante</option>
                <option value="SIN" <%= sel(f.getComprobante(), "SIN") %>>Sin comprobante</option>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Desde</label>
            <input type="date" class="form-control form-control-sm" name="desde" value="<%= f.getDesde() == null ? "" : f.getDesde() %>">
        </div>
        <div class="col-md-2">
            <label class="form-label small mb-1">Hasta</label>
            <input type="date" class="form-control form-control-sm" name="hasta" value="<%= f.getHasta() == null ? "" : f.getHasta() %>">
        </div>
        <div class="col-md-8 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-sm btn-dark"><i class="bi bi-search"></i> Buscar</button>
            <a href="<%= ctx %>/app/pedidos" class="btn btn-sm btn-outline-secondary">Limpiar</a>
        </div>
    </div>
</form>

<div class="table-responsive">
    <table class="table table-hover align-middle bg-white">
        <thead>
        <tr>
            <th>#</th>
            <th>Cliente</th>
            <th>Tipo de cliente</th>
            <th>Canal</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Comprobante</th>
            <th>Fecha</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            if (pedidos != null) {
                for (Pedido p : pedidos) {
                    String cliente = p.getClienteNombre() != null ? p.getClienteNombre() : "Sin nombre";
                    boolean esTerminal = p.getEstado() == EstadoPedido.ENTREGADO || p.getEstado() == EstadoPedido.CANCELADO;
        %>
        <tr>
            <td>#<%= p.getId() %></td>
            <td><%= cliente %></td>
            <td><span class="badge <%= "Cliente".equals(p.getTipoComprador()) ? "bg-info text-dark" : ("Invitado".equals(p.getTipoComprador()) ? "bg-warning text-dark" : "bg-secondary") %>"><%= p.getTipoComprador() %></span></td>
            <td><%= p.getTipoOrigen() %></td>
            <td>S/ <%= p.getTotal() %></td>
            <td><span class="badge bg-secondary badge-estado"><%= p.getEstado() %></span></td>
            <td><%= p.isTieneComprobante() ? p.getComprobanteNumero() : "-" %></td>
            <td><%= p.getCreatedAt() %></td>
            <td class="table-actions">
                <a href="<%= ctx %>/app/pedidos?action=ver&id=<%= p.getId() %>" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-eye"></i>
                </a>
                <% if (!esTerminal) { %>
                <% if (p.getTipoOrigen().name().equals("DELIVERY") && p.getEstado() == EstadoPedido.LISTO) { %>
                <form method="post" action="<%= ctx %>/app/pedidos" class="d-inline">
                    <input type="hidden" name="action" value="cambiarEstado">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="estado" value="EN_CAMINO">
                    <button type="submit" class="btn btn-sm btn-outline-primary">En camino</button>
                </form>
                <% } %>
                <form method="post" action="<%= ctx %>/app/pedidos" class="d-inline">
                    <input type="hidden" name="action" value="cambiarEstado">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="estado" value="ENTREGADO">
                    <button type="submit" class="btn btn-sm btn-outline-success">Entregar</button>
                </form>
                <form method="post" action="<%= ctx %>/app/pedidos" class="d-inline"
                      onsubmit="return confirm('Se devolvera el stock atendido. Cancelar este pedido?');">
                    <input type="hidden" name="action" value="cancelar">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <button type="submit" class="btn btn-sm btn-outline-danger">Cancelar</button>
                </form>
                <% } %>
            </td>
        </tr>
        <%
                }
            }
        %>
        <% if (pedidos == null || pedidos.isEmpty()) { %>
        <tr><td colspan="9" class="text-center text-muted py-4">No se encontraron pedidos con esos filtros.</td></tr>
        <% } %>
        </tbody>
    </table>
</div>


<%
    int desdeFila = totalPedidos == 0 ? 0 : (pagina - 1) * porPagina + 1;
    int hastaFila = Math.min(pagina * porPagina, totalPedidos);
    String base = ctx + "/app/pedidos?" + queryFiltros + (queryFiltros.isEmpty() ? "" : "&");
%>
<div class="d-flex justify-content-between align-items-center mb-3">
    <small class="text-muted">Mostrando <%= desdeFila %>-<%= hastaFila %> de <%= totalPedidos %></small>
    <div class="btn-group btn-group-sm">
        <% if (pagina > 1) { %>
        <a class="btn btn-outline-secondary" href="<%= base %>pagina=<%= pagina - 1 %>">Anterior</a>
        <% } else { %>
        <span class="btn btn-outline-secondary disabled">Anterior</span>
        <% } %>
        <span class="btn btn-outline-secondary disabled">Pagina <%= pagina %> de <%= totalPaginas %></span>
        <% if (pagina < totalPaginas) { %>
        <a class="btn btn-outline-secondary" href="<%= base %>pagina=<%= pagina + 1 %>">Siguiente</a>
        <% } else { %>
        <span class="btn btn-outline-secondary disabled">Siguiente</span>
        <% } %>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
