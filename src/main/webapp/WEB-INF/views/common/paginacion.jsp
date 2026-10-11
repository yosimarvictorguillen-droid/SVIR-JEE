<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Pie de paginacion compartido. Se incluye con <jsp:include> despues de Pagina.publicar(...).
    // El atributo "sufijo" permite dos paginadores en la misma pantalla (ej. Movimientos).
    String sufijo = request.getAttribute("pgSufijoActual") == null ? "" : (String) request.getAttribute("pgSufijoActual");
    if (request.getAttribute("pgPagina" + sufijo) != null) {
        int pgPagina = (Integer) request.getAttribute("pgPagina" + sufijo);
        int pgTotalPaginas = (Integer) request.getAttribute("pgTotalPaginas" + sufijo);
        int pgTotal = (Integer) request.getAttribute("pgTotal" + sufijo);
        int pgPorPagina = (Integer) request.getAttribute("pgPorPagina" + sufijo);
        String pgUrl = (String) request.getAttribute("pgUrl" + sufijo);
        int desdeFila = pgTotal == 0 ? 0 : (pgPagina - 1) * pgPorPagina + 1;
        int hastaFila = Math.min(pgPagina * pgPorPagina, pgTotal);
%>
<div class="d-flex justify-content-between align-items-center mb-3">
    <small class="text-muted">Mostrando <%= desdeFila %>-<%= hastaFila %> de <%= pgTotal %></small>
    <div class="btn-group btn-group-sm">
        <% if (pgPagina > 1) { %>
        <a class="btn btn-outline-secondary" href="<%= pgUrl %><%= pgPagina - 1 %>">Anterior</a>
        <% } else { %>
        <span class="btn btn-outline-secondary disabled">Anterior</span>
        <% } %>
        <span class="btn btn-outline-secondary disabled">Pagina <%= pgPagina %> de <%= pgTotalPaginas %></span>
        <% if (pgPagina < pgTotalPaginas) { %>
        <a class="btn btn-outline-secondary" href="<%= pgUrl %><%= pgPagina + 1 %>">Siguiente</a>
        <% } else { %>
        <span class="btn btn-outline-secondary disabled">Siguiente</span>
        <% } %>
    </div>
</div>
<% } %>
