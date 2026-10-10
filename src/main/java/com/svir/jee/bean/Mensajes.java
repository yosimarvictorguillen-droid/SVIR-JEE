package com.svir.jee.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/** Atajos para mostrar mensajes globales de JSF (se ven como alertas Bootstrap en la plantilla). */
public final class Mensajes {

    private Mensajes() {
    }

    public static void info(String texto) {
        agregar(FacesMessage.SEVERITY_INFO, texto);
    }

    public static void aviso(String texto) {
        agregar(FacesMessage.SEVERITY_WARN, texto);
    }

    public static void error(String texto) {
        agregar(FacesMessage.SEVERITY_ERROR, texto);
    }

    private static void agregar(FacesMessage.Severity severidad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, texto, null));
    }
}
