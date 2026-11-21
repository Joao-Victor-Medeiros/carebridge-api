package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
<<<<<<< HEAD
import org.springframework.dao.DataIntegrityViolationException;
=======
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
>>>>>>> origin/develop
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
<<<<<<< HEAD
    public void cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        if (cadastroPacienteDto == null || cadastroPacienteDto.usuarioDto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inválidos");
        }

        if (!Boolean.TRUE.equals(cadastroPacienteDto.acceptedTerms())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário aceitar os termos de uso");
        }

        var usuarioDto = cadastroPacienteDto.usuarioDto();
        if (usuarioDto.email() == null || usuarioDto.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail é obrigatório");
        }
        if (usuarioDto.senha() == null || usuarioDto.senha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha é obrigatória");
        }
        if (cadastroPacienteDto.nome() == null || cadastroPacienteDto.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");
        }
        if (cadastroPacienteDto.dataNascimento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data de nascimento é obrigatória");
        }

        String emailNormalizado = usuarioDto.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail");
        }

        String telefoneNormalizado = normalizarTelefone(cadastroPacienteDto.telefone());
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

            Paciente paciente = new Paciente(
                    cadastroPacienteDto.nome().trim(),
                    cadastroPacienteDto.dataNascimento(),
                    telefoneNormalizado,
                    cadastroPacienteDto.endereco(),
                    usuarioSalvo
            );

            pacienteRepository.save(paciente);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta cadastrada com este e-mail", ex);
        }
=======
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
>>>>>>> origin/develop
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        String digitos = telefone.replaceAll("\\D+", "");
        return digitos.isEmpty() ? null : digitos;
    }
}