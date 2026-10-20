package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.io.IOException;
import java.util.stream.Collectors;

@Service
public class ProfissionalSaudeService {

    @Autowired
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
    public void cadastrarProfissionalSaude(CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto, List<MultipartFile> documentos) {
        if (documentos != null && documentos.size() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É permitido enviar no máximo 2 documentos por profissional.");
        }

        List<Documento> documentosEntidade = (documentos == null ? List.<MultipartFile>of() : documentos).stream()
                .map(this::converterDocumento)
                .collect(Collectors.toList());

        var usuarioDto = cadastroProfissionalSaudeDto.usuarioDto();
        var hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                usuarioDto.nome(),
                usuarioDto.email(),
                hashedPassword,
                TipoPerfil.PROFISSIONAL_SAUDE
        ));

        profissionalSaudeRepository.save(new ProfissionalSaude(
                documentosEntidade,
                cadastroProfissionalSaudeDto.biografiaOuDescricao(),
                usuarioSalvo
        ));
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
        try {
            return new Documento(nome, tipo == null || tipo.isBlank()
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE : tipo, documento.getBytes());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao foi possivel ler o arquivo enviado.", exception);
        }
    }
}