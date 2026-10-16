package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
        var usuarioDto = cadastroProfissionalSaudeDto.usuarioDto();
        var hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                usuarioDto.nome(),
                usuarioDto.email(),
                hashedPassword,
                TipoPerfil.PROFISSIONAL_SAUDE
        ));

        List<Documento> documentosEntidade = documentos.stream()
                .map(doc -> new Documento(doc.getOriginalFilename(), "base64_or_path_placeholder")) // Replace with actual file handling logic
                .collect(Collectors.toList());

        profissionalSaudeRepository.save(new ProfissionalSaude(
                documentosEntidade,
                cadastroProfissionalSaudeDto.biografiaOuDescricao(),
                usuarioSalvo
        ));
    }
}