package com.svir.jee.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

/** Lectura segura de los parametros de filtro de las pantallas JSP (GET). */
public final class Filtros {

    private Filtros() {
    }

    /** Texto libre de busqueda, sin caracteres peligrosos (se vuelve a mostrar en el formulario). */
    public static String texto(HttpServletRequest req, String nombre) {
        return Textos.limpiar(req.getParameter(nombre), 100);
    }

    /** Valor de una lista cerrada de opciones; cualquier otro valor se ignora ("" = sin filtro). */
    public static String opcion(HttpServletRequest req, String nombre, String... validas) {
        String valor = req.getParameter(nombre);
        return valor != null && Arrays.asList(validas).contains(valor) ? valor : "";
    }

    public static LocalDate fecha(HttpServletRequest req, String nombre) {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Arma "a=1&b=2" con los pares nombre,valor que no esten vacios (valores codificados para URL). */
    public static String query(String... paresNombreValor) {
        StringBuilder qs = new StringBuilder();
        for (int i = 0; i + 1 < paresNombreValor.length; i += 2) {
            String valor = paresNombreValor[i + 1];
            if (valor != null && !valor.isEmpty()) {
                qs.append(qs.length() == 0 ? "" : "&").append(paresNombreValor[i]).append('=')
                        .append(URLEncoder.encode(valor, StandardCharsets.UTF_8));
            }
        }
        return qs.toString();
    }

    /** Atributo de la peticion como texto ("" si no existe); sirve para rellenar el formulario. */
    public static String attr(HttpServletRequest req, String nombre) {
        Object valor = req.getAttribute(nombre);
        return valor == null ? "" : valor.toString();
    }

    /** "selected" si el valor actual coincide con la opcion. */
    public static String sel(String actual, String opcion) {
        return actual != null && actual.equals(opcion) ? "selected" : "";
    }
}
