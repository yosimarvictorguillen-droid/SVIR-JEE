package com.svir.jee.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Criterios de busqueda de pedidos (pantalla Pedidos). Los textos vacios o null significan "sin filtro".
 * <ul>
 *   <li>texto: numero de pedido, nombre del comprador, telefono o numero de comprobante</li>
 *   <li>estado: un estado, o ACTIVOS (todos menos cancelados) / ABIERTOS (pendiente + en preparacion)</li>
 *   <li>origen: un TipoOrigenPedido</li>
 *   <li>tipoCliente: Cliente (registrado) o Invitado</li>
 *   <li>comprobante: CON o SIN comprobante emitido</li>
 * </ul>
 */
public class FiltroPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    private String texto = "";
    private String estado = "";
    private String origen = "";
    private String tipoCliente = "";
    private String comprobante = "";
    private LocalDate desde;
    private LocalDate hasta;

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto == null ? "" : texto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado == null ? "" : estado; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen == null ? "" : origen; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente == null ? "" : tipoCliente; }
    public String getComprobante() { return comprobante; }
    public void setComprobante(String comprobante) { this.comprobante = comprobante == null ? "" : comprobante; }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
}
