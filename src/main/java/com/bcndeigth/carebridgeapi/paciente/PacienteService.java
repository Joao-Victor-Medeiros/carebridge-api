package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        var hashedPassword = passwordEncoder.encode(cadastroPacienteDto.usuarioDto().senha());
        var usuarioDto = cadastroPacienteDto.usuarioDto();
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                usuarioDto.nome(),
                usuarioDto.email(),
                hashedPassword,
                usuarioDto.tipoPerfil()
        ));

        pacienteRepository.save(new Paciente(
                cadastroPacienteDto.dataNascimento(),
                cadastroPacienteDto.telefone(),
                cadastroPacienteDto.endereco(),
                usuarioSalvo
        ));
    }
}
