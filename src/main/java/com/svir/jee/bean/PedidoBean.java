package com.svir.jee.bean;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.EstadoPedido;
import com.svir.jee.model.FiltroPedido;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;
import com.svir.jee.model.TipoOrigenPedido;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.Textos;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Managed Bean de Pedidos: listado con filtro/paginacion, detalle, cambio de estado,
 * cancelacion (devuelve stock) y alta de pedidos con varias lineas de producto.
 * La logica transaccional vive en PedidoDAO (JDBC).
 */
@Named
@ViewScoped
public class PedidoBean extends ListadoBase<Pedido> {

    private static final long serialVersionUID = 1L;
    private static final PedidoDAO DAO = new PedidoDAO();
    private static final ProductoDAO DAO_PRODUCTO = new ProductoDAO();
    private static final ClienteDAO DAO_CLIENTE = new ClienteDAO();

    /** Una fila del pedido nuevo (producto + cantidad). */
    public static class Linea implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer productoId;
        private Integer cantidad = 1;

        public Integer getProductoId() { return productoId; }
        public void setProductoId(Integer productoId) { this.productoId = productoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    // filtros avanzados (los "Sel" son lo que se ve en el formulario; el filtro se aplica al pulsar Buscar)
    private String estadoSel = "";
    private String origenSel = "";
    private String tipoClienteSel = "";
    private String comprobanteSel = "";
    private java.time.LocalDate desdeSel;
    private java.time.LocalDate hastaSel;
    private FiltroPedido aplicado = new FiltroPedido();

    // detalle
    private boolean viendo;
    private Pedido detalle;
    private String compTipo = "BOLETA_SIMPLE";
    private String compDocumento;

    // pedido nuevo
    private boolean creando;
    private Integer clienteId;
    private String nombreInvitado;
    private TipoOrigenPedido tipoOrigen = TipoOrigenPedido.PRESENCIAL;
    private String observacion;
    private List<Linea> lineas = new ArrayList<>();

    private transient List<Producto> productos;
    private transient List<Cliente> clientes;

    @Override
    protected List<Pedido> cargar() throws SQLException {
        return DAO.buscar(aplicado, 0, 0);
    }

    @Override
    public void buscar() {
        FiltroPedido f = new FiltroPedido();
        f.setEstado(estadoSel);
        f.setOrigen(origenSel);
        f.setTipoCliente(tipoClienteSel);
        f.setComprobante(comprobanteSel);
        f.setDesde(desdeSel);
        f.setHasta(hastaSel);
        aplicado = f;
        super.buscar();
    }

    @Override
    public void limpiar() {
        estadoSel = "";
        origenSel = "";
        tipoClienteSel = "";
        comprobanteSel = "";
        desdeSel = null;
        hastaSel = null;
        super.limpiar();
    }

    @Override
    protected String textoBusqueda(Pedido p) {
        return p.getId() + " " + texto(p.getClienteNombre()) + " " + p.getEstado() + " " + p.getTipoOrigen()
                + " " + texto(p.getComprobanteNumero()) + " " + texto(p.getObservacion());
    }

    // ---- filtros rapidos (tarjetas del dashboard) y apertura directa del detalle ----

    @Override
    protected void aplicarAtajo(String atajo) {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        switch (atajo) {
            case "hoy" -> {
                desdeSel = hoy;
                hastaSel = hoy;
                estadoSel = "ACTIVOS";
            }
            case "mes" -> {
                desdeSel = hoy.withDayOfMonth(1);
                hastaSel = hoy.withDayOfMonth(hoy.lengthOfMonth());
                estadoSel = "ACTIVOS";
            }
            case "pendientes" -> estadoSel = "ABIERTOS";
            case "listos" -> estadoSel = "LISTO";
            default -> { }
        }
    }

    /** Si la pagina se abre con ?ver=ID (desde "Pedidos recientes" del dashboard) muestra ese detalle. */
    @jakarta.annotation.PostConstruct
    void abrirDesdeEnlace() {
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ver");
        if (id != null && id.matches("\\d{1,9}")) {
            try {
                DAO.buscarPorId(Integer.parseInt(id)).ifPresent(p -> {
                    detalle = p;
                    viendo = true;
                });
            } catch (SQLException e) {
                Mensajes.error("No se pudo leer el detalle del pedido.");
            }
        }
    }

    // ---- reglas de visibilidad de botones (mismas que el panel JSP) ----

    public boolean esTerminal(Pedido p) {
        return p.getEstado() == EstadoPedido.ENTREGADO || p.getEstado() == EstadoPedido.CANCELADO;
    }

    public boolean puedeSalirAReparto(Pedido p) {
        return p.getTipoOrigen() == TipoOrigenPedido.DELIVERY && p.getEstado() == EstadoPedido.LISTO;
    }

    // ---- detalle ----

    public void ver(Pedido p) {
        try {
            Formularios.resetear("formDetalle");
            compTipo = "BOLETA_SIMPLE";
            compDocumento = null;
            detalle = DAO.buscarPorId(p.getId()).orElse(p);
            viendo = true;
        } catch (SQLException e) {
            Mensajes.error("No se pudo leer el detalle del pedido.");
        }
    }

    public void cerrarDetalle() {
        viendo = false;
        detalle = null;
    }

    // ---- comprobante (boleta / factura) ----

    public boolean isPuedeEmitirComprobante() {
        return detalle != null && !detalle.isTieneComprobante() && detalle.getEstado() != EstadoPedido.CANCELADO;
    }

    public void emitirComprobante() {
        String documento = Textos.limpiar(compDocumento, 20);
        String error = PedidoDAO.validarComprobante(compTipo, documento);
        if (error != null) {
            FacesContext.getCurrentInstance().addMessage("formDetalle:compDocumento",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            return;
        }
        try {
            String numero = DAO.emitirComprobante(detalle.getId(), compTipo, documento);
            detalle = DAO.buscarPorId(detalle.getId()).orElse(detalle);
            Mensajes.info("Comprobante " + numero + " emitido.");
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo emitir el comprobante: " + e.getMessage());
        }
    }

    // ---- cambios de estado ----

    public void marcarEnCamino(Pedido p) {
        cambiarEstado(p, EstadoPedido.EN_CAMINO, "marcado como en camino");
    }

    public void entregar(Pedido p) {
        cambiarEstado(p, EstadoPedido.ENTREGADO, "entregado");
    }

    private void cambiarEstado(Pedido p, EstadoPedido estado, String texto) {
        try {
            DAO.cambiarEstado(p.getId(), estado);
            Mensajes.info("Pedido #" + p.getId() + " " + texto + ".");
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo cambiar el estado: " + e.getMessage());
        }
    }

    public void cancelarPedido(Pedido p) {
        try {
            DAO.cancelar(p.getId(), usuarioActual().getId());
            Mensajes.info("Pedido #" + p.getId() + " cancelado. El stock atendido fue devuelto al inventario.");
            viendo = false;
            detalle = null;
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo cancelar el pedido: " + e.getMessage());
        }
    }

    // ---- pedido nuevo ----

    public void nuevo() {
        Formularios.resetear("formPedido");
        clienteId = null;
        nombreInvitado = null;
        tipoOrigen = TipoOrigenPedido.PRESENCIAL;
        observacion = null;
        lineas = new ArrayList<>();
        lineas.add(new Linea());
        creando = true;
    }

    public void agregarLinea() {
        lineas.add(new Linea());
    }

    public void quitarLinea(Linea l) {
        lineas.remove(l);
        if (lineas.isEmpty()) {
            lineas.add(new Linea());
        }
    }

    public void cancelarNuevo() {
        Formularios.resetear("formPedido");
        creando = false;
    }

    public void guardar() {
        Map<Integer, Producto> porId = new HashMap<>();
        for (Producto p : getProductos()) {
            porId.put(p.getId(), p);
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(clienteId);
        if (clienteId == null) {
            String nombre = Textos.limpiar(nombreInvitado, 150);
            if (nombre.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("formPedido:nombreInvitado",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "El nombre es obligatorio.", null));
                return;
            }
            pedido.setNombreInvitado(nombre);
        }
        pedido.setTipoOrigen(tipoOrigen);
        pedido.setObservacion(observacion);
        for (Linea l : lineas) {
            Producto producto = l.getProductoId() == null ? null : porId.get(l.getProductoId());
            if (producto == null || l.getCantidad() == null || l.getCantidad() <= 0) {
                continue;
            }
            DetallePedido d = new DetallePedido();
            d.setProductoId(producto.getId());
            d.setCantidad(l.getCantidad());
            d.setPrecioUnitario(producto.getPrecio());
            pedido.getDetalles().add(d);
        }

        if (pedido.getDetalles().isEmpty()) {
            Mensajes.error("Debes agregar al menos un producto válido al pedido.");
            return;
        }

        try {
            Pedido creado = DAO.crear(pedido, usuarioActual().getId());
            Mensajes.info("Pedido #" + creado.getId() + " registrado correctamente.");
            Formularios.resetear("formPedido");
            creando = false;
            recargar();
            detalle = DAO.buscarPorId(creado.getId()).orElse(creado);
            viendo = true;
        } catch (SQLException e) {
            Mensajes.error("No se pudo registrar el pedido: " + e.getMessage());
        }
    }

    /** Precio unitario actual del producto elegido en la linea (null si aun no eligio uno). */
    public BigDecimal precioDe(Linea l) {
        if (l.getProductoId() == null) {
            return null;
        }
        for (Producto p : getProductos()) {
            if (p.getId().equals(l.getProductoId())) {
                return p.getPrecio();
            }
        }
        return null;
    }

    /** Subtotal de la linea = precio unitario x cantidad (0 si falta producto o cantidad valida). */
    public BigDecimal subtotalDe(Linea l) {
        BigDecimal precio = precioDe(l);
        if (precio == null || l.getCantidad() == null || l.getCantidad() <= 0) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(l.getCantidad()));
    }

    /** Total estimado del pedido nuevo segun los precios actuales. */
    public BigDecimal getTotalNuevo() {
        BigDecimal total = BigDecimal.ZERO;
        for (Linea l : lineas) {
            total = total.add(subtotalDe(l));
        }
        return total;
    }

    public List<Producto> getProductos() {
        if (productos == null) {
            try {
                productos = DAO_PRODUCTO.listarActivos();
            } catch (SQLException e) {
                Mensajes.error("No se pudo leer la lista de productos.");
                productos = List.of();
            }
        }
        return productos;
    }

    public List<Cliente> getClientes() {
        if (clientes == null) {
            try {
                clientes = DAO_CLIENTE.listarActivos();
            } catch (SQLException e) {
                Mensajes.error("No se pudo leer la lista de clientes.");
                clientes = List.of();
            }
        }
        return clientes;
    }

    private static Usuario usuarioActual() {
        return (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
    }

    public TipoOrigenPedido[] getOrigenes() { return TipoOrigenPedido.values(); }

    public boolean isViendo() { return viendo; }
    public String getEstadoSel() { return estadoSel; }
    public void setEstadoSel(String v) { this.estadoSel = v; }
    public String getOrigenSel() { return origenSel; }
    public void setOrigenSel(String v) { this.origenSel = v; }
    public String getTipoClienteSel() { return tipoClienteSel; }
    public void setTipoClienteSel(String v) { this.tipoClienteSel = v; }
    public String getComprobanteSel() { return comprobanteSel; }
    public void setComprobanteSel(String v) { this.comprobanteSel = v; }
    public java.time.LocalDate getDesdeSel() { return desdeSel; }
    public void setDesdeSel(java.time.LocalDate v) { this.desdeSel = v; }
    public java.time.LocalDate getHastaSel() { return hastaSel; }
    public void setHastaSel(java.time.LocalDate v) { this.hastaSel = v; }
    public String getCompTipo() { return compTipo; }
    public void setCompTipo(String compTipo) { this.compTipo = compTipo; }
    public String getCompDocumento() { return compDocumento; }
    public void setCompDocumento(String compDocumento) { this.compDocumento = compDocumento; }
    public Pedido getDetalle() { return detalle; }
    public boolean isCreando() { return creando; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getNombreInvitado() { return nombreInvitado; }
    public void setNombreInvitado(String nombreInvitado) { this.nombreInvitado = nombreInvitado; }
    public TipoOrigenPedido getTipoOrigen() { return tipoOrigen; }
    public void setTipoOrigen(TipoOrigenPedido tipoOrigen) { this.tipoOrigen = tipoOrigen; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public List<Linea> getLineas() { return lineas; }
}
