package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.Ingrediente;
import com.svir.jee.model.MotivoMovimientoIngrediente;
import com.svir.jee.model.MovimientoIngrediente;
import com.svir.jee.model.TipoMovimiento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de la tabla ingredientes. JDBC puro. */
public class IngredienteDAO {

    public List<Ingrediente> listarTodos() throws SQLException {
        String sql = "SELECT * FROM ingredientes ORDER BY nombre";
        List<Ingrediente> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Ingrediente> listarStockBajo() throws SQLException {
        String sql = "SELECT * FROM ingredientes WHERE activo = 1 AND stock <= stock_minimo ORDER BY nombre";
        List<Ingrediente> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Ingrediente> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM ingredientes WHERE id = ?";
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

    public Ingrediente crear(Ingrediente i) throws SQLException {
        String sql = "INSERT INTO ingredientes (nombre, unidad_medida, stock, stock_minimo, activo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidadMedida());
            ps.setBigDecimal(3, i.getStock());
            ps.setBigDecimal(4, i.getStockMinimo());
            ps.setBoolean(5, i.isActivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    i.setId(keys.getInt(1));
                }
            }
        }
        return i;
    }

    public void actualizar(Ingrediente i) throws SQLException {
        String sql = "UPDATE ingredientes SET nombre = ?, unidad_medida = ?, stock = ?, "
                + "stock_minimo = ?, activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidadMedida());
            ps.setBigDecimal(3, i.getStock());
            ps.setBigDecimal(4, i.getStockMinimo());
            ps.setBoolean(5, i.isActivo());
            ps.setInt(6, i.getId());
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int id, boolean activo) throws SQLException {
        String sql = "UPDATE ingredientes SET activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Lee el stock actual bloqueando la fila (usar dentro de una transaccion). */
    public BigDecimal obtenerStockActual(Connection con, int ingredienteId) throws SQLException {
        String sql = "SELECT stock FROM ingredientes WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ingredienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        throw new SQLException("Ingrediente no encontrado id=" + ingredienteId);
    }

    /** Fija el stock a un valor absoluto (usado por movimientos AJUSTE), dentro de una transaccion. */
    public void fijarStock(Connection con, int ingredienteId, BigDecimal nuevoStock) throws SQLException {
        String sql = "UPDATE ingredientes SET stock = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoStock);
            ps.setInt(2, ingredienteId);
            ps.executeUpdate();
        }
    }

    /**
     * Registra un movimiento de stock (ENTRADA suma, SALIDA resta validando que
     * no quede negativo, AJUSTE fija el stock al valor indicado) y guarda el
     * movimiento de auditoria, todo en una sola transaccion.
     */
    public void registrarMovimiento(int ingredienteId, TipoMovimiento tipo, MotivoMovimientoIngrediente motivo,
                                     BigDecimal cantidad, Integer usuarioId) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            BigDecimal stockAnterior = obtenerStockActual(con, ingredienteId);
            BigDecimal stockNuevo;
            switch (tipo) {
                case ENTRADA -> stockNuevo = stockAnterior.add(cantidad);
                case SALIDA -> {
                    stockNuevo = stockAnterior.subtract(cantidad);
                    if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) {
                        throw new SQLException("Stock insuficiente: hay " + stockAnterior + " y se intenta sacar " + cantidad);
                    }
                }
                case AJUSTE -> stockNuevo = cantidad;
                default -> throw new IllegalArgumentException("Tipo de movimiento no soportado: " + tipo);
            }

            fijarStock(con, ingredienteId, stockNuevo);

            MovimientoIngrediente mov = new MovimientoIngrediente();
            mov.setIngredienteId(ingredienteId);
            mov.setTipo(tipo);
            mov.setMotivo(motivo);
            mov.setCantidad(cantidad);
            mov.setStockAnterior(stockAnterior);
            mov.setStockNuevo(stockNuevo);
            mov.setReferenciaTipo("MANUAL");
            mov.setUsuarioId(usuarioId);
            new MovimientoIngredienteDAO().registrar(con, mov);

            con.commit();
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

    private Ingrediente mapear(ResultSet rs) throws SQLException {
        Ingrediente i = new Ingrediente();
        i.setId(rs.getInt("id"));
        i.setNombre(rs.getString("nombre"));
        i.setUnidadMedida(rs.getString("unidad_medida"));
        i.setStock(rs.getBigDecimal("stock"));
        i.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        i.setActivo(rs.getBoolean("activo"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            i.setCreatedAt(created.toLocalDateTime());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            i.setUpdatedAt(updated.toLocalDateTime());
        }
        return i;
    }
}
