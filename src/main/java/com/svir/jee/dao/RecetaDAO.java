package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.RecetaItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Acceso a datos de la tabla recetas (ingrediente + cantidad por producto). JDBC puro. */
public class RecetaDAO {

    public List<RecetaItem> listarPorProducto(int productoId) throws SQLException {
        String sql = "SELECT r.id, r.producto_id, r.ingrediente_id, r.cantidad, "
                + "i.nombre AS ingrediente_nombre, i.unidad_medida "
                + "FROM recetas r JOIN ingredientes i ON i.id = r.ingrediente_id "
                + "WHERE r.producto_id = ? ORDER BY i.nombre";
        List<RecetaItem> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Reemplaza toda la receta de un producto: borra las lineas anteriores e inserta las nuevas, en una transaccion. */
    public void reemplazar(int productoId, List<RecetaItem> items) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            try (PreparedStatement del = con.prepareStatement("DELETE FROM recetas WHERE producto_id = ?")) {
                del.setInt(1, productoId);
                del.executeUpdate();
            }

            String sqlIns = "INSERT INTO recetas (producto_id, ingrediente_id, cantidad) VALUES (?, ?, ?)";
            try (PreparedStatement ins = con.prepareStatement(sqlIns)) {
                for (RecetaItem item : items) {
                    ins.setInt(1, productoId);
                    ins.setInt(2, item.getIngredienteId());
                    ins.setBigDecimal(3, item.getCantidad());
                    ins.addBatch();
                }
                if (!items.isEmpty()) {
                    ins.executeBatch();
                }
            }

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

    /**
     * Mapa ingredienteId -> cantidad por unidad de producto, dentro de una
     * transaccion ya abierta. Usado por ProduccionDAO para calcular cuanto
     * ingrediente consume producir N unidades de un producto.
     */
    public Map<Integer, BigDecimal> obtenerCantidadesPorProducto(Connection con, int productoId) throws SQLException {
        String sql = "SELECT ingrediente_id, cantidad FROM recetas WHERE producto_id = ?";
        Map<Integer, BigDecimal> mapa = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mapa.put(rs.getInt("ingrediente_id"), rs.getBigDecimal("cantidad"));
                }
            }
        }
        return mapa;
    }

    public boolean tieneReceta(Connection con, int productoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM recetas WHERE producto_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private RecetaItem mapear(ResultSet rs) throws SQLException {
        RecetaItem item = new RecetaItem();
        item.setId(rs.getInt("id"));
        item.setProductoId(rs.getInt("producto_id"));
        item.setIngredienteId(rs.getInt("ingrediente_id"));
        item.setIngredienteNombre(rs.getString("ingrediente_nombre"));
        item.setUnidadMedida(rs.getString("unidad_medida"));
        item.setCantidad(rs.getBigDecimal("cantidad"));
        return item;
    }
}
