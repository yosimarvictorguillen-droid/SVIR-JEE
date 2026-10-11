package com.svir.jee.bean;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Producto;
import com.svir.jee.model.TipoOrigenPedido;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.Textos;

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
    private String razonSocial;

    private boolean mostrandoTicket;
    private Pedido ticket;

    private transient List<Producto> productos;

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

        StringBuilder obs = new StringBuilder("Comprobante: ").append(tipoComprobante);
        if (documento != null && !documento.isBlank()) {
            obs.append(" | Doc: ").append(documento.trim());
        }
        if (razonSocial != null && !razonSocial.isBlank()) {
            obs.append(" | Cliente: ").append(razonSocial.trim());
        }
        pedido.setObservacion(obs.toString());
        String nombreComprador = Textos.limpiar(razonSocial, 150);
        if (!nombreComprador.isEmpty()) {
            pedido.setNombreInvitado(nombreComprador);
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

        try {
            Usuario usuario = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
            Pedido creado = DAO_PEDIDO.crear(pedido, usuario.getId());
            ticket = DAO_PEDIDO.buscarPorId(creado.getId()).orElse(creado);
            mostrandoTicket = true;
            carrito.clear();
            documento = null;
            razonSocial = null;
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
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public boolean isMostrandoTicket() { return mostrandoTicket; }
    public Pedido getTicket() { return ticket; }
}
