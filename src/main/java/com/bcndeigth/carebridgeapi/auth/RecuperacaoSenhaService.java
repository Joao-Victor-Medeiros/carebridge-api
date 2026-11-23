package com.bcndeigth.carebridgeapi.auth;

import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.PoliticaSenha;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class RecuperacaoSenhaService {
    private static final Duration VALIDADE_CODIGO = Duration.ofMinutes(15);
    private static final Duration INTERVALO_REENVIO = Duration.ofSeconds(60);
    private static final int LIMITE_TENTATIVAS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CodigoVerificacaoEmailRepository codigoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoEmailService brevoEmailService;
    private final RegistroTentativaVerificacaoService registroTentativaService;

    public RecuperacaoSenhaService(
            CodigoVerificacaoEmailRepository codigoRepository,
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            ProfissionalSaudeRepository profissionalRepository,
            PasswordEncoder passwordEncoder,
            BrevoEmailService brevoEmailService,
            RegistroTentativaVerificacaoService registroTentativaService
    ) {
        this.codigoRepository = codigoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
        this.brevoEmailService = brevoEmailService;
        this.registroTentativaService = registroTentativaService;
    }

    @Transactional
    public void processar(String email, String codigo, String novaSenha) {
        validarObrigatorio(email, "O e-mail é obrigatório.");
        boolean solicitacao = vazio(codigo) && vazio(novaSenha);
        boolean confirmacao = !vazio(codigo) && !vazio(novaSenha);
        if (!solicitacao && !confirmacao) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe somente o e-mail para solicitar o código ou informe e-mail, código e nova senha.");
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado).orElse(null);
        if (solicitacao) {
            if (usuario != null && usuario.getTipoPerfil() != TipoPerfil.ADMIN) {
                gerarEEnviar(usuario);
            }
            return;
        }
        if (usuario == null || usuario.getTipoPerfil() == TipoPerfil.ADMIN) {
            throw codigoInvalido();
        }
        confirmar(usuario, codigo, novaSenha);
    }

    private void gerarEEnviar(Usuario usuario) {
        codigoRepository.findFirstByUsuarioIdAndFinalidadeOrderByCriadoEmDesc(
                usuario.getId(), FinalidadeCodigo.REDEFINICAO_SENHA).ifPresent(ultimo -> {
            if (ultimo.getCriadoEm().plus(INTERVALO_REENVIO).isAfter(Instant.now())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Aguarde 60 segundos antes de solicitar um novo código.");
            }
        });

        String codigoAberto = "%06d".formatted(RANDOM.nextInt(1_000_000));
        Instant agora = Instant.now();
        codigoRepository.save(new CodigoVerificacaoEmail(
                usuario,
                passwordEncoder.encode(codigoAberto),
                FinalidadeCodigo.REDEFINICAO_SENHA,
                agora,
                agora.plus(VALIDADE_CODIGO)
        ));
        brevoEmailService.enviarCodigoRedefinicaoSenha(
                usuario.getEmail(), obterNome(usuario), codigoAberto);
    }

    private void confirmar(Usuario usuario, String codigoInformado, String novaSenha) {
        PoliticaSenha.validar(novaSenha);
        CodigoVerificacaoEmail codigo = codigoRepository
                .findFirstByUsuarioIdAndFinalidadeOrderByCriadoEmDesc(
                        usuario.getId(), FinalidadeCodigo.REDEFINICAO_SENHA)
                .orElseThrow(this::codigoInvalido);
        Instant agora = Instant.now();
        if (codigo.getUtilizadoEm() != null || !codigo.getExpiraEm().isAfter(agora)
                || codigo.getTentativas() >= LIMITE_TENTATIVAS) {
            throw codigoInvalido();
        }
        if (!passwordEncoder.matches(codigoInformado.trim(), codigo.getCodigoHash())) {
            int tentativas = registroTentativaService.registrarFalha(codigo.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    tentativas >= LIMITE_TENTATIVAS
                            ? "Código invalidado após cinco tentativas incorretas. Solicite um novo código."
                            : "Código de redefinição inválido.");
        }
        if (passwordEncoder.matches(novaSenha, usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A nova senha deve ser diferente da senha atual.");
        }

        codigo.setUtilizadoEm(agora);
        usuario.alterarSenha(passwordEncoder.encode(novaSenha));
        codigoRepository.save(codigo);
        usuarioRepository.save(usuario);
    }

    private String obterNome(Usuario usuario) {
        if (usuario.getTipoPerfil() == TipoPerfil.PACIENTE) {
            return pacienteRepository.findByUsuarioId(usuario.getId())
                    .map(paciente -> paciente.getNome()).orElse("Usuário CareBridge");
        }
        return profissionalRepository.findByUsuarioId(usuario.getId())
                .map(profissional -> profissional.getNome()).orElse("Usuário CareBridge");
    }

    private ResponseStatusException codigoInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Código de redefinição inválido ou expirado.");
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (vazio(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
}