package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.config.TokenService;
import com.bcndeigth.carebridgeapi.users.dto.AuthenticationDTO;
import com.bcndeigth.carebridgeapi.users.dto.LoginResponseDTO;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.UsuarioControllerDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UsuarioController implements UsuarioControllerDocs {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Override
    @PostMapping("/sign-up")
    public void signUp(SignUpUserDto dto) {
        //aqui deve registrar segundo sua role
        usuarioService.cadastrarUsuario(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO authenticationDTO) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationDTO.email(),
                authenticationDTO.password()
        );
        var authentication = authenticationManager.authenticate(authenticationToken);
        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(new LoginResponseDTO(tokenService.generateToken(usuario)));
    }
}
