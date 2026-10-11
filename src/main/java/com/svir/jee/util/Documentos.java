package com.svir.jee.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Documento de identidad del comprador (cliente o invitado). El numero completo es unico en todo el sistema:
 * DNI (8 digitos), carne de extranjeria (9 a 12 letras o numeros) o pasaporte (6 a 12 letras o numeros).
 */
public final class Documentos {

    public static final List<String> TIPOS = Arrays.asList("DNI", "CE", "PASAPORTE");

    private Documentos() {
    }

    /** Devuelve el tipo si es valido; si no, DNI. */
    public static String tipoValido(String tipo) {
        return tipo != null && TIPOS.contains(tipo.trim().toUpperCase(Locale.ROOT)) ? tipo.trim().toUpperCase(Locale.ROOT) : "DNI";
    }

    /** Sin espacios ni guiones y en mayusculas, para comparar y guardar de forma uniforme. */
    public static String normalizar(String documento) {
        if (documento == null) {
            return "";
        }
        return documento.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    /** Mensaje de error si el numero no corresponde al tipo; null si es valido. */
    public static String validar(String tipo, String documento) {
        String doc = normalizar(documento);
        if (doc.isEmpty()) {
            return "El documento es obligatorio.";
        }
        switch (tipoValido(tipo)) {
            case "CE":
                return doc.matches("[A-Z0-9]{9,12}") ? null : "El carne de extranjeria debe tener entre 9 y 12 letras o numeros.";
            case "PASAPORTE":
                return doc.matches("[A-Z0-9]{6,12}") ? null : "El pasaporte debe tener entre 6 y 12 letras o numeros.";
            default:
                return doc.matches("\\d{8}") ? null : "El DNI debe tener 8 digitos.";
        }
    }

    public static String etiqueta(String tipo) {
        switch (tipoValido(tipo)) {
            case "CE":
                return "Carne de extranjeria";
            case "PASAPORTE":
                return "Pasaporte";
            default:
                return "DNI";
        }
    }
}
