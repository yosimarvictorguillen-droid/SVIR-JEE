<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Punto de Venta");
    request.setAttribute("paginaActiva", "pos");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Punto de venta</h5>

<div class="card">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/pos">
            <h6>Productos</h6>
            <table class="table" id="tablaLineas">
                <thead>
                <tr><th style="width:40%">Producto</th><th style="width:15%">Cantidad</th><th style="width:15%">Precio</th><th style="width:15%">Subtotal</th><th></th></tr>
                </thead>
                <tbody></tbody>
            </table>
            <button type="button" class="btn btn-sm btn-outline-secondary mb-3" onclick="agregarLinea()">
                <i class="bi bi-plus-lg"></i> Agregar producto
            </button>
            <div class="text-end mb-4"><strong>Total: S/ <span id="totalPedido">0.00</span></strong></div>

            <h6>Comprobante</h6>
            <div class="row mb-3">
                <div class="col-md-4">
                    <select class="form-select" name="tipoComprobante" id="tipoComprobante" onchange="mostrarCamposComprobante()">
                        <option value="BOLETA_SIMPLE">Boleta simple</option>
                        <option value="BOLETA_DNI">Boleta con DNI</option>
                        <option value="FACTURA">Factura con RUC</option>
                    </select>
                </div>
                <div class="col-md-4" id="campoDocumento" style="display:none;">
                    <input type="text" class="form-control" name="documento" placeholder="DNI o RUC">
                </div>
                <div class="col-md-4" id="campoRazonSocial" style="display:none;">
                    <input type="text" class="form-control" name="razonSocial" placeholder="Nombre / Razon social">
                </div>
            </div>

            <button type="submit" class="btn text-white" style="background-color:#d97706;">Cobrar</button>
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
        { id: <%= p.getId() %>, nombre: "<%= p.getNombre().replace("\"", "'") %>", precio: <%= p.getPrecio() %>, stock: <%= p.getStock() %> }<%= idx < productos.size() - 1 ? "," : "" %>
        <%
                }
            }
        %>
    ];
</script>
<script src="<%= ctx %>/assets/js/carrito-lineas.js"></script>
<script>
    function mostrarCamposComprobante() {
        const tipo = document.getElementById("tipoComprobante").value;
        document.getElementById("campoDocumento").style.display = tipo === "BOLETA_SIMPLE" ? "none" : "block";
        document.getElementById("campoRazonSocial").style.display = tipo === "BOLETA_SIMPLE" ? "none" : "block";
    }
</script>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
