package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;

    public UsuarioService(PacienteRepository pacienteRepository,
                          ProfissionalSaudeRepository profissionalSaudeRepository) {
        this.pacienteRepository = pacienteRepository;
        this.profissionalSaudeRepository = profissionalSaudeRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse obterPerfil(Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }
        if (usuario.getTipoPerfil() == TipoPerfil.PACIENTE) {
            var paciente = pacienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Perfil de paciente não encontrado."));
            return new UserProfileResponse(usuario.getId(), paciente.getNome(), usuario.getEmail(),
                    usuario.getTipoPerfil(), paciente.getDataNascimento(), paciente.getTelefone(), null);
        }
        if (usuario.getTipoPerfil() == TipoPerfil.PROFISSIONAL_SAUDE) {
            var profissional = profissionalSaudeRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Perfil de profissional de saúde não encontrado."));
            return new UserProfileResponse(usuario.getId(), profissional.getNome(), usuario.getEmail(),
                    usuario.getTipoPerfil(), null, null, profissional.getStatusVerificacao().name());
        }
        return new UserProfileResponse(usuario.getId(), null, usuario.getEmail(),
                usuario.getTipoPerfil(), null, null, null);
    }
}
