package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;

    public PacienteService(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        var usuarioDto = cadastroPacienteDto.usuarioDto();
        if (usuarioRepository.existsByEmail(usuarioDto.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        var hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                usuarioDto.email(),
                hashedPassword,
                usuarioDto.tipoPerfil()
        ));

        pacienteRepository.save(new Paciente(
                cadastroPacienteDto.nome(),
                cadastroPacienteDto.dataNascimento(),
                cadastroPacienteDto.telefone(),
                cadastroPacienteDto.endereco(),
                usuarioSalvo
        ));
    }
}
