package com.svir.jee.bean;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.dao.ProduccionDAO;
import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.EstadoProduccion;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Produccion;
import com.svir.jee.model.ProduccionDetalle;
import com.svir.jee.model.Producto;
import com.svir.jee.model.Usuario;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Managed Bean de Produccion: ordenes para stock o por pedido, y su ciclo
 * (terminar / cancelar). El consumo de ingredientes y el ajuste de stock son
 * transacciones JDBC dentro de ProduccionDAO.
 */
@Named
@ViewScoped
public class ProduccionBean extends ListadoBase<Produccion> {

    private static final long serialVersionUID = 1L;
    private static final ProduccionDAO DAO = new ProduccionDAO();
    private static final ProductoDAO DAO_PRODUCTO = new ProductoDAO();
    private static final PedidoDAO DAO_PEDIDO = new PedidoDAO();

    /** Fila editable al terminar una orden: cuanto se produjo realmente de cada producto. */
    public static class Fila implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer detalleId;
        private String producto;
        private int planificada;
        private Integer producida;

        public Integer getDetalleId() { return detalleId; }
        public String getProducto() { return producto; }
        public int getPlanificada() { return planificada; }
        public Integer getProducida() { return producida; }
        public void setProducida(Integer producida) { this.producida = producida; }
    }

    /** Fila de la orden nueva para stock. */
    public static class LineaStock implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer productoId;
        private Integer cantidad = 1;

        public Integer getProductoId() { return productoId; }
        public void setProductoId(Integer productoId) { this.productoId = productoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    // detalle de una orden
    private boolean viendo;
    private Produccion detalle;
    private List<Fila> filas = new ArrayList<>();

    // orden nueva para stock
    private boolean creando;
    private List<LineaStock> lineas = new ArrayList<>();

    // ordenes por pedido
    private boolean porPedido;

    private transient List<Producto> productos;
    private transient List<Pedido> pedidosConFaltante;

    @Override
    protected List<Produccion> cargar() throws SQLException {
        return DAO.listarTodos();
    }

    @Override
    protected boolean cumpleFiltros(Produccion p, java.util.Map<String, String> f) {
        String tipo = valor(f, "tipo");
        String estado = valor(f, "estado");
        if (!tipo.isEmpty() && !p.getTipo().name().equals(tipo)) {
            return false;
        }
        if (!estado.isEmpty() && !p.getEstado().name().equals(estado)) {
            return false;
        }
        java.time.LocalDate dia = p.getCreatedAt() == null ? null : p.getCreatedAt().toLocalDate();
        try {
            if (!valor(f, "desde").isEmpty() && (dia == null || dia.isBefore(java.time.LocalDate.parse(valor(f, "desde"))))) {
                return false;
            }
            if (!valor(f, "hasta").isEmpty() && (dia == null || dia.isAfter(java.time.LocalDate.parse(valor(f, "hasta"))))) {
                return false;
            }
        } catch (java.time.format.DateTimeParseException e) {
            return true; // fecha mal escrita: se ignora ese filtro
        }
        return true;
    }

    @Override
    protected String textoBusqueda(Produccion p) {
        return p.getId() + " " + p.getTipo() + " " + p.getEstado() + " " + texto(p.getUsuarioNombre());
    }

    public boolean puedeGestionar(Produccion p) {
        return p != null && (p.getEstado() == EstadoProduccion.PENDIENTE || p.getEstado() == EstadoProduccion.EN_PROCESO);
    }

    // ---- detalle ----

    public void ver(Produccion p) {
        abrirDetalle(p.getId());
    }

    private void abrirDetalle(int id) {
        try {
            detalle = DAO.buscarPorId(id).orElse(null);
            if (detalle == null) {
                Mensajes.error("Orden de producción no encontrada.");
                return;
            }
            Formularios.resetear("formDetalle");
            filas = new ArrayList<>();
            for (ProduccionDetalle d : detalle.getDetalles()) {
                Fila f = new Fila();
                f.detalleId = d.getId();
                f.producto = d.getProductoNombre();
                f.planificada = d.getCantidadPlanificada();
                f.producida = d.getCantidadProducida() > 0 ? d.getCantidadProducida() : d.getCantidadPlanificada();
                filas.add(f);
            }
            viendo = true;
        } catch (SQLException e) {
            Mensajes.error("No se pudo leer la orden de producción.");
        }
    }

    public void cerrarDetalle() {
        Formularios.resetear("formDetalle");
        viendo = false;
        detalle = null;
    }

    public void terminar() {
        Map<Integer, Integer> cantidades = new HashMap<>();
        for (Fila f : filas) {
            cantidades.put(f.detalleId, f.producida == null ? 0 : f.producida);
        }
        try {
            DAO.terminar(detalle.getId(), cantidades, usuarioActual().getId());
            Mensajes.info("Orden #" + detalle.getId() + " terminada: el stock de productos fue actualizado.");
            int id = detalle.getId();
            recargar();
            abrirDetalle(id);
        } catch (SQLException e) {
            Mensajes.error("No se pudo terminar la orden: " + e.getMessage());
        }
    }

    public void cancelarOrden() {
        try {
            DAO.cancelar(detalle.getId(), usuarioActual().getId());
            Mensajes.info("Orden #" + detalle.getId() + " cancelada: los ingredientes fueron devueltos.");
            cerrarDetalle();
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo cancelar la orden: " + e.getMessage());
        }
    }

    // ---- orden nueva para stock ----

    public void nueva() {
        Formularios.resetear("formNueva");
        lineas = new ArrayList<>();
        lineas.add(new LineaStock());
        creando = true;
    }

    public void agregarLinea() {
        lineas.add(new LineaStock());
    }

    public void quitarLinea(LineaStock l) {
        lineas.remove(l);
        if (lineas.isEmpty()) {
            lineas.add(new LineaStock());
        }
    }

    public void cancelarNueva() {
        Formularios.resetear("formNueva");
        creando = false;
    }

    public void guardarNueva() {
        List<ProduccionDetalle> detalles = new ArrayList<>();
        for (LineaStock l : lineas) {
            if (l.getProductoId() == null || l.getCantidad() == null || l.getCantidad() <= 0) {
                continue;
            }
            ProduccionDetalle pd = new ProduccionDetalle();
            pd.setProductoId(l.getProductoId());
            pd.setCantidadPlanificada(l.getCantidad());
            detalles.add(pd);
        }
        if (detalles.isEmpty()) {
            Mensajes.error("Debes indicar al menos un producto con cantidad a producir.");
            return;
        }
        try {
            Produccion creada = DAO.crearParaStock(detalles, usuarioActual().getId());
            Mensajes.info("Orden #" + creada.getId() + " creada: los ingredientes fueron descontados.");
            Formularios.resetear("formNueva");
            creando = false;
            recargar();
            abrirDetalle(creada.getId());
        } catch (SQLException e) {
            Mensajes.error("No se pudo crear la orden: " + e.getMessage());
        }
    }

    // ---- ordenes por pedido ----

    public void abrirPorPedido() {
        pedidosConFaltante = null;
        porPedido = true;
    }

    public void cerrarPorPedido() {
        porPedido = false;
    }

    public void generarPorPedido(Pedido pedido) {
        try {
            Produccion creada = DAO.crearPorPedido(pedido.getId(), usuarioActual().getId());
            Mensajes.info("Orden #" + creada.getId() + " creada para el pedido #" + pedido.getId() + ".");
            porPedido = false;
            recargar();
            abrirDetalle(creada.getId());
        } catch (SQLException e) {
            Mensajes.error("No se pudo generar la orden: " + e.getMessage());
        }
    }

    public List<Pedido> getPedidosConFaltante() {
        if (pedidosConFaltante == null) {
            try {
                pedidosConFaltante = DAO_PEDIDO.listarConFaltante();
            } catch (SQLException e) {
                Mensajes.error("No se pudieron leer los pedidos con faltante.");
                pedidosConFaltante = List.of();
            }
        }
        return pedidosConFaltante;
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

    private static Usuario usuarioActual() {
        return (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
    }

    public boolean isViendo() { return viendo; }
    public Produccion getDetalle() { return detalle; }
    public List<Fila> getFilasDetalle() { return filas; }
    public boolean isCreando() { return creando; }
    public List<LineaStock> getLineas() { return lineas; }
    public boolean isPorPedido() { return porPedido; }
}
