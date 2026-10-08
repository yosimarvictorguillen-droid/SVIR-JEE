package com.svir.jee.bean;

import com.svir.jee.dao.ReporteDAO;
import com.svir.jee.model.DashboardResumen;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.sql.SQLException;

/** Managed Bean del dashboard: expone el resumen calculado por ReporteDAO (JDBC). */
@Named
@RequestScoped
public class DashboardBean {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private DashboardResumen resumen;

    public DashboardResumen getResumen() throws SQLException {
        if (resumen == null) {
            resumen = reporteDAO.resumenDashboard();
        }
        return resumen;
    }
}
