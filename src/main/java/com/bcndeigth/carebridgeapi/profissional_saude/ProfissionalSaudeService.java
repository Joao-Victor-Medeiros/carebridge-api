package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.auth.VerificacaoEmailService;
import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.ContaService;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.bcndeigth.carebridgeapi.users.ContaService.exigir;

@Service
public class ProfissionalSaudeService {

    private static final int MAXIMO_DOCUMENTOS = 2;
    private static final long TAMANHO_MAXIMO_DOCUMENTO = 10L * 1024 * 1024;
    private static final Set<String> TIPOS_DOCUMENTO_PERMITIDOS = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final ContaService contaService;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;
    private final VerificacaoEmailService verificacaoEmailService;

    public ProfissionalSaudeService(
            ContaService contaService,
            ProfissionalSaudeRepository profissionalSaudeRepository,
            VerificacaoEmailService verificacaoEmailService
    ) {
        this.contaService = contaService;
        this.profissionalSaudeRepository = profissionalSaudeRepository;
        this.verificacaoEmailService = verificacaoEmailService;
    }

    @Transactional
    public CadastroResponse cadastrarProfissionalSaude(CadastroProfissionalSaudeDto dto, List<MultipartFile> documentos) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados do cadastro são obrigatórios.");
        }
        List<MultipartFile> arquivos = documentos == null ? List.of() : documentos;
        if (arquivos.size() > MAXIMO_DOCUMENTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É permitido enviar no máximo 2 documentos por profissional.");
        }
        exigir(dto.nome(), "O nome é obrigatório.");
        exigir(dto.conselho(), "O conselho profissional é obrigatório.");
        exigir(dto.uf(), "A UF do conselho é obrigatória.");
        exigir(dto.numeroRegistro(), "O número de registro é obrigatório.");
        if (!dto.numeroRegistro().trim().matches("\\d{4,7}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O número de registro deve ter de 4 a 7 dígitos.");
        }
        exigir(dto.especialidade(), "A especialidade é obrigatória.");
        exigir(dto.ondeAtende(), "O local de atendimento é obrigatório.");

        List<Documento> documentosRegistro = arquivos.stream().map(this::converterDocumento).toList();
        Usuario usuario = contaService.criar(dto.usuarioDto(), dto.acceptedTerms(), TipoPerfil.PROFISSIONAL_SAUDE);
        ProfissionalSaude profissional = profissionalSaudeRepository.save(new ProfissionalSaude(
                dto.nome().trim(),
                dto.conselho().trim().toUpperCase(Locale.ROOT),
                dto.uf().trim().toUpperCase(Locale.ROOT),
                dto.numeroRegistro().trim(),
                dto.especialidade().trim(),
                dto.ondeAtende().trim(),
                documentosRegistro,
                dto.biografiaOuDescricao(),
                usuario
        ));
        verificacaoEmailService.gerarEEnviarCodigo(usuario, profissional.getNome());
        return new CadastroResponse(usuario.getEmail(), TipoPerfil.PROFISSIONAL_SAUDE, true, 6);
    }

    private Documento converterDocumento(MultipartFile documento) {
        String nome = documento.getOriginalFilename();
        if (documento.isEmpty() || nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo com nome e conteúdo.");
        }
        nome = nome.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1);
        String tipo = documento.getContentType();
        if (nome.isBlank() || nome.length() > 255 || (tipo != null && tipo.length() > 255)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome ou tipo de arquivo inválido.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo enviado.", exception);
        }
    }
}