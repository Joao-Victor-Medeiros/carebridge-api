package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
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
    public CadastroResponse cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        if (cadastroPacienteDto == null || cadastroPacienteDto.usuarioDto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados da conta são obrigatórios.");
        }
        var usuarioDto = cadastroPacienteDto.usuarioDto();
        validarObrigatorio(cadastroPacienteDto.nome(), "O nome é obrigatório.");
        if (cadastroPacienteDto.dataNascimento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de nascimento é obrigatória.");
        }
        validarConta(usuarioDto.email(), usuarioDto.senha(), usuarioDto.acceptedTerms());
        String email = usuarioDto.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        var hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        var usuarioSalvo = usuarioRepository.save(new Usuario(
                email,
                hashedPassword,
                TipoPerfil.PACIENTE,
                java.time.Instant.now()
        ));

        pacienteRepository.save(new Paciente(
                cadastroPacienteDto.nome(),
                cadastroPacienteDto.dataNascimento(),
                somenteDigitos(cadastroPacienteDto.telefone()),
                cadastroPacienteDto.endereco(),
                usuarioSalvo
        ));
        return new CadastroResponse(email, TipoPerfil.PACIENTE.name(), 6);
    }

    private void validarConta(String email, String senha, boolean acceptedTerms) {
        validarObrigatorio(email, "O e-mail é obrigatório.");
        validarObrigatorio(senha, "A senha é obrigatória.");
        if (!acceptedTerms) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário aceitar os termos de uso e privacidade.");
        }
    }

    private void validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }

    private String somenteDigitos(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        String normalizado = telefone.replaceAll("\\D", "");
        return normalizado.isBlank() ? null : normalizado;
    }
}
