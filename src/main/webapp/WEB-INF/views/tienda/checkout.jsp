<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="com.svir.jee.model.Cliente" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.math.BigDecimal" %>
<%
    request.setAttribute("tituloPagina", "Checkout");
    Map<Producto, Integer> lineas = (Map<Producto, Integer>) request.getAttribute("lineas");
    BigDecimal total = (BigDecimal) request.getAttribute("total");
    Cliente clienteSesion = (Cliente) session.getAttribute("cliente");
%>
<%@ include file="/WEB-INF/views/tienda/common/tienda-header.jspf" %>

<h3 class="mb-4">Confirmar pedido</h3>

<div class="row">
    <div class="col-md-7">
        <div class="card mb-3">
            <div class="card-body">
                <h6>Resumen</h6>
                <ul class="list-unstyled mb-2">
                <%
                    for (Map.Entry<Producto, Integer> e : lineas.entrySet()) {
                %>
                    <li><%= e.getValue() %> x <%= e.getKey().getNombre() %> - S/ <%= e.getKey().getPrecio().multiply(BigDecimal.valueOf(e.getValue())) %></li>
                <% } %>
                </ul>
                <strong>Total: S/ <%= total %></strong>
            </div>
        </div>
    </div>

    <div class="col-md-5">
        <div class="card">
            <div class="card-body">
                <form method="post" action="<%= ctx %>/checkout" id="formCheckout">
                    <% if (clienteSesion == null) { %>
                    <div class="mb-3">
                        <label class="form-label">Tu nombre</label>
                        <input type="text" class="form-control" name="nombre" placeholder="Nombre para el pedido" required>
                    </div>
                    <% } %>

                    <div class="mb-3">
                        <label class="form-label d-block">Entrega</label>
                        <div class="btn-group w-100" role="group">
                            <input type="radio" class="btn-check" name="modoEntrega" id="modoTienda" value="tienda" checked onclick="toggleDelivery(false)">
                            <label class="btn btn-outline-secondary" for="modoTienda">Recojo en tienda</label>
                            <input type="radio" class="btn-check" name="modoEntrega" id="modoDelivery" value="delivery" onclick="toggleDelivery(true)">
                            <label class="btn btn-outline-secondary" for="modoDelivery">Delivery</label>
                        </div>
                    </div>

                    <div id="camposDelivery" style="display:none;">
                        <div class="mb-2">
                            <label class="form-label">Direccion</label>
                            <input type="text" class="form-control" name="direccion" id="direccion">
                        </div>
                        <div class="mb-2">
                            <label class="form-label">Telefono</label>
                            <input type="text" class="form-control" name="telefono"
                                   value="<%= clienteSesion != null && clienteSesion.getTelefono() != null ? clienteSesion.getTelefono() : "" %>">
                        </div>
                        <div class="mb-2">
                            <label class="form-label">Referencia (opcional)</label>
                            <input type="text" class="form-control" name="referencia">
                        </div>
                        <input type="hidden" name="gps" id="gps">
                        <button type="button" class="btn btn-sm btn-outline-secondary mb-3" onclick="marcarUbicacion()">
                            <i class="bi bi-geo-alt"></i> Marcar mi ubicacion actual
                        </button>
                    </div>

                    <button type="submit" class="btn btn-amber w-100">Confirmar pedido</button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    function toggleDelivery(mostrar) {
        document.getElementById("camposDelivery").style.display = mostrar ? "block" : "none";
        document.getElementById("direccion").required = mostrar;
    }
    function marcarUbicacion() {
        if (!navigator.geolocation) {
            alert("Tu navegador no soporta geolocalizacion.");
            return;
        }
        navigator.geolocation.getCurrentPosition(function (pos) {
            document.getElementById("gps").value = pos.coords.latitude + "," + pos.coords.longitude;
            alert("Ubicacion marcada correctamente.");
        }, function () {
            alert("No se pudo obtener tu ubicacion.");
        });
    }
</script>

<%@ include file="/WEB-INF/views/tienda/common/tienda-footer.jspf" %>
