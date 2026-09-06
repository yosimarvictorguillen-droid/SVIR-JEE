package com.svir.jee.servlet;

import com.svir.jee.util.AlmacenImagenes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Sirve las imagenes de productos guardadas por AlmacenImagenes (que viven
 * fuera del .war desplegado). Valida que la ruta pedida no se escape del
 * directorio de subidas (proteccion contra path traversal).
 */
@WebServlet("/uploads/productos/*")
public class ImagenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File directorio = AlmacenImagenes.directorioProductos();
        File archivo = new File(directorio, pathInfo.substring(1));

        String dirCanonico = directorio.getCanonicalPath();
        String archivoCanonico = archivo.getCanonicalPath();
        if (!archivoCanonico.startsWith(dirCanonico + File.separator) || !archivo.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = getServletContext().getMimeType(archivo.getName());
        resp.setContentType(contentType != null ? contentType : "application/octet-stream");
        resp.setContentLengthLong(archivo.length());
        try (FileInputStream in = new FileInputStream(archivo)) {
            in.transferTo(resp.getOutputStream());
        }
    }
}
