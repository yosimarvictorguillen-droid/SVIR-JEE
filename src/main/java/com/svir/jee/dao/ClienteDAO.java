package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla clientes. JDBC puro. */
public class ClienteDAO {

    public List<Cliente> listarTodos() throws SQLException {
        String sql = "SELECT * FROM clientes ORDER BY nombre";
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

    public List<Cliente> listarActivos() throws SQLException {
        String sql = "SELECT * FROM clientes WHERE activo = 1 ORDER BY nombre";
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
        String sql = "SELECT * FROM clientes WHERE id = ?";
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

    public Optional<Cliente> buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE dni = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existeDni(String dni) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE dni = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Alta de una cuenta de tienda web (con password). */
    public Cliente registrar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, dni, telefono, direccion, email, password_hash, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDni());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getPasswordHash());
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
        String sql = "INSERT INTO clientes (nombre, dni, ruc, telefono, direccion, email, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDni());
            ps.setString(3, c.getRuc());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getEmail());
            ps.setBoolean(7, c.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        }
        return c;
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, dni = ?, ruc = ?, telefono = ?, "
                + "direccion = ?, email = ?, activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDni());
            ps.setString(3, c.getRuc());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getEmail());
            ps.setBoolean(7, c.isActivo());
            ps.setInt(8, c.getId());
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
        return c;
    }
}
