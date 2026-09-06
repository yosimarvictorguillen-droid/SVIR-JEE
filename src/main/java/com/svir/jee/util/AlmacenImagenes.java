package com.svir.jee.util;

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Guarda las imagenes subidas de productos en una carpeta FUERA del
 * directorio desplegado de la aplicacion (dentro de CATALINA_BASE), para
 * que sobrevivan a un redeploy del .war. Se sirven de vuelta con
 * {@link com.svir.jee.servlet.ImagenServlet}.
 */
public final class AlmacenImagenes {

    private AlmacenImagenes() {
    }

    public static File directorioProductos() {
        String catalinaBase = System.getProperty("catalina.base");
        File base = catalinaBase != null
                ? new File(catalinaBase, "svir-uploads")
                : new File(System.getProperty("java.io.tmpdir"), "svir-uploads");
        File dir = new File(base, "productos");
        dir.mkdirs();
        return dir;
    }

    /** Guarda el archivo subido y devuelve el nombre de archivo generado (sin la ruta). */
    public static String guardar(Part imagen, int productoId) throws IOException {
        String original = imagen.getSubmittedFileName();
        String extension = "";
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        String nombreArchivo = "producto_" + productoId + "_" + System.currentTimeMillis() + extension;
        File destino = new File(directorioProductos(), nombreArchivo);
        try (InputStream in = imagen.getInputStream()) {
            Files.copy(in, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return nombreArchivo;
    }

    /** Borra un archivo previamente guardado a partir de la URL publica (/uploads/productos/xxx). */
    public static void borrarPorUrl(String imagenUrl) {
        if (imagenUrl == null || !imagenUrl.startsWith("/uploads/productos/")) {
            return;
        }
        String nombreArchivo = imagenUrl.substring("/uploads/productos/".length());
        new File(directorioProductos(), nombreArchivo).delete();
    }
}
