<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="com.svir.jee.model.Cliente" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Pedidos");
    request.setAttribute("paginaActiva", "pedidos");
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
%>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<h5 class="mb-3">Nuevo pedido</h5>

<div class="card">
    <div class="card-body">
        <form method="post" action="<%= ctx %>/app/pedidos" id="formPedido">
            <div class="row">
                <div class="col-md-6 mb-3">
                    <label class="form-label">Cliente (opcional)</label>
                    <select class="form-select" name="clienteId">
                        <option value="">Cliente general (sin registrar)</option>
                        <%
                            if (clientes != null) {
                                for (Cliente c : clientes) {
                        %>
                        <option value="<%= c.getId() %>"><%= c.getNombre() %></option>
                        <%
                                }
                            }
                        %>
                    </select>
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">Canal</label>
                    <select class="form-select" name="tipoOrigen">
                        <option value="PRESENCIAL">Presencial</option>
                        <option value="WHATSAPP">WhatsApp</option>
                        <option value="TIENDA">Tienda</option>
                        <option value="WEB">Web</option>
                    </select>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label">Observacion</label>
                <input type="text" class="form-control" name="observacion" placeholder="Nota opcional">
            </div>

            <h6>Productos</h6>
            <table class="table" id="tablaLineas">
                <thead>
                <tr>
                    <th style="width:45%">Producto</th>
                    <th style="width:15%">Cantidad</th>
                    <th style="width:15%">Precio unit.</th>
                    <th style="width:15%">Subtotal</th>
                    <th></th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
            <button type="button" class="btn btn-sm btn-outline-secondary mb-3" onclick="agregarLinea()">
                <i class="bi bi-plus-lg"></i> Agregar producto
            </button>

            <div class="text-end mb-3">
                <strong>Total: S/ <span id="totalPedido">0.00</span></strong>
            </div>

            <button type="submit" class="btn text-white" style="background-color:#d97706;">Registrar pedido</button>
            <a href="<%= ctx %>/app/pedidos" class="btn btn-outline-secondary">Cancelar</a>
        </form>
    </div>
</div>

<script>
    // Catalogo de productos activos, generado desde el JSP para el JS del formulario.
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

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
