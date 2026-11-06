package com.example.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Abstracción para consultar la identidad y los roles del usuario autenticado
 * a partir del SecurityContext de Spring Security.
 * Permite que los Services no dependan directamente de SecurityContextHolder
 * y facilita el mockeo en pruebas unitarias.
 */
@Component
public class ContextoSeguridad {

    public Long usuarioActualId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }
        try {
            return Long.valueOf(principal.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
