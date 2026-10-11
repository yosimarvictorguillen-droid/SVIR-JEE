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

    /**
     * Si dos nombres corresponden a la misma persona: sin tildes ni mayusculas, todas las palabras del nombre mas
     * corto deben estar en el mas largo ("Juan Perez" coincide con "Juan Perez Lopez", pero no con "Pedro Gomez").
     */
    public static boolean mismoNombre(String a, String b) {
        java.util.Set<String> ta = palabras(a);
        java.util.Set<String> tb = palabras(b);
        if (ta.isEmpty() || tb.isEmpty()) {
            return false;
        }
        return ta.size() <= tb.size() ? tb.containsAll(ta) : ta.containsAll(tb);
    }

    private static java.util.Set<String> palabras(String nombre) {
        java.util.Set<String> set = new java.util.HashSet<>();
        if (nombre == null) {
            return set;
        }
        String base = java.text.Normalizer.normalize(nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9ñ ]", " ");
        for (String p : base.trim().split("\\s+")) {
            if (!p.isEmpty()) {
                set.add(p);
            }
        }
        return set;
    }

    /** Escapa HTML para imprimir en una pagina texto que NO paso por {@link #limpiar} (por ejemplo parametros de la URL). */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
