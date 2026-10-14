package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public PacienteService(
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        var hashedPassword = passwordEncoder.encode(cadastroPacienteDto.usuarioDto().senha());
        var usuarioDto = cadastroPacienteDto.usuarioDto();
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                usuarioDto.nome(),
                usuarioDto.email(),
                hashedPassword,
                TipoPerfil.PACIENTE
        ));

        pacienteRepository.save(new Paciente(
                cadastroPacienteDto.dataNascimento(),
                cadastroPacienteDto.telefone(),
                cadastroPacienteDto.endereco(),
                usuarioSalvo
        ));
    }
}
