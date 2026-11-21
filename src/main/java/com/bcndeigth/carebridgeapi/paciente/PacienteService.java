package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;

    public PacienteService(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            PasswordEncoder passwordEncoder
    ) {
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

        boolean aceitouTermos = !Boolean.FALSE.equals(cadastroPacienteDto.acceptedTerms()) && (Boolean.TRUE.equals(cadastroPacienteDto.acceptedTerms()) || usuarioDto.acceptedTerms());
        validarConta(usuarioDto.email(), usuarioDto.senha(), aceitouTermos);

        String emailNormalizado = usuarioDto.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(emailNormalizado) || usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail");
        }

        String hashedPassword = passwordEncoder.encode(usuarioDto.senha());
        Instant agora = Instant.now();

        Usuario usuario = new Usuario(
                emailNormalizado,
                hashedPassword,
                TipoPerfil.PACIENTE,
                true,
                agora
        );

        try {
            Usuario usuarioSalvo = usuarioRepository.save(usuario);
            pacienteRepository.save(new Paciente(
                    cadastroPacienteDto.nome().trim(),
                    cadastroPacienteDto.dataNascimento(),
                    normalizarTelefone(cadastroPacienteDto.telefone()),
                    cadastroPacienteDto.endereco(),
                    usuarioSalvo
            ));
            return new CadastroResponse(emailNormalizado, TipoPerfil.PACIENTE.name(), 6);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail", ex);
        }
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

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        String digitos = telefone.replaceAll("\\D+", "");
        return digitos.isEmpty() ? null : digitos;
    }
}