package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.config.TokenService;
import com.bcndeigth.carebridgeapi.users.dto.AuthenticationDTO;
import com.bcndeigth.carebridgeapi.users.dto.LoginResponseDTO;
import com.bcndeigth.carebridgeapi.users.dto.UserProfileResponse;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.UsuarioControllerDocs;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user")
public class UsuarioController implements UsuarioControllerDocs {
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    public UsuarioController(UsuarioService usuarioService, AuthenticationManager authenticationManager,
                             TokenService tokenService, LoginAttemptService loginAttemptService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO authenticationDTO,
                                                   HttpServletRequest request) {
        if (authenticationDTO == null || authenticationDTO.email() == null
                || authenticationDTO.email().isBlank() || authenticationDTO.password() == null
                || authenticationDTO.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e senha são obrigatórios.");
        }
        String ip = request.getRemoteAddr();
        loginAttemptService.validarPermitido(authenticationDTO.email(), ip);
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(
                    authenticationDTO.email().trim(), authenticationDTO.password());
            var authentication = authenticationManager.authenticate(authenticationToken);
            Usuario usuario = (Usuario) authentication.getPrincipal();
            loginAttemptService.registrarSucesso(authenticationDTO.email(), ip);
            return ResponseEntity.ok(new LoginResponseDTO(
                    tokenService.generateToken(usuario),
                    TokenService.EXPIRATION_SECONDS,
                    usuarioService.obterPerfil(usuario)
            ));
        } catch (AuthenticationException exception) {
            loginAttemptService.registrarFalha(authenticationDTO.email(), ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Sessão expirada ou credenciais inválidas.");
        }
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal Usuario usuario) {
        return usuarioService.obterPerfil(usuario);
    }
}
