package com.svir.jee.dao;

import com.svir.jee.db.ConexionBD;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.Invitado;
import com.svir.jee.util.Textos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a datos de invitados (compradores sin cuenta) y de su paso a cliente. JDBC puro. */
public class InvitadoDAO {

    public Optional<Invitado> buscarPorDocumento(String documento) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            return buscarPorDocumento(con, documento, false);
        }
    }

    /** Con {@code bloquear} la fila queda bloqueada hasta el commit (se usa dentro de la transaccion de conversion). */
    public Optional<Invitado> buscarPorDocumento(Connection con, String documento, boolean bloquear) throws SQLException {
        String sql = "SELECT i.*, NULL AS cliente_nombre FROM invitados i WHERE i.documento = ?" + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Invitado crear(Invitado i) throws SQLException {
        String sql = "INSERT INTO invitados (tipo_documento, documento, nombre, telefono) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getTipoDocumento());
            ps.setString(2, i.getDocumento());
            ps.setString(3, i.getNombre());
            ps.setString(4, i.getTelefono());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    i.setId(keys.getInt(1));
                }
            }
        }
        return i;
    }

    /** Actualiza el nombre con el ultimo escrito y el telefono solo si se informo uno. */
    public void actualizarContacto(int id, String nombre, String telefono) throws SQLException {
        String sql = "UPDATE invitados SET nombre = ?, telefono = COALESCE(?, telefono) WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, telefono);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public int contarPedidos(int invitadoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE invitado_id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, invitadoId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Comprueba que el pedido es de ese invitado y que el total coincide (prueba de que conoce su compra). */
    public boolean verificarPedido(int invitadoId, int pedidoId, BigDecimal total) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos WHERE id = ? AND invitado_id = ? AND ABS(total - ?) < 0.01";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ps.setInt(2, invitadoId);
            ps.setBigDecimal(3, total);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Tras dar de alta un cliente (misma transaccion): si su documento era el de un invitado, vincula sus pedidos
     * al nuevo cliente, marca al invitado como CONVERTIDO y deja el registro de trazabilidad.
     * Devuelve cuantos pedidos se vincularon (0 si no habia invitado con ese documento).
     */
    public int vincularTrasAlta(Connection con, Cliente cliente, String validacion) throws SQLException {
        Optional<Invitado> invitado = buscarPorDocumento(con, cliente.getDni(), true);
        if (invitado.isEmpty() || invitado.get().isConvertido()) {
            return 0;
        }
        int invitadoId = invitado.get().getId();
        int vinculados;
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE pedidos SET cliente_id = ? WHERE invitado_id = ? AND cliente_id IS NULL")) {
            ps.setInt(1, cliente.getId());
            ps.setInt(2, invitadoId);
            vinculados = ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE invitados SET estado = 'CONVERTIDO', cliente_id = ?, fecha_conversion = NOW() WHERE id = ?")) {
            ps.setInt(1, cliente.getId());
            ps.setInt(2, invitadoId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO conversiones_invitado (invitado_id, cliente_id, pedidos_vinculados, validacion) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, invitadoId);
            ps.setInt(2, cliente.getId());
            ps.setInt(3, vinculados);
            ps.setString(4, validacion);
            ps.executeUpdate();
        }
        return vinculados;
    }

    /** Todos los invitados con su resumen de compras (para la pantalla Invitados). */
    public List<Invitado> listarConResumen() throws SQLException {
        String sql = "SELECT i.*, c.nombre AS cliente_nombre, "
                + "(SELECT COUNT(*) FROM pedidos p WHERE p.invitado_id = i.id) AS total_pedidos, "
                + "(SELECT COALESCE(SUM(p.total), 0) FROM pedidos p WHERE p.invitado_id = i.id AND p.estado <> 'CANCELADO') AS total_comprado, "
                + "(SELECT MAX(p.created_at) FROM pedidos p WHERE p.invitado_id = i.id) AS ultimo_pedido, "
                + "(SELECT cv.pedidos_vinculados FROM conversiones_invitado cv WHERE cv.invitado_id = i.id ORDER BY cv.id DESC LIMIT 1) AS pedidos_vinculados "
                + "FROM invitados i LEFT JOIN clientes c ON c.id = i.cliente_id ORDER BY i.created_at DESC, i.id DESC";
        List<Invitado> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Invitado i = mapear(rs);
                i.setTotalPedidos(rs.getInt("total_pedidos"));
                i.setTotalComprado(rs.getBigDecimal("total_comprado"));
                Timestamp ultimo = rs.getTimestamp("ultimo_pedido");
                if (ultimo != null) {
                    i.setUltimoPedido(ultimo.toLocalDateTime());
                }
                int vinculados = rs.getInt("pedidos_vinculados");
                i.setPedidosVinculados(rs.wasNull() ? null : vinculados);
                lista.add(i);
            }
        }
        lista.addAll(listarAntiguos());
        return lista;
    }

    /**
     * Compradores de antes de pedir documento: pedidos sin cliente ni invitado registrado, agrupados por el nombre
     * que escribieron. Si hay un cliente activo con ese mismo nombre se sugiere vincularlos (lo confirma el personal).
     */
    private List<Invitado> listarAntiguos() throws SQLException {
        String sql = "SELECT p.nombre_invitado AS nombre, COUNT(*) AS total_pedidos, "
                + "COALESCE(SUM(CASE WHEN p.estado <> 'CANCELADO' THEN p.total ELSE 0 END), 0) AS total_comprado, "
                + "MAX(p.created_at) AS ultimo_pedido "
                + "FROM pedidos p WHERE p.invitado_id IS NULL AND p.cliente_id IS NULL "
                + "GROUP BY p.nombre_invitado ORDER BY ultimo_pedido DESC";
        List<Cliente> clientes = new ClienteDAO().listarActivos();
        List<Invitado> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Invitado i = new Invitado();
                String nombre = rs.getString("nombre");
                i.setSinDocumento(true);
                i.setNombre(nombre == null || nombre.isBlank() ? "Sin nombre" : nombre);
                i.setTotalPedidos(rs.getInt("total_pedidos"));
                i.setTotalComprado(rs.getBigDecimal("total_comprado"));
                Timestamp ultimo = rs.getTimestamp("ultimo_pedido");
                if (ultimo != null) {
                    i.setUltimoPedido(ultimo.toLocalDateTime());
                }
                if (nombre != null && !nombre.isBlank()) {
                    List<Cliente> coinciden = clientes.stream()
                            .filter(c -> c.getDni() != null && Textos.mismoNombre(c.getNombre(), nombre)).toList();
                    if (coinciden.size() == 1) {
                        i.setClienteSugeridoId(coinciden.get(0).getId());
                        i.setClienteSugeridoNombre(coinciden.get(0).getNombre());
                    }
                }
                lista.add(i);
            }
        }
        return lista;
    }

    /**
     * El personal confirma que los pedidos antiguos de un nombre (sin documento) son de un cliente: se vinculan a su
     * cuenta y queda el registro de conversion con el documento del cliente. Devuelve cuantos pedidos se vincularon.
     */
    public int vincularAntiguo(String nombre, int clienteId) throws SQLException {
        Connection con = null;
        try {
            con = ConexionBD.getInstancia().obtenerConexion();
            con.setAutoCommit(false);

            String tipo;
            String documento;
            try (PreparedStatement ps = con.prepareStatement("SELECT tipo_documento, dni FROM clientes WHERE id = ?")) {
                ps.setInt(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getString("dni") == null) {
                        throw new SQLException("El cliente no tiene documento registrado.");
                    }
                    tipo = rs.getString("tipo_documento");
                    documento = rs.getString("dni");
                }
            }

            Optional<Invitado> existente = buscarPorDocumento(con, documento, true);
            int invitadoId;
            if (existente.isPresent()) {
                if (!existente.get().isConvertido()) {
                    throw new SQLException("Ese documento ya es de un invitado con pedidos; se vincula desde el registro del cliente.");
                }
                invitadoId = existente.get().getId();
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO invitados (tipo_documento, documento, nombre, estado, cliente_id, fecha_conversion) "
                                + "VALUES (?, ?, ?, 'CONVERTIDO', ?, NOW())", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, tipo);
                    ps.setString(2, documento);
                    ps.setString(3, nombre);
                    ps.setInt(4, clienteId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        invitadoId = keys.getInt(1);
                    }
                }
            }

            int vinculados;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE pedidos SET invitado_id = ?, cliente_id = ? "
                            + "WHERE invitado_id IS NULL AND cliente_id IS NULL AND nombre_invitado = ?")) {
                ps.setInt(1, invitadoId);
                ps.setInt(2, clienteId);
                ps.setString(3, nombre);
                vinculados = ps.executeUpdate();
            }
            if (vinculados == 0) {
                throw new SQLException("No hay pedidos antiguos con ese nombre.");
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO conversiones_invitado (invitado_id, cliente_id, pedidos_vinculados, validacion) "
                            + "VALUES (?, ?, ?, 'PERSONAL_NOMBRE')")) {
                ps.setInt(1, invitadoId);
                ps.setInt(2, clienteId);
                ps.setInt(3, vinculados);
                ps.executeUpdate();
            }
            con.commit();
            return vinculados;
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

    private Invitado mapear(ResultSet rs) throws SQLException {
        Invitado i = new Invitado();
        i.setId(rs.getInt("id"));
        i.setTipoDocumento(rs.getString("tipo_documento"));
        i.setDocumento(rs.getString("documento"));
        i.setNombre(rs.getString("nombre"));
        i.setTelefono(rs.getString("telefono"));
        i.setEstado(rs.getString("estado"));
        int clienteId = rs.getInt("cliente_id");
        i.setClienteId(rs.wasNull() ? null : clienteId);
        i.setClienteNombre(rs.getString("cliente_nombre"));
        Timestamp conversion = rs.getTimestamp("fecha_conversion");
        if (conversion != null) {
            i.setFechaConversion(conversion.toLocalDateTime());
        }
        Timestamp creado = rs.getTimestamp("created_at");
        if (creado != null) {
            i.setCreatedAt(creado.toLocalDateTime());
        }
        return i;
    }
}
