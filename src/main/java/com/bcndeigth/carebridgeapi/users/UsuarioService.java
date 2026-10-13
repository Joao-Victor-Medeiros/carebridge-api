package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void cadastrarUsuario(SignUpUserDto dto) {
        var hashedPassword = passwordEncoder.encode(dto.senha());
        userRepository.save(new Usuario(dto.nome(), dto.email(), hashedPassword, dto.tipoPerfil()));
    }
}
