package com.svir.jee.bean;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;
import com.svir.jee.model.TipoOrigenPedido;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.Compradores;
import com.svir.jee.util.Textos;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Managed Bean del Punto de Venta: catalogo de productos, carrito en memoria (vista),
 * cobro que registra un pedido PRESENCIAL (PedidoDAO, transaccion JDBC) y ticket.
 */
@Named
@ViewScoped
public class PosBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final PedidoDAO DAO_PEDIDO = new PedidoDAO();
    private static final ProductoDAO DAO_PRODUCTO = new ProductoDAO();
    private static final ClienteDAO DAO_CLIENTE = new ClienteDAO();

    /** Linea del carrito. */
    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer productoId;
        private String nombre;
        private BigDecimal precio;
        private Integer cantidad = 1;

        public Integer getProductoId() { return productoId; }
        public String getNombre() { return nombre; }
        public BigDecimal getPrecio() { return precio; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public BigDecimal getSubtotal() {
            return cantidad == null || cantidad <= 0 ? BigDecimal.ZERO : precio.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private String filtro = "";
    private String filtroAplicado = "";
    private final List<Item> carrito = new ArrayList<>();

    private String tipoComprobante = "BOLETA_SIMPLE";
    private String documento;
    private Integer clienteId;
    private String nombreComprador;
    private String compradorTipoDoc = "DNI";
    private String compradorDoc;

    private boolean mostrandoTicket;
    private Pedido ticket;

    private transient List<Producto> productos;
    private transient List<Cliente> clientes;

    // ---- catalogo ----

    public List<Producto> getProductos() {
        if (productos == null) {
            String f = filtroAplicado.toLowerCase(Locale.ROOT);
            try {
                productos = DAO_PRODUCTO.listarActivos().stream()
                        .filter(p -> f.isEmpty() || p.getNombre().toLowerCase(Locale.ROOT).contains(f))
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                Mensajes.error("No se pudo leer el catálogo de productos.");
                productos = List.of();
            }
        }
        return productos;
    }

    public void buscar() {
        filtroAplicado = filtro == null ? "" : filtro.trim();
        productos = null;
    }

    public void limpiar() {
        filtro = "";
        buscar();
    }

    // ---- carrito ----

    public void agregar(Producto p) {
        for (Item item : carrito) {
            if (item.productoId.equals(p.getId())) {
                item.cantidad = (item.cantidad == null ? 0 : item.cantidad) + 1;
                return;
            }
        }
        Item nuevo = new Item();
        nuevo.productoId = p.getId();
        nuevo.nombre = p.getNombre();
        nuevo.precio = p.getPrecio();
        carrito.add(nuevo);
    }

    public void quitar(Item item) {
        carrito.remove(item);
    }

    public void vaciar() {
        carrito.clear();
    }

    public BigDecimal getTotal() {
        return carrito.stream().map(Item::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---- cobro y ticket ----

    public void cobrar() {
        Pedido pedido = new Pedido();
        pedido.setTipoOrigen(TipoOrigenPedido.PRESENCIAL);

        if (clienteId != null) {
            pedido.setClienteId(clienteId);
        }

        String errorComprobante = PedidoDAO.validarComprobante(tipoComprobante, Textos.limpiar(documento, 20));
        if (errorComprobante != null) {
            FacesContext.getCurrentInstance().addMessage("formPos:documento",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, errorComprobante, null));
            return;
        }

        for (Item item : carrito) {
            if (item.cantidad == null || item.cantidad <= 0) {
                continue;
            }
            DetallePedido linea = new DetallePedido();
            linea.setProductoId(item.productoId);
            linea.setCantidad(item.cantidad);
            linea.setPrecioUnitario(item.precio);
            pedido.getDetalles().add(linea);
        }

        if (pedido.getDetalles().isEmpty()) {
            Mensajes.error("El carrito está vacío.");
            return;
        }

        // Sin cliente elegido: el comprador es un invitado identificado por su documento (unico).
        if (clienteId == null) {
            try {
                Compradores.Comprador comprador = Compradores.resolver(compradorTipoDoc, compradorDoc, nombreComprador, null, true);
                pedido.setClienteId(comprador.clienteId);
                pedido.setInvitadoId(comprador.invitadoId);
                if (comprador.clienteId == null) {
                    pedido.setNombreInvitado(comprador.nombre);
                }
            } catch (Compradores.ValidacionException e) {
                String campo = e.getMessage().contains("nombre") ? "nombreComprador" : "compradorDoc";
                FacesContext.getCurrentInstance().addMessage("formPos:" + campo,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
                return;
            } catch (SQLException e) {
                Mensajes.error("No se pudo registrar al comprador: " + e.getMessage());
                return;
            }
        }

        try {
            Usuario usuario = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
            Pedido creado = DAO_PEDIDO.crear(pedido, usuario.getId());
            DAO_PEDIDO.emitirComprobante(creado.getId(), tipoComprobante, Textos.limpiar(documento, 20));
            ticket = DAO_PEDIDO.buscarPorId(creado.getId()).orElse(creado);
            mostrandoTicket = true;
            carrito.clear();
            documento = null;
            clienteId = null;
            nombreComprador = null;
            compradorDoc = null;
            compradorTipoDoc = "DNI";
            tipoComprobante = "BOLETA_SIMPLE";
            productos = null;
        } catch (SQLException e) {
            Mensajes.error("No se pudo procesar la venta: " + e.getMessage());
        }
    }

    public void nuevaVenta() {
        mostrandoTicket = false;
        ticket = null;
    }

    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }
    public List<Item> getCarrito() { return carrito; }
    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public Integer getClienteId() { return clienteId; }
    public String getCompradorTipoDoc() { return compradorTipoDoc; }
    public void setCompradorTipoDoc(String v) { this.compradorTipoDoc = v; }
    public String getCompradorDoc() { return compradorDoc; }
    public void setCompradorDoc(String v) { this.compradorDoc = v; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getNombreComprador() { return nombreComprador; }
    public void setNombreComprador(String nombreComprador) { this.nombreComprador = nombreComprador; }
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
    public boolean isMostrandoTicket() { return mostrandoTicket; }
    public Pedido getTicket() { return ticket; }
}
