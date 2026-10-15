package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void cadastrarProfissionalSaude(CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto) {
        var usuarioDto = cadastroProfissionalSaudeDto.usuarioDto();
        var hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                usuarioDto.nome(),
                usuarioDto.email(),
                hashedPassword,
                TipoPerfil.PROFISSIONAL_SAUDE
        ));

        profissionalSaudeRepository.save(new ProfissionalSaude(
                cadastroProfissionalSaudeDto.documentoRegistro(),
                cadastroProfissionalSaudeDto.biografiaOuDescricao(),
                usuarioSalvo
        ));
    }
}