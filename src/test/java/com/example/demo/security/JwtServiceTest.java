package com.example.demo.security;

import com.example.demo.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "clave-super-secreta-para-tests-con-tamano-suficiente-1234567890";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRETO, 3600000L);
    }

    @Test
    void generarYValidarToken_devuelveClaimsCorrectos() {
        String token = jwtService.generarToken(42L, Rol.USER);

        Claims claims = jwtService.validarYObtenerClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("rol", String.class)).isEqualTo("USER");
    }

    @Test
    void validar_tokenAlterado_lanzaJwtException() {
        String token = jwtService.generarToken(42L, Rol.USER);
        String tokenAlterado = token + "modificado";

        assertThatThrownBy(() -> jwtService.validarYObtenerClaims(tokenAlterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validar_tokenVencido_lanzaJwtException() {
        JwtService servicioConTokenVencido = new JwtService(SECRETO, -1000L);
        String tokenVencido = servicioConTokenVencido.generarToken(42L, Rol.USER);

        assertThatThrownBy(() -> jwtService.validarYObtenerClaims(tokenVencido))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validar_tokenFirmadoConOtroSecreto_lanzaJwtException() {
        JwtService otroServicio = new JwtService("otra-clave-completamente-distinta-para-firmar-tokens-123456789", 3600000L);
        String token = otroServicio.generarToken(42L, Rol.USER);

        assertThatThrownBy(() -> jwtService.validarYObtenerClaims(token))
                .isInstanceOf(JwtException.class);
    }
}
