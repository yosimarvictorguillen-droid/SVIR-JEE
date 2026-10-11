package com.svir.jee.util;

/** Utilidades para texto libre que escribe el usuario. */
public final class Textos {

    private Textos() {
    }

    /**
     * Quita los caracteres con los que se podria inyectar HTML en pantallas que imprimen el texto
     * sin escapar (< > & " ' `) y el separador "|" de las observaciones; recorta espacios y largo.
     * Devuelve "" si el texto es nulo o queda vacio.
     */
    public static String limpiar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.replaceAll("[<>&\"'`|]", "").trim();
        return limpio.length() > maximo ? limpio.substring(0, maximo).trim() : limpio;
    }
}
