package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.ErrorResponse;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<ErrorResponse> cadastrarUsuario(SignUpUserDto dto) {
        if (userRepository.findByEmail(dto.email()) != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // Código HTTP: 409
                    .body(new ErrorResponse(
                            409,
                            "Já existe um usuário cadastrado com este e-mail."
                    ));
        }

        var hashedPassword = passwordEncoder.encode(dto.senha());
        userRepository.save(new Usuario(dto.nome(), dto.email(), hashedPassword, dto.tipoPerfil()));

        return ResponseEntity
                .status(HttpStatus.CREATED) // Código HTTP: 201
                .body(new ErrorResponse(
                        201,
                        "Usuário cadastrado com sucesso."
                ));
    }
}
