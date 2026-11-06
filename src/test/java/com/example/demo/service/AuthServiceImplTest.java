package com.example.demo.service;

import com.example.demo.domain.Rol;
import com.example.demo.domain.Usuario;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.exception.CredencialesInvalidasException;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_conCredencialesValidas_devuelveToken() {
        Usuario usuario = new Usuario(1L, "ana@test.com", "$2a$10$hash", Rol.USER);
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("12345678", "$2a$10$hash")).thenReturn(true);
        when(jwtService.generarToken(1L, Rol.USER)).thenReturn("token.jwt.valido");

        LoginResponse response = authService.login(new LoginRequest("ana@test.com", "12345678"));

        assertThat(response.token()).isEqualTo("token.jwt.valido");
    }

    @Test
    void login_conEmailInexistente_lanzaCredencialesInvalidasException() {
        when(usuarioRepository.findByEmail("inexistente@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("inexistente@test.com", "12345678")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Email o contraseña incorrectos");
    }

    @Test
    void login_conPasswordIncorrecta_lanzaCredencialesInvalidasException() {
        Usuario usuario = new Usuario(1L, "ana@test.com", "$2a$10$hash", Rol.USER);
        when(usuarioRepository.findByEmail("ana@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password_erronea", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@test.com", "password_erronea")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Email o contraseña incorrectos");
    }
}
