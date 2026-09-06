package com.svir.jee.servlet;

import com.svir.jee.dao.ReporteDAO;
import com.svir.jee.model.DashboardResumen;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/app/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            DashboardResumen resumen = reporteDAO.resumenDashboard();
            req.setAttribute("resumen", resumen);
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al calcular el resumen del dashboard", e);
        }
    }
}
