package com.svir.jee.filter;

import com.svir.jee.model.RolUsuario;
import com.svir.jee.model.Usuario;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Controla acceso a los modulos internos (/app/*): exige sesion iniciada y,
 * ademas, valida que el rol del usuario tenga permiso sobre el modulo
 * solicitado (misma logica de roles que el SVIR original: ADMIN, VENTAS,
 * COCINA, REPARTIDOR).
 */
@WebFilter("/app/*")
public class AuthFilter implements Filter {

    private static final Map<String, Set<RolUsuario>> PERMISOS = new java.util.HashMap<>();

    static {
        PERMISOS.put("/app/dashboard", EnumSet.of(RolUsuario.ADMIN, RolUsuario.VENTAS, RolUsuario.COCINA, RolUsuario.REPARTIDOR));
        PERMISOS.put("/app/productos", EnumSet.of(RolUsuario.ADMIN, RolUsuario.VENTAS));
        PERMISOS.put("/app/ingredientes", EnumSet.of(RolUsuario.ADMIN, RolUsuario.COCINA));
        PERMISOS.put("/app/recetas", EnumSet.of(RolUsuario.ADMIN, RolUsuario.COCINA));
        PERMISOS.put("/app/producciones", EnumSet.of(RolUsuario.ADMIN, RolUsuario.COCINA));
        PERMISOS.put("/app/clientes", EnumSet.of(RolUsuario.ADMIN, RolUsuario.VENTAS));
        PERMISOS.put("/app/pedidos", EnumSet.of(RolUsuario.ADMIN, RolUsuario.VENTAS));
        PERMISOS.put("/app/pos", EnumSet.of(RolUsuario.ADMIN, RolUsuario.VENTAS));
        PERMISOS.put("/app/repartidor", EnumSet.of(RolUsuario.ADMIN, RolUsuario.VENTAS, RolUsuario.REPARTIDOR));
        PERMISOS.put("/app/movimientos", EnumSet.of(RolUsuario.ADMIN));
        PERMISOS.put("/app/usuarios", EnumSet.of(RolUsuario.ADMIN));
        PERMISOS.put("/app/reportes", EnumSet.of(RolUsuario.ADMIN));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Usuario usuario = session != null ? (Usuario) session.getAttribute("usuario") : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login?expirado=1");
            return;
        }

        String path = req.getServletPath();
        Set<RolUsuario> rolesPermitidos = PERMISOS.get(path);
        if (rolesPermitidos != null && !rolesPermitidos.contains(usuario.getRol())) {
            resp.sendRedirect(req.getContextPath() + "/app/dashboard?error=sin_permiso");
            return;
        }

        chain.doFilter(request, response);
    }
}
