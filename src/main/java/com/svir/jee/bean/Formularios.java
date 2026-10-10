package com.svir.jee.bean;

import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * Limpia el estado de los campos de un formulario JSF (valor enviado, validez).
 * Sin esto, un modal que se cierra con "Cancelar" vuelve a abrirse mostrando el
 * texto escrito antes en lugar de los datos del registro que se quiere editar.
 */
public final class Formularios {

    private Formularios() {
    }

    public static void resetear(String idFormulario) {
        UIComponent raiz = FacesContext.getCurrentInstance().getViewRoot().findComponent(idFormulario);
        if (raiz != null) {
            resetearArbol(raiz);
        }
    }

    private static void resetearArbol(UIComponent componente) {
        if (componente instanceof EditableValueHolder campo) {
            campo.resetValue();
        }
        for (UIComponent hijo : componente.getChildren()) {
            resetearArbol(hijo);
        }
    }
}
