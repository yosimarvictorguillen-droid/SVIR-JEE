package com.svir.jee.util;

import com.svir.jee.dao.ClienteDAO;
import com.svir.jee.dao.InvitadoDAO;
import com.svir.jee.model.Cliente;
import com.svir.jee.model.Invitado;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Identifica a quien compra a partir de su documento (unico en todo el sistema): si ya es cliente registrado se usa
 * su cuenta; si ya habia comprado como invitado se reutiliza su registro; si no, se crea el invitado.
 */
public final class Compradores {

    private static final ClienteDAO CLIENTES = new ClienteDAO();
    private static final InvitadoDAO INVITADOS = new InvitadoDAO();

    private Compradores() {
    }

    /** Error de datos que se puede mostrar tal cual a la persona. */
    public static class ValidacionException extends Exception {
        private static final long serialVersionUID = 1L;

        public ValidacionException(String mensaje) {
            super(mensaje);
        }
    }

    /** El documento ya pertenece a un cliente registrado (en la tienda se le pide iniciar sesion). */
    public static class ClienteExistenteException extends ValidacionException {
        private static final long serialVersionUID = 1L;

        public ClienteExistenteException() {
            super("Ese documento ya tiene una cuenta. Inicia sesion para continuar.");
        }
    }

    /** Resultado: o es un cliente registrado (clienteId) o un invitado (invitadoId). */
    public static final class Comprador {
        public final Integer clienteId;
        public final Integer invitadoId;
        public final String nombre;

        Comprador(Integer clienteId, Integer invitadoId, String nombre) {
            this.clienteId = clienteId;
            this.invitadoId = invitadoId;
            this.nombre = nombre;
        }
    }

    /**
     * @param permitirCliente true en las pantallas del personal (si el documento es de un cliente se usa su cuenta);
     *                        false en la tienda (el cliente debe iniciar sesion).
     */
    public static Comprador resolver(String tipoDocumento, String documento, String nombre, String telefono,
                                     boolean permitirCliente) throws SQLException, ValidacionException {
        String tipo = Documentos.tipoValido(tipoDocumento);
        String doc = Documentos.normalizar(documento);
        String error = Documentos.validar(tipo, doc);
        if (error != null) {
            throw new ValidacionException(error);
        }

        Optional<Cliente> cliente = CLIENTES.buscarPorDni(doc);
        if (cliente.isPresent()) {
            if (!permitirCliente) {
                throw new ClienteExistenteException();
            }
            return new Comprador(cliente.get().getId(), null, cliente.get().getNombre());
        }

        String nom = Textos.limpiar(nombre, 150);
        if (nom.isEmpty()) {
            throw new ValidacionException("El nombre del comprador es obligatorio.");
        }
        String tel = Textos.limpiar(telefono, 30);

        Optional<Invitado> existente = INVITADOS.buscarPorDocumento(doc);
        if (existente.isPresent()) {
            INVITADOS.actualizarContacto(existente.get().getId(), nom, tel.isEmpty() ? null : tel);
            return new Comprador(null, existente.get().getId(), nom);
        }

        Invitado nuevo = new Invitado();
        nuevo.setTipoDocumento(tipo);
        nuevo.setDocumento(doc);
        nuevo.setNombre(nom);
        nuevo.setTelefono(tel.isEmpty() ? null : tel);
        INVITADOS.crear(nuevo);
        return new Comprador(null, nuevo.getId(), nom);
    }
}
