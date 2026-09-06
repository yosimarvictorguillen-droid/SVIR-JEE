package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.DashboardResumen;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Consultas agregadas (dashboard y reportes) con SQL directo. */
public class ReporteDAO {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    public DashboardResumen resumenDashboard() throws SQLException {
        DashboardResumen r = new DashboardResumen();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {

            r.setVentasHoy(sumarTotal(con,
                    "SELECT COALESCE(SUM(total),0) FROM pedidos "
                            + "WHERE estado <> 'CANCELADO' AND DATE(created_at) = CURDATE()"));

            r.setVentasMes(sumarTotal(con,
                    "SELECT COALESCE(SUM(total),0) FROM pedidos "
                            + "WHERE estado <> 'CANCELADO' AND YEAR(created_at) = YEAR(CURDATE()) "
                            + "AND MONTH(created_at) = MONTH(CURDATE())"));

            r.setPedidosHoy(contar(con,
                    "SELECT COUNT(*) FROM pedidos WHERE estado <> 'CANCELADO' AND DATE(created_at) = CURDATE()"));

            r.setPedidosPendientes(contar(con, "SELECT COUNT(*) FROM pedidos WHERE estado = 'PENDIENTE'"));
            r.setPedidosPreparacion(contar(con, "SELECT COUNT(*) FROM pedidos WHERE estado = 'PREPARACION'"));
            r.setPedidosListo(contar(con, "SELECT COUNT(*) FROM pedidos WHERE estado = 'LISTO'"));

            r.setTotalProductos(contar(con, "SELECT COUNT(*) FROM productos WHERE activo = 1"));
            r.setTotalIngredientes(contar(con, "SELECT COUNT(*) FROM ingredientes WHERE activo = 1"));

            r.setProductosStockBajo(contar(con,
                    "SELECT COUNT(*) FROM productos WHERE activo = 1 AND stock <= stock_minimo"));
            r.setIngredientesStockBajo(contar(con,
                    "SELECT COUNT(*) FROM ingredientes WHERE activo = 1 AND stock <= stock_minimo"));
        }

        r.setPedidosRecientes(pedidoDAO.listarTodos().stream().limit(5).toList());
        return r;
    }

    /** Total vendido (suma de pedidos no cancelados) en un rango de fechas inclusivo. */
    public BigDecimal totalVentasEnRango(java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total),0) FROM pedidos "
                + "WHERE estado <> 'CANCELADO' AND DATE(created_at) BETWEEN ? AND ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private BigDecimal sumarTotal(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private int contar(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
