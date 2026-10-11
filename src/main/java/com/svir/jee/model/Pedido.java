package com.svir.jee.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido implements java.io.Serializable {

    private Integer id;
    private Integer clienteId;
    private String clienteNombre; // solo para mostrar en pantalla (join)
    private String nombreInvitado; // nombre que escribio quien compra sin cuenta
    private Integer usuarioId;
    private String usuarioNombre; // solo para mostrar en pantalla (join)
    private BigDecimal total;
    private EstadoPedido estado;
    private TipoOrigenPedido tipoOrigen;
    private String observacion;
    private String comprobanteTipo; // BOLETA_SIMPLE, BOLETA_DNI o FACTURA (null = sin comprobante)
    private String comprobanteNumero; // ej. B001-00000012
    private String comprobanteDocumento; // DNI o RUC del comprobante
    private LocalDateTime comprobanteFecha;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    /** Nombre de quien compro: el del cliente con cuenta o, si no tiene cuenta, el que escribio como invitado. */
    public String getClienteNombre() {
        return clienteNombre != null ? clienteNombre : nombreInvitado;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getNombreInvitado() {
        return nombreInvitado;
    }

    public void setNombreInvitado(String nombreInvitado) {
        this.nombreInvitado = nombreInvitado;
    }

    /** "Cliente" si esta registrado en el sistema; "Invitado" si no lo esta (con o sin nombre escrito). */
    public String getTipoComprador() {
        return clienteId != null ? "Cliente" : "Invitado";
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public TipoOrigenPedido getTipoOrigen() {
        return tipoOrigen;
    }

    public void setTipoOrigen(TipoOrigenPedido tipoOrigen) {
        this.tipoOrigen = tipoOrigen;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getComprobanteTipo() {
        return comprobanteTipo;
    }

    public void setComprobanteTipo(String comprobanteTipo) {
        this.comprobanteTipo = comprobanteTipo;
    }

    public String getComprobanteNumero() {
        return comprobanteNumero;
    }

    public void setComprobanteNumero(String comprobanteNumero) {
        this.comprobanteNumero = comprobanteNumero;
    }

    public String getComprobanteDocumento() {
        return comprobanteDocumento;
    }

    public void setComprobanteDocumento(String comprobanteDocumento) {
        this.comprobanteDocumento = comprobanteDocumento;
    }

    public LocalDateTime getComprobanteFecha() {
        return comprobanteFecha;
    }

    public void setComprobanteFecha(LocalDateTime comprobanteFecha) {
        this.comprobanteFecha = comprobanteFecha;
    }

    public boolean isTieneComprobante() {
        return comprobanteNumero != null;
    }

    /** "Factura" o "Boleta" segun el comprobante emitido; vacio si no tiene. */
    public String getComprobanteNombre() {
        if (comprobanteTipo == null) {
            return "";
        }
        return "FACTURA".equals(comprobanteTipo) ? "Factura" : "Boleta";
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
