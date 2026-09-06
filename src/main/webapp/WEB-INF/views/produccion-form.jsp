<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Produccion");
    request.setAttribute("paginaActiva", "producciones");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Nueva orden de produccion (para reponer stock)</h5>
<p class="text-muted">Cada producto debe tener una receta configurada; si falta, la orden no se podra crear.</p>

<div class="card">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/producciones">
            <table class="table" id="tablaLineas">
                <thead><tr><th style="width:60%">Producto</th><th style="width:30%">Cantidad a producir</th><th></th></tr></thead>
                <tbody></tbody>
            </table>
            <button type="button" class="btn btn-sm btn-outline-secondary mb-3" onclick="agregarLinea()">
                <i class="bi bi-plus-lg"></i> Agregar producto
            </button>
            <br>
            <button type="submit" class="btn text-white" style="background-color:#d97706;">Crear orden</button>
            <a href="<%= ctx %>/app/producciones" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<script>
    const CATALOGO = [
        <%
            if (productos != null) {
                for (int idx = 0; idx < productos.size(); idx++) {
                    Producto p = productos.get(idx);
        %>
        { id: <%= p.getId() %>, nombre: "<%= p.getNombre().replace("\"", "'") %>" }<%= idx < productos.size() - 1 ? "," : "" %>
        <%
                }
            }
        %>
    ];
</script>
<script src="<%= ctx %>/assets/js/produccion-lineas.js"></script>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
