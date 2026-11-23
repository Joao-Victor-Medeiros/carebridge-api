package com.bcndeigth.carebridgeapi.auth;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
public class VerificacaoEmailService {
    private static final Duration VALIDADE_CODIGO = Duration.ofMinutes(15);
    private static final Duration INTERVALO_REENVIO = Duration.ofSeconds(60);
    private static final int LIMITE_TENTATIVAS = 5;

    private final CodigoVerificacaoEmailRepository codigoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoEmailService brevoEmailService;
    private final RegistroTentativaVerificacaoService registroTentativaService;
    private final CodigoAcesso codigoAcesso;

    public VerificacaoEmailService(
            CodigoVerificacaoEmailRepository codigoRepository,
            UsuarioRepository usuarioRepository,
            ProfissionalSaudeRepository profissionalRepository,
            PasswordEncoder passwordEncoder,
            BrevoEmailService brevoEmailService,
            RegistroTentativaVerificacaoService registroTentativaService,
            CodigoAcesso codigoAcesso
    ) {
        this.codigoRepository = codigoRepository;
        this.usuarioRepository = usuarioRepository;
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
        this.brevoEmailService = brevoEmailService;
        this.registroTentativaService = registroTentativaService;
        this.codigoAcesso = codigoAcesso;
    }

    @Transactional
    public void gerarEEnviarCodigo(Usuario usuario, String nome) {
        validarProfissionalNaoVerificado(usuario);
        criarEEnviar(usuario, nome);
    }

    @Transactional
    public void reenviarCodigo(String email) {
        Usuario usuario = buscarUsuario(email);
        validarProfissionalNaoVerificado(usuario);

        codigoRepository.findFirstByUsuarioIdAndFinalidadeOrderByCriadoEmDesc(
                usuario.getId(), FinalidadeCodigo.VERIFICACAO_EMAIL).ifPresent(ultimo -> {
            if (ultimo.getCriadoEm().plus(INTERVALO_REENVIO).isAfter(Instant.now())) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Aguarde 60 segundos antes de solicitar um novo código.");
            }
        });

        String nome = profissionalRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil de profissional de saúde não encontrado."))
                .getNome();
        criarEEnviar(usuario, nome);
    }

    @Transactional
    public void verificar(String email, String codigoInformado) {
        validarObrigatorio(codigoInformado, "O código de verificação é obrigatório.");
        Usuario usuario = buscarUsuario(email);
        if (usuario.getTipoPerfil() != TipoPerfil.PROFISSIONAL_SAUDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A verificação de e-mail é exclusiva para profissionais de saúde.");
        }
        if (usuario.isEmailVerificado()) {
            return;
        }

        CodigoVerificacaoEmail codigo = codigoRepository.findFirstByUsuarioIdAndFinalidadeOrderByCriadoEmDesc(
                        usuario.getId(), FinalidadeCodigo.VERIFICACAO_EMAIL)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Código de verificação inválido ou expirado."));
        Instant agora = Instant.now();
        if (codigo.getUtilizadoEm() != null || !codigo.getExpiraEm().isAfter(agora)
                || codigo.getTentativas() >= LIMITE_TENTATIVAS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Código de verificação inválido ou expirado.");
        }
        if (!passwordEncoder.matches(codigoInformado.trim(), codigo.getCodigoHash())) {
            int tentativas = registroTentativaService.registrarFalha(codigo.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    tentativas >= LIMITE_TENTATIVAS
                            ? "Código invalidado após cinco tentativas incorretas. Solicite um novo código."
                            : "Código de verificação inválido.");
        }

        codigo.setUtilizadoEm(agora);
        usuario.marcarEmailComoVerificado();
        codigoRepository.save(codigo);
        usuarioRepository.save(usuario);
    }

    private void criarEEnviar(Usuario usuario, String nome) {
        String codigoAberto = codigoAcesso.gerar();
        Instant agora = Instant.now();
        CodigoVerificacaoEmail codigo = new CodigoVerificacaoEmail(
                usuario,
                passwordEncoder.encode(codigoAberto),
                FinalidadeCodigo.VERIFICACAO_EMAIL,
                agora,
                agora.plus(VALIDADE_CODIGO)
        );
        codigoRepository.save(codigo);
        if (!codigoAcesso.simulado()) {
            brevoEmailService.enviarCodigoVerificacao(usuario.getEmail(), nome, codigoAberto);
        }
    }

    private Usuario buscarUsuario(String email) {
        validarObrigatorio(email, "O e-mail é obrigatório.");
        return usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuário não encontrado para o e-mail informado."));
    }

    private void validarProfissionalNaoVerificado(Usuario usuario) {
        if (usuario == null || usuario.getTipoPerfil() != TipoPerfil.PROFISSIONAL_SAUDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A verificação de e-mail é exclusiva para profissionais de saúde.");
        }
        if (usuario.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O e-mail informado já foi verificado.");
        }
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
}