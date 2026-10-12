package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla clientes. JDBC puro. La columna "dni" guarda el numero de documento (DNI, carne de
 * extranjeria o pasaporte; el tipo esta en tipo_documento) y es unica en todo el sistema
 */
public class ClienteDAO {

    private static final String SELECT_CLIENTES = "SELECT c.*, "
            + "(SELECT MAX(i.fecha_conversion) FROM invitados i WHERE i.cliente_id = c.id) AS fecha_conversion "
            + "FROM clientes c ";

    private final InvitadoDAO invitadoDAO = new InvitadoDAO();

    public List<Cliente> listarTodos() throws SQLException {
        return listar(SELECT_CLIENTES + "ORDER BY c.nombre");
    }

    public List<Cliente> listarActivos() throws SQLException {
        return listar(SELECT_CLIENTES + "WHERE c.activo = 1 ORDER BY c.nombre");
    }

    private List<Cliente> listar(String sql) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        String sql = SELECT_CLIENTES + "WHERE c.id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** Busca por numero de documento (la columna se llama dni por historia del esquema). */
    public Optional<Cliente> buscarPorDni(String documento) throws SQLException {
        String sql = SELECT_CLIENTES + "WHERE c.dni = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existeDni(String documento) throws SQLException {
        return existeDocumento(documento, null);
    }

    /** Si otro cliente (distinto de {@code excluirId}) ya tiene ese documento. */
    public boolean existeDocumento(String documento, Integer excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE dni = ? AND (? IS NULL OR id <> ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            if (excluirId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, excluirId);
                ps.setInt(3, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Alta de una cuenta de tienda web (con password). */
    public Cliente registrar(Cliente c) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            return registrar(con, c);
        }
    }

    public Cliente registrar(Connection con, Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, tipo_documento, dni, telefono, direccion, email, password_hash, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTipoDocumento());
            ps.setString(3, c.getDni());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getEmail());
            ps.setString(7, c.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        }
        return c;
    }

    public Cliente crear(Cliente c) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            return crear(con, c);
        }
    }

    public Cliente crear(Connection con, Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, tipo_documento, dni, ruc, telefono, direccion, email, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTipoDocumento());
            ps.setString(3, c.getDni());
            ps.setString(4, c.getRuc());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getDireccion());
            ps.setString(7, c.getEmail());
            ps.setBoolean(8, c.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        }
        return c;
    }

    /**
     * Da de alta al cliente y, en la misma transaccion, vincula los pedidos del invitado que tenia ese documento
     * (si lo habia). {@code conCuentaTienda} usa el alta de la tienda (con password); si no, el alta del personal.
     * Devuelve cuantos pedidos se vincularon.
     *
     * @param validacion como se comprobo la identidad: PEDIDO_ANTERIOR (tienda) o PERSONAL (alta por el personal)
     */
    public int altaConVinculo(Cliente c, boolean conCuentaTienda, String validacion) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);
            if (conCuentaTienda) {
                registrar(con, c);
            } else {
                crear(con, c);
            }
            int vinculados = invitadoDAO.vincularTrasAlta(con, c, validacion);
            con.commit();
            return vinculados;
        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, tipo_documento = ?, dni = ?, ruc = ?, telefono = ?, "
                + "direccion = ?, email = ?, activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getTipoDocumento());
            ps.setString(3, c.getDni());
            ps.setString(4, c.getRuc());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getDireccion());
            ps.setString(7, c.getEmail());
            ps.setBoolean(8, c.isActivo());
            ps.setInt(9, c.getId());
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int id, boolean activo) throws SQLException {
        String sql = "UPDATE clientes SET activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setTipoDocumento(rs.getString("tipo_documento"));
        c.setDni(rs.getString("dni"));
        c.setRuc(rs.getString("ruc"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setEmail(rs.getString("email"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setActivo(rs.getBoolean("activo"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            c.setUpdatedAt(updated.toLocalDateTime());
        }
        Timestamp conversion = rs.getTimestamp("fecha_conversion");
        if (conversion != null) {
            c.setFechaConversionInvitado(conversion.toLocalDateTime());
        }
        return c;
    }
}
