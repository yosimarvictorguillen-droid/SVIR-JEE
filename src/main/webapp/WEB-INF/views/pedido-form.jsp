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
                    <select class="form-select" name="clienteId" id="clienteId" onchange="alternarNombre()">
                        <option value="">Invitado (no registrado)</option>
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
                <div class="col-md-6 mb-3" id="campoNombre">
                    <div class="row g-2">
                        <div class="col-5">
                            <label class="form-label">Documento</label>
                            <select class="form-select" name="compradorTipoDoc">
                                <option value="DNI">DNI</option>
                                <option value="CE">Carne ext.</option>
                                <option value="PASAPORTE">Pasaporte</option>
                            </select>
                        </div>
                        <div class="col-7">
                            <label class="form-label">Numero</label>
                            <input type="text" class="form-control" name="compradorDoc" id="compradorDoc" maxlength="12" required>
                        </div>
                        <div class="col-12">
                            <input type="text" class="form-control" name="nombreInvitado" id="nombreInvitado"
                                   maxlength="150" required placeholder="Nombre de quien compra">
                        </div>
                    </div>
                    <div class="form-text">Si el documento ya es de un cliente registrado, se usa su cuenta.</div>
                </div>
                <div class="col-md-3 mb-3">
                    <label class="form-label">Canal</label>
                    <select class="form-select" name="tipoOrigen">
                        <option value="PRESENCIAL">Presencial</option>
                        <option value="WHATSAPP">WhatsApp</option>
                        <option value="TIENDA">Tienda</option>
                        <option value="WEB">Web</option>
                        <option value="DELIVERY">Delivery</option>
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

<script>
    function alternarNombre() {
        const hayCliente = document.getElementById("clienteId").value !== "";
        const input = document.getElementById("nombreInvitado");
        const doc = document.getElementById("compradorDoc");
        document.getElementById("campoNombre").style.display = hayCliente ? "none" : "block";
        input.required = !hayCliente;
        doc.required = !hayCliente;
        if (hayCliente) { input.value = ""; doc.value = ""; }
    }
</script>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
