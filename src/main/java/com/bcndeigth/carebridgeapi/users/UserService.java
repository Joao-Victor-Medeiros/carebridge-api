package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


    public void cadastrarUsuario(SignUpUserDto dto) {
        //transformar a senha em hash
        var encoder = new BCryptPasswordEncoder();
        var hashedPassword = encoder.encode(dto.senha());
        userRepository.save(new Usuario(dto.nome(), dto.email(), hashedPassword, dto.tipoPerfil()));
    }
}
