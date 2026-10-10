package com.svir.jee.bean;

import com.svir.jee.dao.UsuarioDAO;
import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.PasswordUtil;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.sql.SQLException;
import java.util.List;

/** Managed Bean del modulo Usuarios (solo ADMIN): alta/edicion con rol y contraseña cifrada con BCrypt. */
@Named
@ViewScoped
public class UsuarioBean extends ListadoBase<Usuario> {

    private static final long serialVersionUID = 1L;
    private static final UsuarioDAO DAO = new UsuarioDAO();

    private boolean editando;
    private Usuario usuario;
    private String password;

    @Override
    protected List<Usuario> cargar() throws SQLException {
        return DAO.listarTodos();
    }

    @Override
    protected String textoBusqueda(Usuario u) {
        return texto(u.getNombre()) + " " + texto(u.getEmail()) + " " + u.getRol();
    }

    public void nuevo() {
        Formularios.resetear("formUsuario");
        usuario = new Usuario();
        usuario.setRol(RolUsuario.VENTAS);
        usuario.setActivo(true);
        password = null;
        editando = true;
    }

    public void editar(Usuario u) {
        Formularios.resetear("formUsuario");
        Usuario copia = new Usuario();
        copia.setId(u.getId());
        copia.setNombre(u.getNombre());
        copia.setEmail(u.getEmail());
        copia.setRol(u.getRol());
        copia.setTelefono(u.getTelefono());
        copia.setActivo(u.isActivo());
        usuario = copia;
        password = null;
        editando = true;
    }

    public void cancelar() {
        editando = false;
        usuario = null;
        password = null;
    }

    public void guardar() {
        try {
            boolean esNuevo = usuario.getId() == null;
            if (esNuevo) {
                usuario.setPasswordHash(PasswordUtil.hash(password));
                DAO.crear(usuario);
            } else {
                DAO.actualizar(usuario);
                if (password != null && !password.isBlank()) {
                    DAO.actualizarPassword(usuario.getId(), PasswordUtil.hash(password));
                }
            }
            Mensajes.info("Usuario \"" + usuario.getNombre() + "\" " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
            editando = false;
            usuario = null;
            password = null;
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo guardar el usuario (revisa que el correo no esté repetido).");
        }
    }

    public void alternarActivo(Usuario u) {
        try {
            DAO.cambiarActivo(u.getId(), !u.isActivo());
            Mensajes.info("Usuario \"" + u.getNombre() + "\" " + (u.isActivo() ? "desactivado" : "activado") + ".");
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo cambiar el estado del usuario.");
        }
    }

    public RolUsuario[] getRoles() { return RolUsuario.values(); }

    public boolean isEditando() { return editando; }
    public Usuario getUsuario() { return usuario; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
