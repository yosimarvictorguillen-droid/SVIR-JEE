package com.svir.jee.util;

import com.svir.jee.dao.UsuarioDAO;
import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

        asegurarColumnaNombreInvitado();

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

    /**
     * Migracion automatica: las bases creadas con el schema.sql anterior no tienen la columna
     * pedidos.nombre_invitado. Si falta, se agrega y se completa con el nombre que los pedidos
     * antiguos de la tienda guardaban al final de la observacion.
     */
    private void asegurarColumnaNombreInvitado() {
        String existe = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pedidos' AND COLUMN_NAME = 'nombre_invitado'";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(existe)) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
            try (Statement ddl = con.createStatement()) {
                ddl.executeUpdate("ALTER TABLE pedidos ADD COLUMN nombre_invitado VARCHAR(150) NULL AFTER cliente_id");
                int completados = ddl.executeUpdate("UPDATE pedidos "
                        + "SET nombre_invitado = TRIM(SUBSTRING_INDEX(observacion, '|', -1)) "
                        + "WHERE cliente_id IS NULL AND tipo_origen IN ('TIENDA', 'DELIVERY') AND observacion LIKE '%|%'");
                LOG.info("Migracion: columna pedidos.nombre_invitado creada; pedidos antiguos completados: " + completados);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo verificar/crear la columna pedidos.nombre_invitado.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nada que liberar
    }
}
