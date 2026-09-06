<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="false" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>404 - No encontrado</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center vh-100 bg-light">
<div class="text-center">
    <h1 class="display-4">404</h1>
    <p class="lead">La pagina que buscas no existe.</p>
    <a href="<%= request.getContextPath() %>/" class="btn btn-primary">Volver al inicio</a>
</div>
</body>
</html>
