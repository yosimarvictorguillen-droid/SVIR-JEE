package com.svir.jee.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprador sin cuenta, identificado por su documento (unico). Cuando se registra como cliente su estado pasa a
 * CONVERTIDO y se conserva el registro para la trazabilidad (cuando se convirtio y cuantos pedidos se vincularon).
 */
public class Invitado implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String tipoDocumento = "DNI";
    private String documento;
    private String nombre;
    private String telefono;
    private String estado = "ACTIVO"; // ACTIVO o CONVERTIDO
    private Integer clienteId;
    private String clienteNombre;
    private LocalDateTime fechaConversion;
    private LocalDateTime createdAt;

    // resumen (solo en el listado)
    private int totalPedidos;
    private BigDecimal totalComprado = BigDecimal.ZERO;
    private LocalDateTime ultimoPedido;
    private Integer pedidosVinculados;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public LocalDateTime getFechaConversion() { return fechaConversion; }
    public void setFechaConversion(LocalDateTime fechaConversion) { this.fechaConversion = fechaConversion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public int getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(int totalPedidos) { this.totalPedidos = totalPedidos; }
    public BigDecimal getTotalComprado() { return totalComprado; }
    public void setTotalComprado(BigDecimal totalComprado) { this.totalComprado = totalComprado; }
    public LocalDateTime getUltimoPedido() { return ultimoPedido; }
    public void setUltimoPedido(LocalDateTime ultimoPedido) { this.ultimoPedido = ultimoPedido; }
    public Integer getPedidosVinculados() { return pedidosVinculados; }
    public void setPedidosVinculados(Integer pedidosVinculados) { this.pedidosVinculados = pedidosVinculados; }

    public boolean isConvertido() {
        return "CONVERTIDO".equals(estado);
    }
}
