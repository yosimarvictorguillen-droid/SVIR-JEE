package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.EstadoProduccion;
import com.svir.jee.model.MotivoMovimientoIngrediente;
import com.svir.jee.model.MotivoMovimientoProducto;
import com.svir.jee.model.MovimientoIngrediente;
import com.svir.jee.model.MovimientoProducto;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.Produccion;
import com.svir.jee.model.ProduccionDetalle;
import com.svir.jee.model.TipoMovimiento;
import com.svir.jee.model.TipoProduccion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ordenes de produccion de cocina. Concentra la logica transaccional de
 * consumo de ingredientes (al crear) y de reposicion de stock de productos
 * (al terminar) o de reversa de ingredientes (al cancelar).
 */
public class ProduccionDAO {

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();
    private final MovimientoIngredienteDAO movimientoIngredienteDAO = new MovimientoIngredienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoProductoDAO movimientoProductoDAO = new MovimientoProductoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    /** Crea una orden para reponer stock (sin pedido origen). Requiere receta configurada para cada producto. */
    public Produccion crearParaStock(List<ProduccionDetalle> detalles, int usuarioId) throws SQLException {
        return crear(TipoProduccion.STOCK, null, detalles, usuarioId, "Orden para reponer stock");
    }

    /** Crea una orden a partir de las lineas de un pedido con faltante. Requiere receta configurada para cada producto. */
    public Produccion crearPorPedido(int pedidoId, int usuarioId) throws SQLException {
        Pedido pedido = pedidoDAO.buscarPorId(pedidoId)
                .orElseThrow(() -> new SQLException("Pedido no encontrado id=" + pedidoId));

        List<ProduccionDetalle> detalles = new ArrayList<>();
        for (DetallePedido linea : pedido.getDetalles()) {
            int faltante = linea.getCantidad() - linea.getCantidadAtendida();
            if (faltante > 0) {
                ProduccionDetalle pd = new ProduccionDetalle();
                pd.setProductoId(linea.getProductoId());
                pd.setCantidadPlanificada(faltante);
                detalles.add(pd);
            }
        }
        if (detalles.isEmpty()) {
            throw new SQLException("El pedido #" + pedidoId + " no tiene productos pendientes de atender.");
        }
        return crear(TipoProduccion.PEDIDO, pedidoId, detalles, usuarioId,
                "Generado desde el pedido #" + pedidoId);
    }

    private Produccion crear(TipoProduccion tipo, Integer pedidoId, List<ProduccionDetalle> detalles,
                              int usuarioId, String observacion) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            // Validar que TODOS los productos tengan receta antes de tocar ningun stock.
            for (ProduccionDetalle detalle : detalles) {
                if (!recetaDAO.tieneReceta(con, detalle.getProductoId())) {
                    throw new SQLException("El producto id=" + detalle.getProductoId()
                            + " no tiene una receta configurada. Configura su receta antes de producirlo.");
                }
            }

            String sqlProduccion = "INSERT INTO producciones (tipo, pedido_id, usuario_id, estado, observacion, fecha_inicio) "
                    + "VALUES (?, ?, ?, 'PENDIENTE', ?, CURRENT_TIMESTAMP)";
            Integer produccionId;
            try (PreparedStatement ps = con.prepareStatement(sqlProduccion, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tipo.name());
                if (pedidoId != null) {
                    ps.setInt(2, pedidoId);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setInt(3, usuarioId);
                ps.setString(4, observacion);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    produccionId = keys.getInt(1);
                }
            }

            String sqlDetalle = "INSERT INTO produccion_detalle (produccion_id, producto_id, cantidad_planificada) VALUES (?, ?, ?)";
            for (ProduccionDetalle detalle : detalles) {
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    ps.setInt(1, produccionId);
                    ps.setInt(2, detalle.getProductoId());
                    ps.setInt(3, detalle.getCantidadPlanificada());
                    ps.executeUpdate();
                }

