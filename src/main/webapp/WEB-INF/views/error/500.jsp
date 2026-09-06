<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Error interno</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center vh-100 bg-light">
<div class="text-center" style="max-width:700px;">
    <h1 class="display-4">Ups, algo fallo</h1>
    <p class="lead">Ocurrio un error inesperado procesando tu solicitud.</p>
    <% if (exception != null) { %>
        <pre class="text-start bg-white border rounded p-3 small text-danger"><%= exception %></pre>
    <% } %>
    <a href="<%= request.getContextPath() %>/" class="btn btn-primary">Volver al inicio</a>
</div>
</body>
</html>
