package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla productos. JDBC puro (PreparedStatement). */
public class ProductoDAO {

    public List<Producto> listarTodos() throws SQLException {
        String sql = "SELECT * FROM productos ORDER BY nombre";
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Producto> listarActivos() throws SQLException {
        String sql = "SELECT * FROM productos WHERE activo = 1 ORDER BY nombre";
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Producto> listarStockBajo() throws SQLException {
        String sql = "SELECT * FROM productos WHERE activo = 1 AND stock <= stock_minimo ORDER BY nombre";
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Producto> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM productos WHERE id = ?";
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

    public Producto crear(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, stock, stock_minimo, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setBoolean(6, p.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        }
        return p;
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, "
                + "stock = ?, stock_minimo = ?, activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarImagen(int id, String imagenUrl) throws SQLException {
        String sql = "UPDATE productos SET imagen_url = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, imagenUrl);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int id, boolean activo) throws SQLException {
        String sql = "UPDATE productos SET activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Descuenta stock dentro de una transaccion ya abierta (Connection recibida,
     * no se cierra aqui). Lanza excepcion si el stock resultante seria negativo.
     */
    public void descontarStock(Connection con, int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            ps.setInt(3, cantidad);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Stock insuficiente para el producto id=" + productoId);
            }
        }
    }

    /** Devuelve stock dentro de una transaccion ya abierta. */
    public void restaurarStock(Connection con, int productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, productoId);
            ps.executeUpdate();
        }
    }

    public int obtenerStockActual(Connection con, int productoId) throws SQLException {
        String sql = "SELECT stock FROM productos WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Producto no encontrado id=" + productoId);
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setActivo(rs.getBoolean("activo"));
        p.setImagenUrl(rs.getString("imagen_url"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            p.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            p.setUpdatedAt(updated.toLocalDateTime());
        }
        return p;
    }
}
