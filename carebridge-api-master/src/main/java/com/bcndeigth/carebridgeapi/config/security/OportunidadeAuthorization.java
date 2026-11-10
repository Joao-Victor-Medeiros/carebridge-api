package com.bcndeigth.carebridgeapi.config.security;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("oportunidadeAuthorization")
public class OportunidadeAuthorization {
    private final ProfissionalSaudeRepository profissionalSaudeRepository;

    public OportunidadeAuthorization(ProfissionalSaudeRepository profissionalSaudeRepository) {
        this.profissionalSaudeRepository = profissionalSaudeRepository;
    }

    public boolean podeGerenciar(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Usuario usuario)
                || usuario.getTipoPerfil() != TipoPerfil.PROFISSIONAL_SAUDE) {
            return false;
        }
        UUID usuarioId = usuario.getId();
        return profissionalSaudeRepository.findByUsuarioId(usuarioId)
                .map(profissional -> profissional.isValidadoInstitucionalmente())
                .orElse(false);
    }
}