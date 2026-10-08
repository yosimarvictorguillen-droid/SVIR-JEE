package com.svir.jee.bean;

import com.svir.jee.dao.ProductoDAO;
import com.svir.jee.model.Producto;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Bean de diagnostico: confirma que JSF + CDI + DAO/JDBC funcionan juntos en Tomcat. */
@Named
@RequestScoped
public class PruebaBean {

    private final ProductoDAO productoDAO = new ProductoDAO();

    private String nombre;
    private String saludo;

    public String getMensaje() {
        return "JSF + CDI funcionando en Tomcat";
    }

    public String getHora() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getImplementacionFaces() {
        return FacesContext.getCurrentInstance().getClass().getPackageName();
    }

    public List<Producto> getProductos() throws SQLException {
        return productoDAO.listarActivos();
    }

    public void saludar() {
        saludo = "Hola, " + (nombre == null || nombre.isBlank() ? "visitante" : nombre.trim()) + "!";
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSaludo() { return saludo; }
}
