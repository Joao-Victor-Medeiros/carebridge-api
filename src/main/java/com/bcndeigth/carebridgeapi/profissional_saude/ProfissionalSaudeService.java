package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
<<<<<<< HEAD
import org.springframework.dao.DataIntegrityViolationException;
=======
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
>>>>>>> origin/develop
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
import java.util.stream.Collectors;

@Service
public class ProfissionalSaudeService {

    private static final long TAMANHO_MAXIMO_DOCUMENTO = 10L * 1024 * 1024;
    private static final java.util.Set<String> TIPOS_DOCUMENTO_PERMITIDOS = java.util.Set.of(
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
<<<<<<< HEAD
    public void cadastrarProfissionalSaude(CadastroProfissionalSaudeDto dto, List<MultipartFile> documentos) {
        if (dto == null || dto.usuarioDto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inválidos");
        }

        if (!Boolean.TRUE.equals(dto.acceptedTerms())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário aceitar os termos de uso");
        }

        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }

        var usuarioDto = dto.usuarioDto();
        if (usuarioDto.email() == null || usuarioDto.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail é obrigatório");
        }
        if (usuarioDto.senha() == null || usuarioDto.senha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha é obrigatória");
        }

=======
    public CadastroResponse cadastrarProfissionalSaude(CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto, List<MultipartFile> documentos) {
        if (cadastroProfissionalSaudeDto == null || cadastroProfissionalSaudeDto.usuarioDto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados da conta são obrigatórios.");
        }
>>>>>>> origin/develop
        if (documentos != null && documentos.size() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É permitido enviar no máximo 2 documentos por profissional.");
        }

        String emailNormalizado = usuarioDto.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail");
        }

        List<Documento> documentosEntidade = (documentos == null ? List.<MultipartFile>of() : documentos).stream()
                .map(this::converterDocumento)
                .collect(Collectors.toList());

<<<<<<< HEAD
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

            ProfissionalSaude profissional = new ProfissionalSaude(
                    dto.nome().trim(),
                    normalizarTexto(dto.conselho()),
                    normalizarUf(dto.uf()),
                    normalizarTexto(dto.numeroRegistro()),
                    normalizarTexto(dto.especialidade()),
                    normalizarTexto(dto.ondeAtende()),
                    documentosEntidade,
                    dto.biografiaOuDescricao(),
                    usuarioSalvo
            );

            profissionalSaudeRepository.save(profissional);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail", ex);
        }
    }

    private String normalizarTexto(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private String normalizarUf(String uf) {
        if (uf == null || uf.isBlank()) {
            return null;
        }
        return uf.trim().toUpperCase(Locale.ROOT);
=======
        var usuarioDto = cadastroProfissionalSaudeDto.usuarioDto();
        validarObrigatorio(cadastroProfissionalSaudeDto.nome(), "O nome é obrigatório.");
        validarObrigatorio(cadastroProfissionalSaudeDto.conselho(), "O conselho profissional é obrigatório.");
        validarObrigatorio(cadastroProfissionalSaudeDto.uf(), "A UF do conselho é obrigatória.");
        validarObrigatorio(cadastroProfissionalSaudeDto.numeroRegistro(), "O número de registro é obrigatório.");
        validarObrigatorio(cadastroProfissionalSaudeDto.especialidade(), "A especialidade é obrigatória.");
        validarObrigatorio(cadastroProfissionalSaudeDto.ondeAtende(), "O local de atendimento é obrigatório.");
        validarObrigatorio(usuarioDto.email(), "O e-mail é obrigatório.");
        validarObrigatorio(usuarioDto.senha(), "A senha é obrigatória.");
        if (!usuarioDto.acceptedTerms()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário aceitar os termos de uso e privacidade.");
        }
        String email = usuarioDto.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um usuário cadastrado com este e-mail.");
        }
        var hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                email,
                hashedPassword,
                TipoPerfil.PROFISSIONAL_SAUDE,
                java.time.Instant.now()
        ));

        profissionalSaudeRepository.save(new ProfissionalSaude(
                cadastroProfissionalSaudeDto.nome(),
                cadastroProfissionalSaudeDto.conselho().trim().toUpperCase(java.util.Locale.ROOT),
                cadastroProfissionalSaudeDto.uf().trim().toUpperCase(java.util.Locale.ROOT),
                cadastroProfissionalSaudeDto.numeroRegistro().trim(),
                cadastroProfissionalSaudeDto.especialidade().trim(),
                cadastroProfissionalSaudeDto.ondeAtende().trim(),
                documentosEntidade,
                cadastroProfissionalSaudeDto.biografiaOuDescricao(),
                usuarioSalvo
        ));
        return new CadastroResponse(email, TipoPerfil.PROFISSIONAL_SAUDE.name(), 6);
>>>>>>> origin/develop
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
        if (tipo == null || !TIPOS_DOCUMENTO_PERMITIDOS.contains(tipo.toLowerCase(java.util.Locale.ROOT))) {
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