package com.svir.jee.dao;

import com.svir.jee.model.MotivoMovimientoIngrediente;
import com.svir.jee.model.MovimientoIngrediente;
import com.svir.jee.model.TipoMovimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Auditoria de movimientos de stock de ingredientes. JDBC puro. */
public class MovimientoIngredienteDAO {

    /** Inserta el movimiento dentro de una transaccion ya abierta (no cierra la conexion). */
    public void registrar(Connection con, MovimientoIngrediente m) throws SQLException {
        String sql = "INSERT INTO movimientos_ingrediente "
                + "(ingrediente_id, tipo, motivo, cantidad, stock_anterior, stock_nuevo, "
                + "referencia_tipo, referencia_id, usuario_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getIngredienteId());
            ps.setString(2, m.getTipo().name());
            ps.setString(3, m.getMotivo().name());
            ps.setBigDecimal(4, m.getCantidad());
            ps.setBigDecimal(5, m.getStockAnterior());
            ps.setBigDecimal(6, m.getStockNuevo());
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

    public List<MovimientoIngrediente> listarPorIngrediente(int ingredienteId) throws SQLException {
        String sql = "SELECT m.*, i.nombre AS ingrediente_nombre, u.nombre AS usuario_nombre "
                + "FROM movimientos_ingrediente m "
                + "JOIN ingredientes i ON i.id = m.ingrediente_id "
                + "LEFT JOIN usuarios u ON u.id = m.usuario_id "
                + "WHERE m.ingrediente_id = ? ORDER BY m.id DESC";
        return listar(sql, ingredienteId);
    }

    public List<MovimientoIngrediente> listarTodos() throws SQLException {
        String sql = "SELECT m.*, i.nombre AS ingrediente_nombre, u.nombre AS usuario_nombre "
                + "FROM movimientos_ingrediente m "
                + "JOIN ingredientes i ON i.id = m.ingrediente_id "
                + "LEFT JOIN usuarios u ON u.id = m.usuario_id "
                + "ORDER BY m.id DESC";
        return listar(sql, null);
    }

    private List<MovimientoIngrediente> listar(String sql, Integer ingredienteId) throws SQLException {
        List<MovimientoIngrediente> lista = new ArrayList<>();
        try (Connection con = com.svir.jee.db.ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (ingredienteId != null) {
                ps.setInt(1, ingredienteId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private MovimientoIngrediente mapear(ResultSet rs) throws SQLException {
        MovimientoIngrediente m = new MovimientoIngrediente();
        m.setId(rs.getInt("id"));
        m.setIngredienteId(rs.getInt("ingrediente_id"));
        m.setIngredienteNombre(rs.getString("ingrediente_nombre"));
        m.setTipo(TipoMovimiento.valueOf(rs.getString("tipo")));
        m.setMotivo(MotivoMovimientoIngrediente.valueOf(rs.getString("motivo")));
        m.setCantidad(rs.getBigDecimal("cantidad"));
        m.setStockAnterior(rs.getBigDecimal("stock_anterior"));
        m.setStockNuevo(rs.getBigDecimal("stock_nuevo"));
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
