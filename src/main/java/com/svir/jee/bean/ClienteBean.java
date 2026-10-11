package com.svir.jee.bean;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.InvitadoDAO;
import com.svir.jee.util.Documentos;
import com.svir.jee.model.Cliente;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.sql.SQLException;
import java.util.List;

/** Managed Bean del modulo Clientes: listado con filtro/paginacion y alta/edicion en modal. */
@Named
@ViewScoped
public class ClienteBean extends ListadoBase<Cliente> {

    private static final long serialVersionUID = 1L;
    private static final ClienteDAO DAO = new ClienteDAO();
    private static final InvitadoDAO DAO_INVITADO = new InvitadoDAO();

    private boolean editando;
    private Cliente cliente;

    @Override
    protected List<Cliente> cargar() throws SQLException {
        return DAO.listarTodos();
    }

    @Override
    protected boolean cumpleFiltros(Cliente c, java.util.Map<String, String> f) {
        String estado = valor(f, "estado");
        return estado.isEmpty() || c.isActivo() == "ACTIVO".equals(estado);
    }

    @Override
    protected String textoBusqueda(Cliente c) {
        return texto(c.getNombre()) + " " + texto(c.getDni()) + " " + texto(c.getRuc()) + " " + texto(c.getTelefono());
    }

    public void nuevo() {
        Formularios.resetear("formCliente");
        cliente = new Cliente();
        cliente.setActivo(true);
        editando = true;
    }

    public void editar(Cliente c) {
        Formularios.resetear("formCliente");
        Cliente copia = new Cliente();
        copia.setId(c.getId());
        copia.setNombre(c.getNombre());
        copia.setTipoDocumento(c.getTipoDocumento());
        copia.setDni(c.getDni());
        copia.setRuc(c.getRuc());
        copia.setTelefono(c.getTelefono());
        copia.setDireccion(c.getDireccion());
        copia.setEmail(c.getEmail());
        copia.setActivo(c.isActivo());
        cliente = copia;
        editando = true;
    }

    public void cancelar() {
        editando = false;
        cliente = null;
    }

    public void guardar() {
        try {
            boolean esNuevo = cliente.getId() == null;
            // El documento es obligatorio y unico en todo el sistema (clientes e invitados).
            cliente.setDni(Documentos.normalizar(cliente.getDni()));
            String error = Documentos.validar(cliente.getTipoDocumento(), cliente.getDni());
            if (error == null && DAO.existeDocumento(cliente.getDni(), cliente.getId())) {
                error = "Ese documento ya pertenece a otro cliente.";
            }
            if (error == null && !esNuevo
                    && DAO_INVITADO.buscarPorDocumento(cliente.getDni()).filter(i -> !i.isConvertido()).isPresent()) {
                error = "Ese documento pertenece a un invitado con pedidos. Regístralo como cliente nuevo para vincular sus pedidos.";
            }
            if (error != null) {
                FacesContext.getCurrentInstance().addMessage("formCliente:dni",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
                return;
            }
            int vinculados = 0;
            if (esNuevo) {
                vinculados = DAO.altaConVinculo(cliente, false, "PERSONAL");
            } else {
                DAO.actualizar(cliente);
            }
            Mensajes.info("Cliente \"" + cliente.getNombre() + "\" " + (esNuevo ? "creado" : "actualizado") + " correctamente."
                    + (vinculados > 0 ? " Se vincularon " + vinculados + " pedido(s) de cuando compraba como invitado." : ""));
            editando = false;
            cliente = null;
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo guardar el cliente.");
        }
    }

    public void alternarActivo(Cliente c) {
        try {
            DAO.cambiarActivo(c.getId(), !c.isActivo());
            Mensajes.info("Cliente \"" + c.getNombre() + "\" " + (c.isActivo() ? "desactivado" : "activado") + ".");
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo cambiar el estado del cliente.");
        }
    }

    public boolean isEditando() { return editando; }
    public Cliente getCliente() { return cliente; }
}
