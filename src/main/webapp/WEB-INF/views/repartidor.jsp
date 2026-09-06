<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Pedido" %>
<%@ page import="com.svir.jee.model.EstadoPedido" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%!
    private List<Pedido> filtrarPorEstado(List<Pedido> pedidos, EstadoPedido estado) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (p.getEstado() == estado) {
                resultado.add(p);
            }
        }
        return resultado;
    }
%>
<%
    request.setAttribute("tituloPagina", "Repartidor");
    request.setAttribute("paginaActiva", "repartidor");
    List<Pedido> pedidosDelivery = (List<Pedido>) request.getAttribute("pedidosDelivery");
    if (pedidosDelivery == null) pedidosDelivery = new ArrayList<>();
    List<Pedido> paraRecoger = filtrarPorEstado(pedidosDelivery, EstadoPedido.LISTO);
    List<Pedido> enCamino = filtrarPorEstado(pedidosDelivery, EstadoPedido.EN_CAMINO);
    List<Pedido> entregadosHoy = filtrarPorEstado(pedidosDelivery, EstadoPedido.ENTREGADO);
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Panel de reparto</h5>

<div class="row g-3">
    <div class="col-md-4">
        <h6>Para recoger (<%= paraRecoger.size() %>)</h6>
        <%
            for (Pedido p : paraRecoger) {
        %>
        <div class="card mb-2">
            <div class="card-body">
                <p class="mb-1"><strong>Pedido #<%= p.getId() %></strong></p>
                <% for (String parte : (p.getObservacion() != null ? p.getObservacion() : "").split("\\|")) { %>
                    <div class="small text-muted"><%= parte.trim() %></div>
                <% } %>
                <p class="mb-2 mt-2"><strong>Total: S/ <%= p.getTotal() %></strong></p>
                <form method="post" action="<%= ctx %>/app/repartidor">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="estado" value="EN_CAMINO">
                    <button type="submit" class="btn btn-sm text-white w-100" style="background-color:#d97706;">
                        Salir a entregar
                    </button>
                </form>
            </div>
        </div>
        <% } %>
        <% if (paraRecoger.isEmpty()) { %><p class="text-muted small">Nada por recoger.</p><% } %>
    </div>

    <div class="col-md-4">
        <h6>En camino (<%= enCamino.size() %>)</h6>
        <%
            for (Pedido p : enCamino) {
        %>
        <div class="card mb-2">
            <div class="card-body">
                <p class="mb-1"><strong>Pedido #<%= p.getId() %></strong></p>
                <% for (String parte : (p.getObservacion() != null ? p.getObservacion() : "").split("\\|")) { %>
                    <div class="small text-muted"><%= parte.trim() %></div>
                <% } %>
                <p class="mb-2 mt-2"><strong>Total: S/ <%= p.getTotal() %></strong></p>
                <form method="post" action="<%= ctx %>/app/repartidor">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input type="hidden" name="estado" value="ENTREGADO">
                    <button type="submit" class="btn btn-sm btn-success w-100">Marcar entregado</button>
                </form>
            </div>
        </div>
        <% } %>
        <% if (enCamino.isEmpty()) { %><p class="text-muted small">Nadie en camino.</p><% } %>
    </div>

    <div class="col-md-4">
        <h6>Entregados hoy (<%= entregadosHoy.size() %>)</h6>
        <%
            for (Pedido p : entregadosHoy) {
        %>
        <div class="card mb-2">
            <div class="card-body">
                <p class="mb-1"><strong>Pedido #<%= p.getId() %></strong></p>
                <p class="mb-0"><strong>Total: S/ <%= p.getTotal() %></strong></p>
            </div>
        </div>
        <% } %>
        <% if (entregadosHoy.isEmpty()) { %><p class="text-muted small">Aun no hay entregas hoy.</p><% } %>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
