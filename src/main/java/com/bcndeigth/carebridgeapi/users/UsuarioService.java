package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void cadastrarUsuario(SignUpUserDto dto) {
        var hashedPassword = passwordEncoder.encode(dto.senha());
        userRepository.save(new Usuario(dto.nome(), dto.email(), hashedPassword, dto.tipoPerfil()));
    }
}
