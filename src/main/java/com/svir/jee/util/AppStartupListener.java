package com.svir.jee.util;

import com.svir.jee.dao.UsuarioDAO;
import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Al arrancar la aplicacion, si la tabla usuarios esta vacia, crea un
 * usuario ADMIN por defecto para poder entrar por primera vez sin tener
 * que insertar un hash de BCrypt a mano en el schema.sql.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Version para "cache-busting" del CSS: cambia una vez por despliegue,
        // asi el navegador no sigue mostrando estilos viejos tras un redeploy.
        sce.getServletContext().setAttribute("iniciadoEn", System.currentTimeMillis());

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        try {
            if (!usuarioDAO.existeAlgunUsuario()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setEmail("admin@dulcemomento.com");
                admin.setPasswordHash(PasswordUtil.hash("Admin123!"));
                admin.setRol(RolUsuario.ADMIN);
                admin.setActivo(true);
                usuarioDAO.crear(admin);
                LOG.info("Usuario ADMIN por defecto creado: admin@dulcemomento.com / Admin123!");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo verificar/crear el usuario ADMIN por defecto. "
                    + "Revisa que la base de datos 'reposteria_jee' exista y este accesible.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nada que liberar
    }
}
