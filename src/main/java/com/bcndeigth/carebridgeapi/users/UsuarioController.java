package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.UserProfileResponse;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.UsuarioControllerDocs;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UsuarioController implements UsuarioControllerDocs {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal Usuario usuario) {
        return usuarioService.obterPerfil(usuario);
    }
}
