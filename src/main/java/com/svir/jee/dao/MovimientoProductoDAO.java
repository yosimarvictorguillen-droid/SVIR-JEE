package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.MotivoMovimientoProducto;
import com.svir.jee.model.MovimientoProducto;
import com.svir.jee.model.TipoMovimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Auditoria de movimientos de stock de productos. JDBC puro. */
public class MovimientoProductoDAO {

    /** Inserta el movimiento dentro de una transaccion ya abierta (no cierra la conexion). */
    public void registrar(Connection con, MovimientoProducto m) throws SQLException {
        String sql = "INSERT INTO movimientos_producto "
                + "(producto_id, tipo, motivo, cantidad, stock_anterior, stock_nuevo, "
                + "referencia_tipo, referencia_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getProductoId());
            ps.setString(2, m.getTipo().name());
            ps.setString(3, m.getMotivo().name());
            ps.setInt(4, m.getCantidad());
            ps.setInt(5, m.getStockAnterior());
            ps.setInt(6, m.getStockNuevo());
            ps.setString(7, m.getReferenciaTipo());
            if (m.getReferenciaId() != null) {
                ps.setInt(8, m.getReferenciaId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            if (m.getUsuarioId() != null) {
                ps.setInt(9, m.getUsuarioId());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    public List<MovimientoProducto> listarTodos() throws SQLException {
        String sql = "SELECT m.*, p.nombre AS producto_nombre, u.nombre AS usuario_nombre "
                + "FROM movimientos_producto m "
                + "JOIN productos p ON p.id = m.producto_id "
                + "LEFT JOIN usuarios u ON u.id = m.usuario_id "
                + "ORDER BY m.id DESC";
        List<MovimientoProducto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ---- busqueda con filtros (pantalla Movimientos) ----

    private String condiciones(String texto, String tipo, java.time.LocalDate desde, java.time.LocalDate hasta,
                               List<Object> params) {
        StringBuilder w = new StringBuilder("WHERE 1 = 1 ");
        if (texto != null && !texto.isBlank()) {
            String seguro = texto.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            w.append("AND LOWER(p.nombre) LIKE ? ");
            params.add("%" + seguro + "%");
        }
        if ("ENTRADA".equals(tipo) || "SALIDA".equals(tipo)) {
            w.append("AND m.tipo = ? ");
            params.add(tipo);
        }
        if (desde != null) {
            w.append("AND DATE(m.created_at) >= ? ");
            params.add(desde);
        }
        if (hasta != null) {
            w.append("AND DATE(m.created_at) <= ? ");
            params.add(hasta);
        }
        return w.toString();
    }

    public int contar(String texto, String tipo, java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM movimientos_producto m JOIN productos p ON p.id = m.producto_id "
                + condiciones(texto, tipo, desde, hasta, params);
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<MovimientoProducto> buscar(String texto, String tipo, java.time.LocalDate desde, java.time.LocalDate hasta,
                                  int limite, int offset) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT m.*, p.nombre AS producto_nombre, u.nombre AS usuario_nombre "
                + "FROM movimientos_producto m JOIN productos p ON p.id = m.producto_id "
                + "LEFT JOIN usuarios u ON u.id = m.usuario_id "
                + condiciones(texto, tipo, desde, hasta, params) + "ORDER BY m.id DESC LIMIT ? OFFSET ?";
        List<MovimientoProducto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (Object p : params) {
                ps.setObject(i++, p);
            }
            ps.setInt(i++, limite);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private MovimientoProducto mapear(ResultSet rs) throws SQLException {
        MovimientoProducto m = new MovimientoProducto();
        m.setId(rs.getInt("id"));
        m.setProductoId(rs.getInt("producto_id"));
        m.setProductoNombre(rs.getString("producto_nombre"));
        m.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
        m.setMotivo(MotivoMovimientoProducto.valueOf(rs.getString("motivo")));
        m.setCantidad(rs.getInt("cantidad"));
        m.setStockAnterior(rs.getInt("stock_anterior"));
        m.setStockNuevo(rs.getInt("stock_nuevo"));
        m.setReferenciaTipo(rs.getString("referencia_tipo"));
        int refId = rs.getInt("referencia_id");
        m.setReferenciaId(rs.wasNull() ? null : refId);
        m.setUsuarioNombre(rs.getString("usuario_nombre"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            m.setCreatedAt(created.toLocalDateTime());
        }
        return m;
    }
}
