package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.UsuarioControllerDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController implements UsuarioControllerDocs {
    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void signUp(SignUpUserDto dto) {
        //aqui deve registrar segundo sua role
        usuarioService.cadastrarUsuario(dto);
    }
}
