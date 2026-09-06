package com.svir.jee.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase singleton responsable de entregar conexiones JDBC a MySQL.
 *
 * No mantiene una unica Connection compartida (JDBC/Connection no es segura
 * para hilos concurrentes): expone una unica instancia que conoce como
 * cargar el driver y los parametros de conexion, y cada llamada a
 * {@link #obtenerConexion()} abre una conexion nueva que el que la use
 * debe cerrar (try-with-resources) para devolverla lo antes posible.
 */
public final class ConexionBD {

    private static final ConexionBD INSTANCIA = new ConexionBD();

    private final String url;
    private final String usuario;
    private final String password;

    private ConexionBD() {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontro db.properties en el classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo db.properties", e);
        }

        this.url = props.getProperty("db.url");
        this.usuario = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        try {
            Class.forName(props.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC de MySQL no encontrado en el classpath", e);
        }
    }

    public static ConexionBD getInstancia() {
        return INSTANCIA;
    }

    /**
     * Abre y devuelve una conexion JDBC nueva. El llamador es responsable
     * de cerrarla (usar try-with-resources).
     */
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }
}
