/*
 * Filas dinamicas de producto/cantidad/precio/subtotal, compartidas por
 * pedido-form.jsp y pos.jsp. Espera que la pagina ya haya definido un
 * array global CATALOGO = [{id, nombre, precio, stock}, ...] antes de
 * cargar este script.
 */

function opcionesProducto(seleccionado) {
    return CATALOGO.map(p =>
        `<option value="${p.id}" data-precio="${p.precio}" ${p.id === seleccionado ? "selected" : ""}>
            ${p.nombre} (stock: ${p.stock})
         </option>`
    ).join("");
}

function agregarLinea() {
    const tbody = document.querySelector("#tablaLineas tbody");
    const fila = document.createElement("tr");
    fila.innerHTML = `
        <td>
            <select name="productoId" class="form-select form-select-sm" onchange="recalcularFila(this)">
                ${opcionesProducto(null)}
            </select>
        </td>
        <td><input type="number" name="cantidad" min="1" value="1" class="form-control form-control-sm" oninput="recalcularFila(this)"></td>
        <td class="precio-unit">0.00</td>
        <td class="subtotal">0.00</td>
        <td><button type="button" class="btn btn-sm btn-outline-danger" onclick="this.closest('tr').remove(); recalcularTotal();">
            <i class="bi bi-trash"></i>
        </button></td>
    `;
    tbody.appendChild(fila);
    recalcularFila(fila.querySelector("select"));
}

function recalcularFila(elemento) {
    const fila = elemento.closest("tr");
    const select = fila.querySelector("select[name='productoId']");
    const cantidad = parseFloat(fila.querySelector("input[name='cantidad']").value) || 0;
    const opcion = select.options[select.selectedIndex];
    const precio = opcion ? parseFloat(opcion.dataset.precio) : 0;
    const subtotal = precio * cantidad;
    fila.querySelector(".precio-unit").textContent = precio.toFixed(2);
    fila.querySelector(".subtotal").textContent = subtotal.toFixed(2);
    recalcularTotal();
}

function recalcularTotal() {
    let total = 0;
    document.querySelectorAll("#tablaLineas .subtotal").forEach(td => total += parseFloat(td.textContent) || 0);
    document.getElementById("totalPedido").textContent = total.toFixed(2);
}

// Al menos una linea por defecto.
agregarLinea();
