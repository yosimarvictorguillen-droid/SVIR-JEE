package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.DetallePedido;
import com.svir.jee.model.EstadoDetallePedido;
import com.svir.jee.model.EstadoPedido;
import com.svir.jee.model.FiltroPedido;
import com.svir.jee.model.MotivoMovimientoProducto;
import com.svir.jee.model.MovimientoProducto;
import com.svir.jee.model.Pedido;
import com.svir.jee.model.TipoMovimiento;
import com.svir.jee.model.TipoOrigenPedido;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de pedidos y su detalle. Concentra la logica transaccional
 * de venta: descuenta stock de producto y registra el movimiento de
 * auditoria dentro de la MISMA transaccion JDBC que crea el pedido, de modo
 * que si algo falla a mitad de camino, todo se revierte (rollback).
 */
public class PedidoDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoProductoDAO movimientoDAO = new MovimientoProductoDAO();

    /**
     * Crea un pedido con su detalle. Para cada linea, atiende de inmediato
     * lo que el stock actual permita (igual que el sistema original SVIR):
     * el total cobrado es por la cantidad solicitada completa, pero solo se
     * descuenta stock por la cantidad efectivamente atendida.
     */
    public Pedido crear(Pedido pedido, Integer usuarioId) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            String sqlPedido = "INSERT INTO pedidos (cliente_id, invitado_id, usuario_id, total, estado, tipo_origen, observacion, nombre_invitado) "
                    + "VALUES (?, ?, ?, 0, 'PENDIENTE', ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPedido, PreparedStatement.RETURN_GENERATED_KEYS)) {
                if (pedido.getClienteId() != null) {
                    ps.setInt(1, pedido.getClienteId());
                } else {
                    ps.setNull(1, Types.INTEGER);
                }
                if (pedido.getInvitadoId() != null) {
                    ps.setInt(2, pedido.getInvitadoId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                if (usuarioId != null) {
                    ps.setInt(3, usuarioId);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, pedido.getTipoOrigen().name());
                ps.setString(5, pedido.getObservacion());
                ps.setString(6, pedido.getNombreInvitado());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    pedido.setId(keys.getInt(1));
                }
            }

            BigDecimal total = BigDecimal.ZERO;
            boolean todoAtendido = true;

            String sqlDetalle = "INSERT INTO detalle_pedido "
                    + "(pedido_id, producto_id, cantidad, cantidad_atendida, precio_unitario, subtotal, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            for (DetallePedido linea : pedido.getDetalles()) {
                int stockActual = productoDAO.obtenerStockActual(con, linea.getProductoId());
                int cantidadAtendida = Math.min(linea.getCantidad(), stockActual);
                BigDecimal subtotal = linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()));

                EstadoDetallePedido estadoLinea;
                if (cantidadAtendida == 0) {
                    estadoLinea = EstadoDetallePedido.PENDIENTE;
                    todoAtendido = false;
                } else if (cantidadAtendida < linea.getCantidad()) {
                    estadoLinea = EstadoDetallePedido.PARCIAL;
                    todoAtendido = false;
                } else {
                    estadoLinea = EstadoDetallePedido.ATENDIDO;
                }

                try (PreparedStatement ps = con.prepareStatement(sqlDetalle, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pedido.getId());
                    ps.setInt(2, linea.getProductoId());
                    ps.setInt(3, linea.getCantidad());
                    ps.setInt(4, cantidadAtendida);
                    ps.setBigDecimal(5, linea.getPrecioUnitario());
                    ps.setBigDecimal(6, subtotal);
                    ps.setString(7, estadoLinea.name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        linea.setId(keys.getInt(1));
                    }
                }

                if (cantidadAtendida > 0) {
                    int stockAnterior = stockActual;
                    productoDAO.descontarStock(con, linea.getProductoId(), cantidadAtendida);
                    int stockNuevo = stockAnterior - cantidadAtendida;

                    MovimientoProducto mov = new MovimientoProducto();
                    mov.setProductoId(linea.getProductoId());
                    mov.setTipo(TipoMovimiento.SALIDA);
                    mov.setMotivo(MotivoMovimientoProducto.VENTA);
                    mov.setCantidad(cantidadAtendida);
                    mov.setStockAnterior(stockAnterior);
                    mov.setStockNuevo(stockNuevo);
                    mov.setReferenciaTipo("PEDIDO");
                    mov.setReferenciaId(pedido.getId());
                    mov.setUsuarioId(usuarioId);
                    movimientoDAO.registrar(con, mov);
                }

                linea.setCantidadAtendida(cantidadAtendida);
                linea.setSubtotal(subtotal);
                linea.setEstado(estadoLinea);
                total = total.add(subtotal);
            }

            EstadoPedido estadoPedido = todoAtendido ? EstadoPedido.LISTO : EstadoPedido.PREPARACION;
            String sqlUpdate = "UPDATE pedidos SET total = ?, estado = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setBigDecimal(1, total);
                ps.setString(2, estadoPedido.name());
                ps.setInt(3, pedido.getId());
                ps.executeUpdate();
            }
            pedido.setTotal(total);
            pedido.setEstado(estadoPedido);

            con.commit();
            return pedido;
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
     * Valida los datos de un comprobante antes de emitirlo. Devuelve el mensaje de error o null si es valido.
     * Boleta simple no lleva documento; boleta con DNI exige 8 digitos; factura exige RUC de 11 digitos.
     */
    public static String validarComprobante(String tipo, String documento) {
        String doc = documento == null ? "" : documento.trim();
        if ("BOLETA_SIMPLE".equals(tipo)) {
            return null;
        }
        if ("BOLETA_DNI".equals(tipo)) {
            return doc.matches("\\d{8}") ? null : "Para boleta con DNI escribe un DNI de 8 digitos.";
        }
        if ("FACTURA".equals(tipo)) {
            return doc.matches("\\d{11}") ? null : "Para factura escribe un RUC de 11 digitos.";
        }
        return "Elige el tipo de comprobante.";
    }

    /**
     * Emite el comprobante (boleta o factura) de un pedido ya existente y le asigna el siguiente
     * numero de su serie (B001 para boletas, F001 para facturas). Un pedido solo puede tener un
     * comprobante y no se emite para pedidos cancelados. Devuelve el numero asignado.
     */
    public String emitirComprobante(int pedidoId, String tipo, String documento) throws SQLException {
        String error = validarComprobante(tipo, documento);
        if (error != null) {
            throw new SQLException(error);
        }
        String doc = "BOLETA_SIMPLE".equals(tipo) || documento == null || documento.isBlank() ? null : documento.trim();
        String serie = "FACTURA".equals(tipo) ? "F001" : "B001";

        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado, comprobante_numero FROM pedidos WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, pedidoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Pedido no encontrado id=" + pedidoId);
                    }
                    if ("CANCELADO".equals(rs.getString("estado"))) {
                        throw new SQLException("No se puede emitir comprobante de un pedido cancelado.");
                    }
                    if (rs.getString("comprobante_numero") != null) {
                        throw new SQLException("Este pedido ya tiene el comprobante " + rs.getString("comprobante_numero") + ".");
                    }
                }
            }

            int siguiente;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COALESCE(MAX(CAST(SUBSTRING(comprobante_numero, 6) AS UNSIGNED)), 0) + 1 "
                            + "FROM pedidos WHERE comprobante_numero LIKE ?")) {
                ps.setString(1, serie + "-%");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    siguiente = rs.getInt(1);
                }
            }
            String numero = String.format("%s-%08d", serie, siguiente);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE pedidos SET comprobante_tipo = ?, comprobante_numero = ?, comprobante_documento = ?, "
                            + "comprobante_fecha = NOW() WHERE id = ?")) {
                ps.setString(1, tipo);
                ps.setString(2, numero);
                ps.setString(3, doc);
                ps.setInt(4, pedidoId);
                ps.executeUpdate();
            }

            con.commit();
            return numero;
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

    /** Cambia el estado del pedido sin validar la maquina de estados (igual que el sistema original). */
    public void cambiarEstado(int pedidoId, EstadoPedido nuevoEstado) throws SQLException {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        }
    }

    /**
     * Cancela un pedido: devuelve al stock de cada producto la cantidad que
     * habia sido atendida, registra el movimiento de reversa y marca el
     * pedido y sus lineas como CANCELADO. Todo en una sola transaccion.
     */
    public void cancelar(int pedidoId, Integer usuarioId) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            String sqlEstado = "SELECT estado FROM pedidos WHERE id = ? FOR UPDATE";
            String estadoActual;
            try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
                ps.setInt(1, pedidoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Pedido no encontrado id=" + pedidoId);
                    }
                    estadoActual = rs.getString("estado");
                }
            }
            if ("CANCELADO".equals(estadoActual) || "ENTREGADO".equals(estadoActual)) {
                throw new SQLException("No se puede cancelar un pedido en estado " + estadoActual);
            }

            String sqlDetalles = "SELECT id, producto_id, cantidad_atendida FROM detalle_pedido WHERE pedido_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
                ps.setInt(1, pedidoId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int detalleId = rs.getInt("id");
                        int productoId = rs.getInt("producto_id");
                        int cantidadAtendida = rs.getInt("cantidad_atendida");

                        if (cantidadAtendida > 0) {
                            int stockAnterior = productoDAO.obtenerStockActual(con, productoId);
                            productoDAO.restaurarStock(con, productoId, cantidadAtendida);

                            MovimientoProducto mov = new MovimientoProducto();
                            mov.setProductoId(productoId);
                            mov.setTipo(TipoMovimiento.ENTRADA);
                            mov.setMotivo(MotivoMovimientoProducto.CANCELACION);
                            mov.setCantidad(cantidadAtendida);
                            mov.setStockAnterior(stockAnterior);
                            mov.setStockNuevo(stockAnterior + cantidadAtendida);
                            mov.setReferenciaTipo("PEDIDO");
                            mov.setReferenciaId(pedidoId);
                            mov.setUsuarioId(usuarioId);
                            movimientoDAO.registrar(con, mov);
                        }

                        try (PreparedStatement upd = con.prepareStatement(
                                "UPDATE detalle_pedido SET cantidad_atendida = 0, estado = 'CANCELADO' WHERE id = ?")) {
                            upd.setInt(1, detalleId);
                            upd.executeUpdate();
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET estado = 'CANCELADO' WHERE id = ?")) {
                ps.setInt(1, pedidoId);
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

    // ---- busqueda con filtros (pantalla Pedidos) ----

    private static final String DESDE_PEDIDOS = "FROM pedidos p "
            + "LEFT JOIN clientes c ON c.id = p.cliente_id "
            + "LEFT JOIN usuarios u ON u.id = p.usuario_id ";

    /** Condiciones WHERE y sus parametros, siempre con PreparedStatement (nunca se concatena texto del usuario). */
    private static final class Condiciones {
        final StringBuilder sql = new StringBuilder("WHERE 1 = 1 ");
        final List<Object> params = new ArrayList<>();
    }

    private Condiciones condiciones(FiltroPedido f) {
        Condiciones w = new Condiciones();

        String texto = f.getTexto() == null ? "" : f.getTexto().trim().toLowerCase();
        if (!texto.isEmpty()) {
            String seguro = texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            String patron = "%" + seguro + "%";
            int id = texto.matches("\\d{1,9}") ? Integer.parseInt(texto) : -1;
            w.sql.append("AND (p.id = ? OR LOWER(p.nombre_invitado) LIKE ? OR LOWER(c.nombre) LIKE ? "
                    + "OR LOWER(p.comprobante_numero) LIKE ? OR p.observacion LIKE ? "
                    + "OR LOWER(c.dni) LIKE ? OR EXISTS (SELECT 1 FROM invitados iv WHERE iv.id = p.invitado_id AND LOWER(iv.documento) LIKE ?)) ");
            w.params.add(id);
            w.params.add(patron);
            w.params.add(patron);
            w.params.add(patron);
            w.params.add("%Tel: " + seguro + "%");
            w.params.add(patron);
            w.params.add(patron);
        }

        String estado = f.getEstado();
        if ("ACTIVOS".equals(estado)) {
            w.sql.append("AND p.estado <> 'CANCELADO' ");
        } else if ("ABIERTOS".equals(estado)) {
            w.sql.append("AND p.estado IN ('PENDIENTE','PREPARACION') ");
        } else if (estado != null && !estado.isEmpty()) {
            try {
                w.sql.append("AND p.estado = ? ");
                w.params.add(EstadoPedido.valueOf(estado).name());
            } catch (IllegalArgumentException e) {
                w.sql.setLength(w.sql.length() - "AND p.estado = ? ".length());
            }
        }

        String origen = f.getOrigen();
        if (origen != null && !origen.isEmpty()) {
            try {
                w.sql.append("AND p.tipo_origen = ? ");
                w.params.add(TipoOrigenPedido.valueOf(origen).name());
            } catch (IllegalArgumentException e) {
                w.sql.setLength(w.sql.length() - "AND p.tipo_origen = ? ".length());
            }
        }

        if ("Cliente".equals(f.getTipoCliente())) {
            w.sql.append("AND p.cliente_id IS NOT NULL ");
        } else if ("Invitado".equals(f.getTipoCliente())) {
            w.sql.append("AND p.cliente_id IS NULL ");
        }

        if ("CON".equals(f.getComprobante())) {
            w.sql.append("AND p.comprobante_numero IS NOT NULL ");
        } else if ("SIN".equals(f.getComprobante())) {
            w.sql.append("AND p.comprobante_numero IS NULL ");
        }

        if (f.getDesde() != null) {
            w.sql.append("AND DATE(p.created_at) >= ? ");
            w.params.add(f.getDesde());
        }
        if (f.getHasta() != null) {
            w.sql.append("AND DATE(p.created_at) <= ? ");
            w.params.add(f.getHasta());
        }
        return w;
    }

    /** Pedidos que cumplen el filtro, mas recientes primero. limite <= 0 devuelve todos. */
    public List<Pedido> buscar(FiltroPedido filtro, int limite, int offset) throws SQLException {
        Condiciones w = condiciones(filtro);
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + DESDE_PEDIDOS + w.sql + "ORDER BY p.id DESC" + (limite > 0 ? " LIMIT ? OFFSET ?" : "");
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (Object param : w.params) {
                ps.setObject(i++, param);
            }
            if (limite > 0) {
                ps.setInt(i++, limite);
                ps.setInt(i, offset);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCabecera(rs));
                }
            }
        }
        return lista;
    }

    public int contar(FiltroPedido filtro) throws SQLException {
        Condiciones w = condiciones(filtro);
        String sql = "SELECT COUNT(*) " + DESDE_PEDIDOS + w.sql;
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (Object param : w.params) {
                ps.setObject(i++, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "ORDER BY p.id DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCabecera(rs));
            }
        }
        return lista;
    }

    public List<Pedido> listarPorRangoFechas(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "WHERE DATE(p.created_at) BETWEEN ? AND ? AND p.estado <> 'CANCELADO' "
                + "ORDER BY p.created_at DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCabecera(rs));
                }
            }
        }
        return lista;
    }

    /** Pedidos activos (no cancelados/entregados) que tienen al menos una linea sin atender del todo. */
    public List<Pedido> listarConFaltante() throws SQLException {
        String sql = "SELECT DISTINCT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "JOIN detalle_pedido d ON d.pedido_id = p.id AND d.cantidad_atendida < d.cantidad "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "WHERE p.estado NOT IN ('CANCELADO','ENTREGADO') "
                + "ORDER BY p.id DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCabecera(rs));
            }
        }
        return lista;
    }

    /** Pedidos con tipo_origen DELIVERY activos o entregados hoy (para el panel de repartidor). */
    public List<Pedido> listarDelivery() throws SQLException {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "WHERE p.tipo_origen = 'DELIVERY' AND ("
                + "  p.estado IN ('LISTO','EN_CAMINO') "
                + "  OR (p.estado = 'ENTREGADO' AND DATE(p.updated_at) = CURDATE())"
                + ") ORDER BY p.created_at ASC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearCabecera(rs));
            }
        }
        return lista;
    }

    /** Historial de pedidos de un cliente registrado, mas reciente primero. */
    public List<Pedido> listarPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "WHERE p.cliente_id = ? ORDER BY p.created_at DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCabecera(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busqueda publica de la tienda: por nombre (del cliente con cuenta o del invitado) y, si el texto
     * son 7 o mas digitos, tambien por telefono. Maximo 20 resultados, mas recientes primero.
     * Las cabeceras devueltas no se usan para mostrar direcciones ni telefonos.
     */
    public List<Pedido> buscarPorTexto(String texto) throws SQLException {
        String limpio = texto.trim().toLowerCase();
        boolean esTelefono = limpio.matches("\\d{7,}");
        // se escapan los comodines de LIKE para que "%%%" o "___" no listen todos los pedidos
        String seguro = limpio.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "WHERE " + (esTelefono
                        ? "p.observacion LIKE ? "
                        : "(LOWER(p.nombre_invitado) LIKE ? OR LOWER(c.nombre) LIKE ?) ")
                + "ORDER BY p.created_at DESC LIMIT 20";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (esTelefono) {
                ps.setString(1, "%Tel: " + seguro + "%");
            } else {
                String patron = "%" + seguro + "%";
                ps.setString(1, patron);
                ps.setString(2, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCabecera(rs));
                }
            }
        }
        return lista;
    }

    public Optional<Pedido> buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.*, c.nombre AS cliente_nombre, c.dni AS cliente_documento, (SELECT iv.documento FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_documento, c.telefono AS cliente_telefono, (SELECT iv.telefono FROM invitados iv WHERE iv.id = p.invitado_id) AS invitado_telefono, u.nombre AS usuario_nombre "
                + "FROM pedidos p "
                + "LEFT JOIN clientes c ON c.id = p.cliente_id "
                + "LEFT JOIN usuarios u ON u.id = p.usuario_id "
                + "WHERE p.id = ?";
        Pedido pedido;
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                pedido = mapearCabecera(rs);
            }
        }
        pedido.setDetalles(listarDetalles(id));
        return Optional.of(pedido);
    }

    /**
     * Aplica lo producido en una orden de produccion al pedido que la origino:
     * suma la cantidad producida (por producto) a cantidad_atendida de cada
     * linea, recalcula su estado, y recalcula el estado del pedido. Se
     * ejecuta dentro de la transaccion ya abierta por ProduccionDAO.terminar.
     */
    public void aplicarProduccionAPedido(Connection con, int pedidoId,
                                          java.util.Map<Integer, Integer> cantidadProducidaPorProducto) throws SQLException {
        String sqlEstado = "SELECT estado FROM pedidos WHERE id = ? FOR UPDATE";
        String estadoActual;
        try (PreparedStatement ps = con.prepareStatement(sqlEstado)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Pedido no encontrado id=" + pedidoId);
                }
                estadoActual = rs.getString("estado");
            }
        }
        if ("CANCELADO".equals(estadoActual) || "ENTREGADO".equals(estadoActual)) {
            return;
        }

        boolean todoAtendido = true;
        String sqlDetalles = "SELECT id, producto_id, cantidad, cantidad_atendida FROM detalle_pedido WHERE pedido_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int detalleId = rs.getInt("id");
                    int productoId = rs.getInt("producto_id");
                    int cantidad = rs.getInt("cantidad");
                    int cantidadAtendida = rs.getInt("cantidad_atendida");

                    Integer producido = cantidadProducidaPorProducto.get(productoId);
                    if (producido != null && producido > 0) {
                        int nuevaAtendida = Math.min(cantidad, cantidadAtendida + producido);
                        EstadoDetallePedido nuevoEstado = nuevaAtendida == 0 ? EstadoDetallePedido.PENDIENTE
                                : nuevaAtendida < cantidad ? EstadoDetallePedido.PARCIAL
                                : EstadoDetallePedido.ATENDIDO;
                        try (PreparedStatement upd = con.prepareStatement(
                                "UPDATE detalle_pedido SET cantidad_atendida = ?, estado = ? WHERE id = ?")) {
                            upd.setInt(1, nuevaAtendida);
                            upd.setString(2, nuevoEstado.name());
                            upd.setInt(3, detalleId);
                            upd.executeUpdate();
                        }
                        cantidadAtendida = nuevaAtendida;
                    }
                    if (cantidadAtendida < cantidad) {
                        todoAtendido = false;
                    }
                }
            }
        }

        EstadoPedido nuevoEstadoPedido = todoAtendido ? EstadoPedido.LISTO : EstadoPedido.PREPARACION;
        try (PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET estado = ? WHERE id = ?")) {
            ps.setString(1, nuevoEstadoPedido.name());
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        }
    }

    private List<DetallePedido> listarDetalles(int pedidoId) throws SQLException {
        String sql = "SELECT d.*, pr.nombre AS producto_nombre FROM detalle_pedido d "
                + "JOIN productos pr ON pr.id = d.producto_id "
                + "WHERE d.pedido_id = ? ORDER BY d.id";
        List<DetallePedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido d = new DetallePedido();
                    d.setId(rs.getInt("id"));
                    d.setPedidoId(rs.getInt("pedido_id"));
                    d.setProductoId(rs.getInt("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setCantidadAtendida(rs.getInt("cantidad_atendida"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setEstado(EstadoDetallePedido.valueOf(rs.getString("estado")));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        d.setCreatedAt(created.toLocalDateTime());
                    }
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    private Pedido mapearCabecera(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        int clienteId = rs.getInt("cliente_id");
        p.setClienteId(rs.wasNull() ? null : clienteId);
        p.setClienteNombre(rs.getString("cliente_nombre"));
        p.setNombreInvitado(rs.getString("nombre_invitado"));
        int invitadoId = rs.getInt("invitado_id");
        p.setInvitadoId(rs.wasNull() ? null : invitadoId);
        p.setClienteDocumento(rs.getString("cliente_documento"));
        p.setInvitadoDocumento(rs.getString("invitado_documento"));
        p.setClienteTelefono(rs.getString("cliente_telefono"));
        p.setInvitadoTelefono(rs.getString("invitado_telefono"));
        int usuarioId = rs.getInt("usuario_id");
        p.setUsuarioId(rs.wasNull() ? null : usuarioId);
        p.setUsuarioNombre(rs.getString("usuario_nombre"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setEstado(EstadoPedido.valueOf(rs.getString("estado")));
        p.setTipoOrigen(TipoOrigenPedido.valueOf(rs.getString("tipo_origen")));
        p.setObservacion(rs.getString("observacion"));
        p.setComprobanteTipo(rs.getString("comprobante_tipo"));
        p.setComprobanteNumero(rs.getString("comprobante_numero"));
        p.setComprobanteDocumento(rs.getString("comprobante_documento"));
        Timestamp compFecha = rs.getTimestamp("comprobante_fecha");
        if (compFecha != null) {
            p.setComprobanteFecha(compFecha.toLocalDateTime());
        }
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
