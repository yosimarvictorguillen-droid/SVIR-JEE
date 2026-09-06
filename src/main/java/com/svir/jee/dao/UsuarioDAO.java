package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla usuarios (personal interno). JDBC puro. */
public class UsuarioDAO {

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY nombre";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, telefono = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getRol().name());
            ps.setString(4, u.getTelefono());
            ps.setInt(5, u.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarPassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int id, boolean activo) throws SQLException {
        String sql = "UPDATE usuarios SET activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
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

    public boolean existeAlgunUsuario() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public Usuario crear(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, email, password_hash, rol, activo, telefono) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRol().name());
            ps.setBoolean(5, u.isActivo());
            ps.setString(6, u.getTelefono());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setId(keys.getInt(1));
                }
            }
        }
        return u;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(RolUsuario.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        u.setTelefono(rs.getString("telefono"));
        return u;
    }
}
