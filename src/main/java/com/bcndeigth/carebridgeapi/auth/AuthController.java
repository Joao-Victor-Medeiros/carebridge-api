package com.bcndeigth.carebridgeapi.auth;

import com.bcndeigth.carebridgeapi.auth.dto.AuthMessageResponse;
import com.bcndeigth.carebridgeapi.auth.dto.CadastroUsuarioMultipartSchema;
import com.bcndeigth.carebridgeapi.auth.dto.CadastroUsuarioRequest;
import com.bcndeigth.carebridgeapi.auth.dto.ReenviarCodigoRequest;
import com.bcndeigth.carebridgeapi.auth.dto.PasswordResetRequest;
import com.bcndeigth.carebridgeapi.auth.dto.VerificarEmailRequest;
import com.bcndeigth.carebridgeapi.config.TokenService;
import com.bcndeigth.carebridgeapi.paciente.PacienteService;
import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeService;
import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.LoginAttemptService;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioService;
import com.bcndeigth.carebridgeapi.users.dto.AuthenticationDTO;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import com.bcndeigth.carebridgeapi.users.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login, cadastro, verificação de e-mail e recuperação de senha.")
public class AuthController {
    private final VerificacaoEmailService verificacaoEmailService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final UsuarioService usuarioService;
    private final PacienteService pacienteService;
    private final ProfissionalSaudeService profissionalSaudeService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            VerificacaoEmailService verificacaoEmailService,
            RecuperacaoSenhaService recuperacaoSenhaService,
            UsuarioService usuarioService,
            PacienteService pacienteService,
            ProfissionalSaudeService profissionalSaudeService,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            LoginAttemptService loginAttemptService
    ) {
        this.verificacaoEmailService = verificacaoEmailService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
        this.usuarioService = usuarioService;
        this.pacienteService = pacienteService;
        this.profissionalSaudeService = profissionalSaudeService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = "Valida e-mail e senha e retorna um token JWT com o perfil do usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso.",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "E-mail ou senha ausentes."),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas."),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas de login excedido.")
    })
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody AuthenticationDTO authenticationDTO,
            HttpServletRequest request
    ) {
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

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cadastrar paciente ou profissional de saúde",
            description = "Envie a parte dados como JSON com tipoPerfil PACIENTE ou PROFISSIONAL_SAUDE. "
                    + "A parte documentos é aceita somente para profissionais de saúde.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CadastroUsuarioMultipartSchema.class),
                            encoding = @Encoding(name = "dados", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso.",
                    content = @Content(schema = @Schema(implementation = CadastroResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados, perfil ou documentos inválidos."),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado."),
            @ApiResponse(responseCode = "413", description = "Arquivo ou requisição acima do limite permitido."),
            @ApiResponse(responseCode = "415", description = "Parte dados sem Content-Type application/json."),
            @ApiResponse(responseCode = "503", description = "Não foi possível enviar o e-mail transacional.")
    })
    public ResponseEntity<CadastroResponse> cadastrar(
            @RequestPart("dados") CadastroUsuarioRequest dados,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos
    ) {
        CadastroResponse response;
        if (dados instanceof CadastroPacienteDto paciente) {
            if (documentos != null && !documentos.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Documentos são permitidos somente no cadastro de profissional de saúde.");
            }
            response = pacienteService.cadastrarPaciente(paciente);
        } else if (dados instanceof CadastroProfissionalSaudeDto profissional) {
            response = profissionalSaudeService.cadastrarProfissionalSaude(profissional, documentos);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de perfil inválido para cadastro.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verificar e-mail profissional",
            description = "Confirma o e-mail de um profissional usando o código temporário recebido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "E-mail verificado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Código inválido, expirado ou limite de tentativas atingido."),
            @ApiResponse(responseCode = "404", description = "Usuário profissional não encontrado.")
    })
    public ResponseEntity<AuthMessageResponse> verificar(@RequestBody VerificarEmailRequest request) {
        verificacaoEmailService.verificar(
                request == null ? null : request.email(),
                request == null ? null : request.code()
        );
        return ResponseEntity.ok(new AuthMessageResponse("E-mail verificado com sucesso."));
    }

    @PostMapping("/resend-code")
    @Operation(summary = "Reenviar código de verificação",
            description = "Gera um novo código de verificação para uma conta profissional elegível.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo código enviado."),
            @ApiResponse(responseCode = "400", description = "Conta não elegível para verificação."),
            @ApiResponse(responseCode = "429", description = "Intervalo mínimo entre envios não respeitado."),
            @ApiResponse(responseCode = "503", description = "Não foi possível enviar o e-mail transacional.")
    })
    public ResponseEntity<AuthMessageResponse> reenviar(@RequestBody ReenviarCodigoRequest request) {
        verificacaoEmailService.reenviarCodigo(request == null ? null : request.email());
        return ResponseEntity.ok(new AuthMessageResponse("Um novo código de verificação foi enviado."));
    }

    @PostMapping("/password-reset")
    @Operation(
            summary = "Solicitar ou confirmar redefinição de senha",
            description = "Envie somente o e-mail para solicitar um código. Para confirmar, envie e-mail, code e newPassword."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação aceita ou senha redefinida."),
            @ApiResponse(responseCode = "400", description = "Combinação de campos, código ou nova senha inválidos."),
            @ApiResponse(responseCode = "429", description = "Intervalo mínimo entre envios não respeitado."),
            @ApiResponse(responseCode = "503", description = "Não foi possível enviar o e-mail transacional.")
    })
    public ResponseEntity<AuthMessageResponse> redefinirSenha(@RequestBody PasswordResetRequest request) {
        recuperacaoSenhaService.processar(
                request == null ? null : request.email(),
                request == null ? null : request.code(),
                request == null ? null : request.newPassword()
        );
        String mensagem = request != null && request.code() != null && !request.code().isBlank()
                ? "Senha redefinida com sucesso."
                : "Se o e-mail estiver cadastrado, um código de redefinição será enviado.";
        return ResponseEntity.ok(new AuthMessageResponse(mensagem));
    }
}