package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.config.TokenService;
import com.bcndeigth.carebridgeapi.users.dto.AuthenticationDTO;
import com.bcndeigth.carebridgeapi.users.dto.LoginResponseDTO;
import com.bcndeigth.carebridgeapi.users.dto.RegisterDTO;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
    }


    //deve existis esse método aqui ou renomear a classe para ficar mais semantico
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody SignUpUserDto userDto) {
        usuarioService.cadastrarUsuario(userDto);

        return ResponseEntity.ok().build();
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