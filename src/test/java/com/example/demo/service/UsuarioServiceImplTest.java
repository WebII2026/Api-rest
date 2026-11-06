package com.example.demo.service;

import com.example.demo.domain.Rol;
import com.example.demo.domain.Usuario;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.auth.UsuarioResponse;
import com.example.demo.exception.EmailDuplicadoException;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl service;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    @Test
    void registrar_nuncaGuardaPasswordEnTextoPlano() {
        when(repository.existsByEmail("ana@test.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("$2a$10$encodedHash123456");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            return new Usuario(1L, u.email(), u.passwordHash(), u.rol());
        });

        UsuarioResponse response = service.registrar(new RegistroRequest("ana@test.com", "12345678"));

        verify(repository).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().passwordHash()).isNotEqualTo("12345678");
        assertThat(usuarioCaptor.getValue().rol()).isEqualTo(Rol.USER);
        assertThat(response.email()).isEqualTo("ana@test.com");
    }

    @Test
    void registrar_emailDuplicado_lanzaEmailDuplicadoException() {
        when(repository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(new RegistroRequest("ana@test.com", "12345678")))
                .isInstanceOf(EmailDuplicadoException.class)
                .hasMessageContaining("ana@test.com");
    }
}
