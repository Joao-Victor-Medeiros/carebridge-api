package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.ContaService;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static com.bcndeigth.carebridgeapi.users.ContaService.exigir;

@Service
public class PacienteService {

    private static final int IDADE_MINIMA = 18;

    private final ContaService contaService;
    private final PacienteRepository pacienteRepository;

    public PacienteService(ContaService contaService, PacienteRepository pacienteRepository) {
        this.contaService = contaService;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public CadastroResponse cadastrarPaciente(CadastroPacienteDto dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados do cadastro são obrigatórios.");
        }
        exigir(dto.nome(), "O nome é obrigatório.");
        if (dto.dataNascimento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de nascimento é obrigatória.");
        }
        if (dto.dataNascimento().isAfter(LocalDate.now().minusYears(IDADE_MINIMA))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É preciso ter 18 anos ou mais para criar uma conta.");
        }

        Usuario usuario = contaService.criar(dto.usuarioDto(), dto.aceitouTermos(), TipoPerfil.PACIENTE);
        pacienteRepository.save(new Paciente(
                dto.nome().trim(),
                dto.dataNascimento(),
                somenteDigitos(dto.telefone()),
                dto.endereco(),
                usuario
        ));
        return CadastroResponse.semVerificacao(usuario.getEmail(), TipoPerfil.PACIENTE);
    }

    private String somenteDigitos(String telefone) {
        if (telefone == null) {
            return null;
        }
        String digitos = telefone.replaceAll("\\D+", "");
        return digitos.isEmpty() ? null : digitos;
    }
}
