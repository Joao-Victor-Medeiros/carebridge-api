package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfissionalSaudeService {

    private static final long TAMANHO_MAXIMO_DOCUMENTO = 10L * 1024 * 1024;
    private static final Set<String> TIPOS_DOCUMENTO_PERMITIDOS = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final ProfissionalSaudeRepository profissionalSaudeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfissionalSaudeService(
            ProfissionalSaudeRepository profissionalSaudeRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.profissionalSaudeRepository = profissionalSaudeRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CadastroResponse cadastrarProfissionalSaude(CadastroProfissionalSaudeDto dto, List<MultipartFile> documentos) {
        if (dto == null || dto.usuarioDto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados da conta são obrigatórios.");
        }
        if (documentos != null && documentos.size() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É permitido enviar no máximo 2 documentos por profissional.");
        }

        var usuarioDto = dto.usuarioDto();
        validarObrigatorio(dto.nome(), "O nome é obrigatório.");
        validarObrigatorio(dto.conselho(), "O conselho profissional é obrigatório.");
        validarObrigatorio(dto.uf(), "A UF do conselho é obrigatória.");
        validarObrigatorio(dto.numeroRegistro(), "O número de registro é obrigatório.");
        validarObrigatorio(dto.especialidade(), "A especialidade é obrigatória.");
        validarObrigatorio(dto.ondeAtende(), "O local de atendimento é obrigatório.");
        validarObrigatorio(usuarioDto.email(), "O e-mail é obrigatório.");
        validarObrigatorio(usuarioDto.senha(), "A senha é obrigatória.");

        boolean aceitouTermos = Boolean.TRUE.equals(dto.acceptedTerms()) || usuarioDto.acceptedTerms();
        if (!aceitouTermos) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário aceitar os termos de uso e privacidade.");
        }

        String emailNormalizado = usuarioDto.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(emailNormalizado) || usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail");
        }

        List<Documento> documentosEntidade = (documentos == null ? List.<MultipartFile>of() : documentos).stream()
                .map(this::converterDocumento)
                .collect(Collectors.toList());

        String hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        Instant agora = Instant.now();

        Usuario usuario = new Usuario(
                emailNormalizado,
                hashedPassword,
                TipoPerfil.PROFISSIONAL_SAUDE,
                true,
                agora
        );

        try {
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
            profissionalSaudeRepository.save(new ProfissionalSaude(
                    dto.nome().trim(),
                    dto.conselho().trim().toUpperCase(Locale.ROOT),
                    dto.uf().trim().toUpperCase(Locale.ROOT),
                    dto.numeroRegistro().trim(),
                    dto.especialidade().trim(),
                    dto.ondeAtende().trim(),
                    documentosEntidade,
                    dto.biografiaOuDescricao(),
                    usuarioSalvo
            ));
            return new CadastroResponse(emailNormalizado, TipoPerfil.PROFISSIONAL_SAUDE.name(), 6);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail", ex);
        }
    }

    private Documento converterDocumento(MultipartFile documento) {
        String nome = documento.getOriginalFilename();
        if (documento.isEmpty() || nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo com nome e conteudo.");
        }
        nome = nome.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1);
        String tipo = documento.getContentType();
        if (nome.isBlank() || nome.length() > 255 || (tipo != null && tipo.length() > 255)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome ou tipo de arquivo invalido.");
        }
        if (documento.getSize() > TAMANHO_MAXIMO_DOCUMENTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada documento pode ter no máximo 10 MB.");
        }
        if (tipo == null || !TIPOS_DOCUMENTO_PERMITIDOS.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie documentos nos formatos PDF, JPG ou PNG.");
        }
        try {
            return new Documento(nome, tipo, documento.getBytes());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao foi possivel ler o arquivo enviado.", exception);
        }
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
}