                Map<Integer, BigDecimal> ingredientesPorUnidad = recetaDAO.obtenerCantidadesPorProducto(con, detalle.getProductoId());
                for (Map.Entry<Integer, BigDecimal> entry : ingredientesPorUnidad.entrySet()) {
                    int ingredienteId = entry.getKey();
                    BigDecimal cantidadNecesaria = entry.getValue().multiply(BigDecimal.valueOf(detalle.getCantidadPlanificada()));

                    BigDecimal stockAnterior = ingredienteDAO.obtenerStockActual(con, ingredienteId);
                    BigDecimal stockNuevo = stockAnterior.subtract(cantidadNecesaria);
                    if (stockNuevo.compareTo(BigDecimal.ZERO) < 0) {
                        throw new SQLException("Stock insuficiente del ingrediente id=" + ingredienteId
                                + " para producir " + detalle.getCantidadPlanificada() + " unidad(es).");
                    }
                    ingredienteDAO.fijarStock(con, ingredienteId, stockNuevo);

                    MovimientoIngrediente mov = new MovimientoIngrediente();
                    mov.setIngredienteId(ingredienteId);
                    mov.setTipo(TipoMovimiento.SALIDA);
                    mov.setMotivo(MotivoMovimientoIngrediente.PRODUCCION);
                    mov.setCantidad(cantidadNecesaria);
                    mov.setStockAnterior(stockAnterior);
                    mov.setStockNuevo(stockNuevo);
                    mov.setReferenciaTipo("PRODUCCION");
                    mov.setReferenciaId(produccionId);
                    mov.setUsuarioId(usuarioId);
                    movimientoIngredienteDAO.registrar(con, mov);
                }
            }

            con.commit();
            return buscarPorId(produccionId).orElseThrow();
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
     * Finaliza la orden: registra cuanto se produjo realmente de cada linea,
     * suma ese stock a los productos, y si la orden viene de un pedido,
     * actualiza el avance de ese pedido. cantidadesProducidas mapea el id
     * del detalle de produccion -> cantidad producida.
     */
    public void terminar(int produccionId, Map<Integer, Integer> cantidadesProducidas, int usuarioId) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            String sqlProd = "SELECT tipo, pedido_id, estado FROM producciones WHERE id = ? FOR UPDATE";
            String tipo;
            Integer pedidoId;
            try (PreparedStatement ps = con.prepareStatement(sqlProd)) {
                ps.setInt(1, produccionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Orden de produccion no encontrada id=" + produccionId);
                    }
                    String estadoActual = rs.getString("estado");
                    if ("TERMINADO".equals(estadoActual) || "CANCELADO".equals(estadoActual)) {
                        throw new SQLException("La orden #" + produccionId + " ya esta " + estadoActual.toLowerCase() + ".");
                    }
                    tipo = rs.getString("tipo");
                    int pedidoIdRaw = rs.getInt("pedido_id");
                    pedidoId = rs.wasNull() ? null : pedidoIdRaw;
                }
            }

            Map<Integer, Integer> producidoPorProducto = new HashMap<>();

            String sqlDetalles = "SELECT id, producto_id FROM produccion_detalle WHERE produccion_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
                ps.setInt(1, produccionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int detalleId = rs.getInt("id");
                        int productoId = rs.getInt("producto_id");
                        Integer cantidadProducida = cantidadesProducidas.get(detalleId);
                        if (cantidadProducida == null) {
                            throw new SQLException("Falta indicar la cantidad producida para el detalle id=" + detalleId);
                        }

                        try (PreparedStatement upd = con.prepareStatement(
                                "UPDATE produccion_detalle SET cantidad_producida = ? WHERE id = ?")) {
                            upd.setInt(1, cantidadProducida);
                            upd.setInt(2, detalleId);
                            upd.executeUpdate();
                        }

                        if (cantidadProducida > 0) {
                            int stockAnterior = productoDAO.obtenerStockActual(con, productoId);
                            productoDAO.restaurarStock(con, productoId, cantidadProducida);

                            MovimientoProducto mov = new MovimientoProducto();
                            mov.setProductoId(productoId);
                            mov.setTipo(TipoMovimiento.ENTRADA);
                            mov.setMotivo(MotivoMovimientoProducto.PRODUCCION);
                            mov.setCantidad(cantidadProducida);
                            mov.setStockAnterior(stockAnterior);
                            mov.setStockNuevo(stockAnterior + cantidadProducida);
                            mov.setReferenciaTipo("PRODUCCION");
                            mov.setReferenciaId(produccionId);
                            mov.setUsuarioId(usuarioId);
                            movimientoProductoDAO.registrar(con, mov);
                        }

                        producidoPorProducto.merge(productoId, cantidadProducida, Integer::sum);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE producciones SET estado = 'TERMINADO', fecha_fin = CURRENT_TIMESTAMP WHERE id = ?")) {
                ps.setInt(1, produccionId);
                ps.executeUpdate();
            }

            if ("PEDIDO".equals(tipo) && pedidoId != null) {
                pedidoDAO.aplicarProduccionAPedido(con, pedidoId, producidoPorProducto);
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

    /** Cancela la orden y devuelve al stock todos los ingredientes que se habian descontado al crearla. */
    public void cancelar(int produccionId, int usuarioId) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            String sqlEstado = "SELECT estado FROM producciones WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                ps.setInt(1, produccionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Orden de produccion no encontrada id=" + produccionId);
                    }
                    String estadoActual = rs.getString("estado");
                    if ("TERMINADO".equals(estadoActual) || "CANCELADO".equals(estadoActual)) {
                        throw new SQLException("La orden #" + produccionId + " ya esta " + estadoActual.toLowerCase() + ".");
                    }
                }
            }

            String sqlDetalles = "SELECT producto_id, cantidad_planificada FROM produccion_detalle WHERE produccion_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
                ps.setInt(1, produccionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int productoId = rs.getInt("producto_id");
                        int cantidadPlanificada = rs.getInt("cantidad_planificada");

                        Map<Integer, BigDecimal> ingredientesPorUnidad = recetaDAO.obtenerCantidadesPorProducto(con, productoId);
                        for (Map.Entry<Integer, BigDecimal> entry : ingredientesPorUnidad.entrySet()) {
                            int ingredienteId = entry.getKey();
                            BigDecimal cantidadADevolver = entry.getValue().multiply(BigDecimal.valueOf(cantidadPlanificada));

                            BigDecimal stockAnterior = ingredienteDAO.obtenerStockActual(con, ingredienteId);
                            BigDecimal stockNuevo = stockAnterior.add(cantidadADevolver);
                            ingredienteDAO.fijarStock(con, ingredienteId, stockNuevo);

                            MovimientoIngrediente mov = new MovimientoIngrediente();
                            mov.setIngredienteId(ingredienteId);
                            mov.setTipo(TipoMovimiento.ENTRADA);
                            mov.setMotivo(MotivoMovimientoIngrediente.CANCELACION);
                            mov.setCantidad(cantidadADevolver);
                            mov.setStockAnterior(stockAnterior);
                            mov.setStockNuevo(stockNuevo);
                            mov.setReferenciaTipo("PRODUCCION");
                            mov.setReferenciaId(produccionId);
                            mov.setUsuarioId(usuarioId);
                            movimientoIngredienteDAO.registrar(con, mov);
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE producciones SET estado = 'CANCELADO' WHERE id = ?")) {
                ps.setInt(1, produccionId);
                ps.executeUpdate();
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

    public List<Produccion> listarTodos() throws SQLException {
        String sql = "SELECT p.*, u.nombre AS usuario_nombre FROM producciones p "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id ORDER BY p.id DESC";
        List<Produccion> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCabecera(rs));
            }
        }
        return lista;
    }

    public Optional<Produccion> buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.*, u.nombre AS usuario_nombre FROM producciones p "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id WHERE p.id = ?";
        Produccion produccion;
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                produccion = mapearCabecera(rs);
            }
        }
        produccion.setDetalles(listarDetalles(id));
        return Optional.of(produccion);
    }

    private List<ProduccionDetalle> listarDetalles(int produccionId) throws SQLException {
        String sql = "SELECT pd.*, pr.nombre AS producto_nombre FROM produccion_detalle pd "
                + "JOIN productos pr ON pr.id = pd.producto_id WHERE pd.produccion_id = ? ORDER BY pd.id";
        List<ProduccionDetalle> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, produccionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProduccionDetalle d = new ProduccionDetalle();
                    d.setId(rs.getInt("id"));
                    d.setProduccionId(rs.getInt("produccion_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setCantidadPlanificada(rs.getInt("cantidad_planificada"));
                    d.setCantidadProducida(rs.getInt("cantidad_producida"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    private Produccion mapearCabecera(ResultSet rs) throws SQLException {
        Produccion p = new Produccion();
        p.setId(rs.getInt("id"));
        p.setTipo(TipoProduccion.valueOf(rs.getString("tipo")));
        int pedidoId = rs.getInt("pedido_id");
        p.setPedidoId(rs.wasNull() ? null : pedidoId);
        p.setUsuarioId(rs.getInt("usuario_id"));
        p.setUsuarioNombre(rs.getString("usuario_nombre"));
        p.setEstado(EstadoProduccion.valueOf(rs.getString("estado")));
        p.setObservacion(rs.getString("observacion"));
        Timestamp fechaInicio = rs.getTimestamp("fecha_inicio");
        if (fechaInicio != null) {
            p.setFechaInicio(fechaInicio.toLocalDateTime());
        }
        Timestamp fechaFin = rs.getTimestamp("fecha_fin");
        if (fechaFin != null) {
            p.setFechaFin(fechaFin.toLocalDateTime());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            p.setCreatedAt(created.toLocalDateTime());
        }
        return p;
    }
}
