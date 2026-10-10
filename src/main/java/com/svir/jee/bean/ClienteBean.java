package com.svir.jee.bean;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.model.Cliente;

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

    private boolean editando;
    private Cliente cliente;

    @Override
    protected List<Cliente> cargar() throws SQLException {
        return DAO.listarTodos();
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
            if (esNuevo) {
                DAO.crear(cliente);
            } else {
                DAO.actualizar(cliente);
            }
            Mensajes.info("Cliente \"" + cliente.getNombre() + "\" " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
            editando = false;
            cliente = null;
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo guardar el cliente (revisa que el DNI no esté repetido).");
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
