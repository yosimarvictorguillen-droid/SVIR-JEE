package com.svir.jee.bean;

import com.svir.jee.dao.PedidoDAO;
import com.svir.jee.model.EstadoPedido;
import com.svir.jee.model.Pedido;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Managed Bean del panel de reparto: pedidos delivery agrupados por estado. */
@Named
@ViewScoped
public class RepartidorBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final PedidoDAO DAO = new PedidoDAO();

    private transient List<Pedido> delivery;

    private List<Pedido> delivery() {
        if (delivery == null) {
            try {
                delivery = DAO.listarDelivery();
            } catch (SQLException e) {
                Mensajes.error("No se pudieron leer los pedidos de delivery.");
                delivery = List.of();
            }
        }
        return delivery;
    }

    private List<Pedido> porEstado(EstadoPedido estado) {
        return delivery().stream().filter(p -> p.getEstado() == estado).collect(Collectors.toList());
    }

    public List<Pedido> getParaRecoger() { return porEstado(EstadoPedido.LISTO); }
    public List<Pedido> getEnCamino() { return porEstado(EstadoPedido.EN_CAMINO); }
    public List<Pedido> getEntregados() { return porEstado(EstadoPedido.ENTREGADO); }

    /** La observacion del pedido guarda datos separados por "|" (cliente, direccion, telefono...). */
    public List<String> partes(Pedido pedido) {
        String obs = pedido.getObservacion() == null ? "" : pedido.getObservacion();
        return Arrays.stream(obs.split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    public void marcarEnCamino(Pedido pedido) {
        cambiar(pedido, EstadoPedido.EN_CAMINO, "en camino");
    }

    public void marcarEntregado(Pedido pedido) {
        cambiar(pedido, EstadoPedido.ENTREGADO, "entregado");
    }

    private void cambiar(Pedido pedido, EstadoPedido estado, String texto) {
        try {
            DAO.cambiarEstado(pedido.getId(), estado);
            Mensajes.info("Pedido #" + pedido.getId() + " marcado como " + texto + ".");
            delivery = null;
        } catch (SQLException e) {
            Mensajes.error("No se pudo actualizar el pedido.");
        }
    }
}
