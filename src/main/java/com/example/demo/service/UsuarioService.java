package com.example.demo.service;

import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.auth.UsuarioResponse;

public interface UsuarioService {
    UsuarioResponse registrar(RegistroRequest request);
}
