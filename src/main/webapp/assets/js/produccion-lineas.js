/*
 * Filas dinamicas de producto/cantidad para la orden de produccion "para
 * stock". Espera un array global CATALOGO = [{id, nombre}, ...] definido
 * por la pagina antes de cargar este script.
 */

function opcionesProducto() {
    return CATALOGO.map(p => `<option value="${p.id}">${p.nombre}</option>`).join("");
}

function agregarLinea() {
    const tbody = document.querySelector("#tablaLineas tbody");
    const fila = document.createElement("tr");
    fila.innerHTML = `
        <td><select name="productoId" class="form-select form-select-sm">${opcionesProducto()}</select></td>
        <td><input type="number" name="cantidad" min="1" value="1" class="form-control form-control-sm"></td>
        <td><button type="button" class="btn btn-sm btn-outline-danger" onclick="this.closest('tr').remove();">
            <i class="bi bi-trash"></i></button></td>
    `;
    tbody.appendChild(fila);
}

agregarLinea();
