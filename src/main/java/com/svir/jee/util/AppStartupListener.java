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
        asegurarColumnasComprobante();
        asegurarModeloDocumentos();

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

    /**
     * Migracion automatica: agrega a pedidos las columnas del comprobante (boleta/factura) y un indice
     * unico sobre el numero, para que no se repita ni se emita dos veces.
     */
    private void asegurarColumnasComprobante() {
        String existe = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pedidos' AND COLUMN_NAME = 'comprobante_numero'";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(existe)) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
            try (Statement ddl = con.createStatement()) {
                ddl.executeUpdate("ALTER TABLE pedidos "
                        + "ADD COLUMN comprobante_tipo VARCHAR(20) NULL AFTER observacion, "
                        + "ADD COLUMN comprobante_numero VARCHAR(20) NULL AFTER comprobante_tipo, "
                        + "ADD COLUMN comprobante_documento VARCHAR(20) NULL AFTER comprobante_numero, "
                        + "ADD COLUMN comprobante_fecha DATETIME NULL AFTER comprobante_documento, "
                        + "ADD UNIQUE KEY uq_pedido_comprobante (comprobante_numero)");
                LOG.info("Migracion: columnas de comprobante creadas en pedidos.");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudieron crear las columnas de comprobante en pedidos.", e);
        }
    }

    /**
     * Migracion automatica del modelo de documentos: tipo de documento en clientes (y dni ampliado a 12 caracteres),
     * tabla de invitados, registro de conversiones invitado -> cliente, pedidos.invitado_id e indice unico del documento.
     */
    private void asegurarModeloDocumentos() {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             Statement ddl = con.createStatement()) {
            if (!existeColumna(con, "clientes", "tipo_documento")) {
                ddl.executeUpdate("ALTER TABLE clientes "
                        + "ADD COLUMN tipo_documento VARCHAR(12) NOT NULL DEFAULT 'DNI' AFTER nombre, "
                        + "MODIFY COLUMN dni VARCHAR(12) NULL");
                LOG.info("Migracion: clientes.tipo_documento creado y clientes.dni ampliado a 12 caracteres.");
            }
            ddl.executeUpdate("CREATE TABLE IF NOT EXISTS invitados ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "tipo_documento VARCHAR(12) NOT NULL DEFAULT 'DNI', "
                    + "documento VARCHAR(12) NOT NULL, "
                    + "nombre VARCHAR(150) NOT NULL, "
                    + "telefono VARCHAR(30) NULL, "
                    + "estado ENUM('ACTIVO','CONVERTIDO') NOT NULL DEFAULT 'ACTIVO', "
                    + "cliente_id INT NULL, "
                    + "fecha_conversion DATETIME NULL, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE KEY uq_invitado_documento (documento), "
                    + "CONSTRAINT fk_invitado_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)"
                    + ") ENGINE=InnoDB");
            ddl.executeUpdate("CREATE TABLE IF NOT EXISTS conversiones_invitado ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "invitado_id INT NOT NULL, "
                    + "cliente_id INT NOT NULL, "
                    + "pedidos_vinculados INT NOT NULL DEFAULT 0, "
                    + "validacion VARCHAR(30) NOT NULL, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT fk_conv_invitado FOREIGN KEY (invitado_id) REFERENCES invitados(id), "
                    + "CONSTRAINT fk_conv_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)"
                    + ") ENGINE=InnoDB");
            if (!existeColumna(con, "pedidos", "invitado_id")) {
                ddl.executeUpdate("ALTER TABLE pedidos ADD COLUMN invitado_id INT NULL AFTER cliente_id, "
                        + "ADD CONSTRAINT fk_pedido_invitado FOREIGN KEY (invitado_id) REFERENCES invitados(id)");
                LOG.info("Migracion: pedidos.invitado_id creado.");
            }
            if (!existeIndice(con, "clientes", "uq_cliente_documento")) {
                try {
                    ddl.executeUpdate("ALTER TABLE clientes ADD UNIQUE KEY uq_cliente_documento (dni)");
                    LOG.info("Migracion: indice unico del documento de clientes creado.");
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "No se pudo crear el indice unico clientes.dni (hay documentos repetidos). "
                            + "Corrige los clientes duplicados; la aplicacion igual valida la unicidad.", e);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo migrar el modelo de documentos e invitados.", e);
        }
    }

    private boolean existeColumna(Connection con, String tabla, String columna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + tabla + "' AND COLUMN_NAME = '" + columna + "'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private boolean existeIndice(Connection con, String tabla, String indice) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.STATISTICS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + tabla + "' AND INDEX_NAME = '" + indice + "'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nada que liberar
    }
}
