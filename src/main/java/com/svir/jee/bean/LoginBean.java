package com.svir.jee.bean;

import com.svir.jee.dao.UsuarioDAO;
import com.svir.jee.model.Usuario;
import com.svir.jee.util.PasswordUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Managed Bean del login JSF. Guarda al usuario en la misma sesion HTTP
 * ("usuario") que usa el login JSP, asi AuthFilter protege ambos paneles.
 */
@Named
@RequestScoped
public class LoginBean {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private String email;
    private String password;

    /** Devuelve un "outcome" resuelto por las reglas de navegacion de faces-config.xml. */
    public String ingresar() {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorEmail(email.trim());
            if (encontrado.isEmpty()
                    || !encontrado.get().isActivo()
                    || !PasswordUtil.verificar(password, encontrado.get().getPasswordHash())) {
                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Correo o contraseña incorrectos.", null));
                return null;
            }
            HttpSession session = (HttpSession) fc.getExternalContext().getSession(true);
            session.setAttribute("usuario", encontrado.get());
            session.setMaxInactiveInterval(60 * 60);
            return "dashboard";
        } catch (SQLException e) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con la base de datos.", null));
            return null;
        }
    }

    public String salir() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login";
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
