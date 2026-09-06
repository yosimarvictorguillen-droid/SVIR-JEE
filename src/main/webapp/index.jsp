<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Pagina de entrada: si ya hay sesion activa, va directo al dashboard;
    // si no, al login. No renderiza nada visible.
    if (session.getAttribute("usuario") != null) {
        response.sendRedirect(request.getContextPath() + "/app/dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/home");
    }
%>
