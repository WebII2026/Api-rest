package com.example.demo.service;

import com.example.demo.domain.Rol;
import com.example.demo.domain.Usuario;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.auth.UsuarioResponse;
import com.example.demo.exception.EmailDuplicadoException;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponse registrar(RegistroRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new EmailDuplicadoException("Ya existe una cuenta con el email " + request.email());
        }
        Usuario nuevo = new Usuario(
                null,
                request.email(),
                passwordEncoder.encode(request.password()),
                Rol.USER
        );
        return aResponse(repository.save(nuevo));
    }

    private UsuarioResponse aResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.email(), usuario.rol());
    }
}
