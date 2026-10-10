package com.svir.jee.bean;

import com.svir.jee.dao.IngredienteDAO;
import com.svir.jee.model.Ingrediente;
import com.svir.jee.model.MotivoMovimientoIngrediente;
import com.svir.jee.model.TipoMovimiento;
import com.svir.jee.model.Usuario;

import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Managed Bean del modulo Ingredientes: CRUD en modal y registro de movimientos de stock. */
@Named
@ViewScoped
public class IngredienteBean extends ListadoBase<Ingrediente> {

    private static final long serialVersionUID = 1L;
    private static final IngredienteDAO DAO = new IngredienteDAO();

    private boolean editando;
    private Ingrediente ingrediente;

    private boolean registrandoMovimiento;
    private Ingrediente ingredienteMovimiento;
    private TipoMovimiento tipo = TipoMovimiento.ENTRADA;
    private MotivoMovimientoIngrediente motivo = MotivoMovimientoIngrediente.COMPRA;
    private BigDecimal cantidad;

    @Override
    protected List<Ingrediente> cargar() throws SQLException {
        return DAO.listarTodos();
    }

    @Override
    protected String textoBusqueda(Ingrediente i) {
        return texto(i.getNombre()) + " " + texto(i.getUnidadMedida());
    }

    // ---- alta / edicion ----

    public void nuevo() {
        Formularios.resetear("formIngrediente");
        ingrediente = new Ingrediente();
        ingrediente.setActivo(true);
        ingrediente.setStock(BigDecimal.ZERO);
        ingrediente.setStockMinimo(BigDecimal.ZERO);
        editando = true;
    }

    public void editar(Ingrediente i) {
        Formularios.resetear("formIngrediente");
        Ingrediente copia = new Ingrediente();
        copia.setId(i.getId());
        copia.setNombre(i.getNombre());
        copia.setUnidadMedida(i.getUnidadMedida());
        copia.setStock(i.getStock());
        copia.setStockMinimo(i.getStockMinimo());
        copia.setActivo(i.isActivo());
        ingrediente = copia;
        editando = true;
    }

    public void cancelar() {
        editando = false;
        ingrediente = null;
    }

    public void guardar() {
        try {
            boolean esNuevo = ingrediente.getId() == null;
            if (esNuevo) {
                DAO.crear(ingrediente);
            } else {
                DAO.actualizar(ingrediente);
            }
            Mensajes.info("Ingrediente \"" + ingrediente.getNombre() + "\" " + (esNuevo ? "creado" : "actualizado") + " correctamente.");
            editando = false;
            ingrediente = null;
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo guardar el ingrediente.");
        }
    }

    public void alternarActivo(Ingrediente i) {
        try {
            DAO.cambiarActivo(i.getId(), !i.isActivo());
            Mensajes.info("Ingrediente \"" + i.getNombre() + "\" " + (i.isActivo() ? "desactivado" : "activado") + ".");
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo cambiar el estado del ingrediente.");
        }
    }

    // ---- movimientos de stock (entrada / salida / ajuste) ----

    public void abrirMovimiento(Ingrediente i) {
        Formularios.resetear("formMovimiento");
        ingredienteMovimiento = i;
        tipo = TipoMovimiento.ENTRADA;
        motivo = MotivoMovimientoIngrediente.COMPRA;
        cantidad = null;
        registrandoMovimiento = true;
    }

    public void cancelarMovimiento() {
        registrandoMovimiento = false;
        ingredienteMovimiento = null;
    }

    public void registrarMovimiento() {
        Usuario usuario = (Usuario) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("usuario");
        try {
            DAO.registrarMovimiento(ingredienteMovimiento.getId(), tipo, motivo, cantidad, usuario.getId());
            Mensajes.info("Movimiento registrado para \"" + ingredienteMovimiento.getNombre() + "\".");
            registrandoMovimiento = false;
            ingredienteMovimiento = null;
            recargar();
        } catch (SQLException e) {
            Mensajes.error("No se pudo registrar el movimiento: " + e.getMessage());
        }
    }

    public TipoMovimiento[] getTipos() { return TipoMovimiento.values(); }
    public MotivoMovimientoIngrediente[] getMotivos() { return MotivoMovimientoIngrediente.values(); }

    public boolean isEditando() { return editando; }
    public Ingrediente getIngrediente() { return ingrediente; }
    public boolean isRegistrandoMovimiento() { return registrandoMovimiento; }
    public Ingrediente getIngredienteMovimiento() { return ingredienteMovimiento; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
    public MotivoMovimientoIngrediente getMotivo() { return motivo; }
    public void setMotivo(MotivoMovimientoIngrediente motivo) { this.motivo = motivo; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
