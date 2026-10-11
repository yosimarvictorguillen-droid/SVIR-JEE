package com.svir.jee.bean;

import com.svir.jee.dao.InvitadoDAO;
import com.svir.jee.model.Invitado;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Managed Bean de Invitados (solo lectura): compradores sin cuenta identificados por su documento, con su resumen
 * de compras y la trazabilidad de los que ya pasaron a ser clientes.
 */
@Named
@ViewScoped
public class InvitadoBean extends ListadoBase<Invitado> {

    private static final long serialVersionUID = 1L;
    private static final InvitadoDAO DAO = new InvitadoDAO();

    @Override
    protected List<Invitado> cargar() throws SQLException {
        return DAO.listarConResumen();
    }

    /** El personal confirma que los pedidos antiguos de este nombre son del cliente sugerido. */
    public void vincular(Invitado i) {
        try {
            int n = DAO.vincularAntiguo(i.getNombre(), i.getClienteSugeridoId());
            Mensajes.info("Se vincularon " + n + " pedido(s) con el cliente " + i.getClienteSugeridoNombre() + ".");
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo vincular: " + e.getMessage());
        }
    }

    @Override
    protected boolean cumpleFiltros(Invitado i, Map<String, String> f) {
        String estado = valor(f, "estado");
        return estado.isEmpty() || i.getEstado().equals(estado);
    }

    @Override
    protected String textoBusqueda(Invitado i) {
        return texto(i.getNombre()) + " " + texto(i.getDocumento()) + " " + texto(i.getTelefono());
    }
}
