<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    if (error == null && "1".equals(request.getParameter("expirado"))) {
        error = "Tu sesion expiro o no has iniciado sesion. Ingresa de nuevo.";
    }
    String emailPrevio = (String) request.getAttribute("email");
    if (emailPrevio == null) emailPrevio = "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ingresar - SVIR-JEE</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="<%= ctx %>/assets/css/styles.css?v=<%= application.getAttribute("iniciadoEn") %>" rel="stylesheet">
</head>
<body>
<div class="login-wrapper">
    <div class="login-shell">
        <div class="login-brand">
            <div class="login-logo">DM</div>
            <h1>Dulce Momento</h1>
            <p class="opacity-75 mb-0">Sistema integral para reposteria artesanal.</p>

            <ul class="login-pills">
                <li><i class="bi bi-shop"></i> Punto de venta presencial</li>
                <li><i class="bi bi-box-seam"></i> Control de stock en tiempo real</li>
                <li><i class="bi bi-fire"></i> Gestion de produccion y recetas</li>
                <li><i class="bi bi-bicycle"></i> Delivery y repartidores</li>
                <li><i class="bi bi-bar-chart-line"></i> Dashboard y reportes</li>
            </ul>

            <div class="login-footnote">
                <i class="bi bi-shield-lock"></i> Acceso exclusivo para personal autorizado
            </div>
        </div>

        <div class="login-form-side">
            <span class="login-badge"><i class="bi bi-lock-fill"></i> Panel de gestion</span>
            <h2>Bienvenido</h2>
            <p class="text-muted mb-4">Ingresa tus credenciales para continuar.</p>

            <% if (error != null) { %>
                <div class="alert alert-danger py-2"><%= error %></div>
            <% } %>

            <form method="post" action="<%= ctx %>/login" id="formLogin">
                <div class="mb-3">
                    <label class="form-label">Correo electronico</label>
                    <div class="input-icon-group">
                        <i class="bi bi-envelope"></i>
                        <input type="email" class="form-control" name="email" id="campoEmail"
                               placeholder="usuario@email.com" value="<%= emailPrevio %>" required autofocus>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">Contrasena</label>
                    <div class="input-icon-group">
                        <i class="bi bi-lock"></i>
                        <input type="password" class="form-control" name="password" id="campoPassword" required>
                        <button type="button" class="toggle-password" onclick="alternarPassword()">
                        <i class="bi bi-eye" id="iconoOjo"></i>
                        </button>
                    </div>
                </div>
                <div class="form-check mb-3">
                    <input type="checkbox" class="form-check-input" id="recordarCorreo">
                    <label class="form-check-label small" for="recordarCorreo">Recordar mi correo</label>
                </div>

                <button type="submit" class="btn btn-amber w-100 py-2">Ingresar</button>

                <div class="text-center mt-3">
                    <a href="#" class="small" onclick="event.preventDefault(); alert('Contacta a un administrador del sistema para restablecer tu contrasena.');">
                        ¿Olvidaste tu contrasena?
                    </a>
                </div>

                <a href="<%= ctx %>/home" class="btn btn-outline-secondary w-100 mt-2">
                    <i class="bi bi-shop"></i> Ver tienda web
                </a>
            </form>

            <p class="text-center text-muted mt-4 mb-0" style="font-size:.78rem;">
                <i class="bi bi-shield-check"></i> Conexion segura &middot; SVIR-JEE v1.0
            </p>
        </div>
    </div>
</div>

<script>
    function alternarPassword() {
        const campo = document.getElementById("campoPassword");
        const icono = document.getElementById("iconoOjo");
        const esPassword = campo.type === "password";
        campo.type = esPassword ? "text" : "password";
        icono.className = esPassword ? "bi bi-eye-slash" : "bi bi-eye";
    }

    // Recordar correo (solo en este navegador, via localStorage).
    (function () {
        const campoEmail = document.getElementById("campoEmail");
        const checkRecordar = document.getElementById("recordarCorreo");
        try {
            const guardado = localStorage.getItem("svirjeeEmail");
            if (guardado && !campoEmail.value) {
                campoEmail.value = guardado;
                checkRecordar.checked = true;
            }
        } catch (e) { /* localStorage no disponible: se ignora */ }

        document.getElementById("formLogin").addEventListener("submit", function () {
            try {
                if (checkRecordar.checked) {
                    localStorage.setItem("svirjeeEmail", campoEmail.value);
                } else {
                    localStorage.removeItem("svirjeeEmail");
                }
            } catch (e) { /* localStorage no disponible: se ignora */ }
        });
    })();
</script>
</body>
</html>
