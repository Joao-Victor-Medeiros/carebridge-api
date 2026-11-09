package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.config.TokenService;
import com.bcndeigth.carebridgeapi.users.dto.AuthenticationDTO;
import com.bcndeigth.carebridgeapi.users.dto.LoginResponseDTO;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import com.bcndeigth.carebridgeapi.users.dto.ProfissionalPendenteResponse;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.UsuarioControllerDocs;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UsuarioController implements UsuarioControllerDocs {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ProfissionalSaudeRepository profissionalSaudeRepository;

    @Autowired
    private AdministradorService administradorService;

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

        boolean validado = profissionalSaudeRepository.findByUsuarioId(usuario.getId())
                .map(com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude::isValidadoInstitucionalmente)
                .orElse(false);

        return ResponseEntity.ok(new LoginResponseDTO(
                tokenService.generateToken(usuario, validado),
                true,
                usuario.getTipoPerfil().name(),
                validado
        ));
    }

    @GetMapping("/profissionais/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProfissionalPendenteResponse> listarProfissionaisPendentes() {
        return administradorService.listarProfissionaisPendentes();
    }

    @PatchMapping("/profissionais/{profissionalId}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfissionalPendenteResponse aprovarProfissional(@PathVariable UUID profissionalId) {
        return administradorService.aprovarProfissional(profissionalId);
    }
}
