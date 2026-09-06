<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Ingrediente" %>
<%
    request.setAttribute("tituloPagina", "Ingredientes");
    request.setAttribute("paginaActiva", "ingredientes");
    Ingrediente ingrediente = (Ingrediente) request.getAttribute("ingrediente");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Registrar movimiento de stock</h5>

<% if (ingrediente == null) { %>
    <div class="alert alert-danger">Ingrediente no encontrado.</div>
<% } else { %>
<div class="card" style="max-width:600px;">
    <div class="card-body">
        <p class="mb-3">
            <strong><%= ingrediente.getNombre() %></strong> &mdash;
            stock actual: <strong><%= ingrediente.getStock() %> <%= ingrediente.getUnidadMedida() %></strong>
        </p>
        <form method="post" action="<%= ctx %>/app/ingredientes">
            <input type="hidden" name="action" value="movimiento">
            <input type="hidden" name="id" value="<%= ingrediente.getId() %>">

            <div class="mb-3">
                <label class="form-label">Tipo de movimiento</label>
                <select class="form-select" name="tipo" required>
                    <option value="ENTRADA">Entrada (compra / reposicion)</option>
                    <option value="SALIDA">Salida (merma / uso manual)</option>
                    <option value="AJUSTE">Ajuste (fijar stock exacto)</option>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">Motivo</label>
                <select class="form-select" name="motivo" required>
                    <option value="COMPRA">Compra</option>
                    <option value="MERMA">Merma</option>
                    <option value="AJUSTE_MANUAL">Ajuste manual</option>
                </select>
            </div>
            <div class="mb-3">
                <label class="form-label">Cantidad (<%= ingrediente.getUnidadMedida() %>)</label>
                <input type="number" step="0.01" min="0" class="form-control" name="cantidad" required>
                <div class="form-text">
                    Para ENTRADA/SALIDA es la cantidad a sumar o restar. Para AJUSTE es el nuevo stock exacto.
                </div>
            </div>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Registrar</button>
            <a href="<%= ctx %>/app/ingredientes" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>
<% } %>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
