package com.svir.jee.bean;

import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.util.Arrays;

/** Decide que opciones del menu ve cada rol (misma matriz de permisos que AuthFilter). */
@Named
@RequestScoped
public class MenuBean {

    private boolean tieneAlguno(RolUsuario... roles) {
        Object u = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
        if (!(u instanceof Usuario usuario)) {
            return false;
        }
        return Arrays.asList(roles).contains(usuario.getRol());
    }

    public boolean isVerVentas() { return tieneAlguno(RolUsuario.ADMIN, RolUsuario.VENTAS); }
    public boolean isVerCocina() { return tieneAlguno(RolUsuario.ADMIN, RolUsuario.COCINA); }
    public boolean isVerReparto() { return tieneAlguno(RolUsuario.ADMIN, RolUsuario.VENTAS, RolUsuario.REPARTIDOR); }
    public boolean isVerAdmin() { return tieneAlguno(RolUsuario.ADMIN); }
}
