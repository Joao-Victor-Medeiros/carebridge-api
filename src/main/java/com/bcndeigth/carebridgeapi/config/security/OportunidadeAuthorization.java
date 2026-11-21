package com.bcndeigth.carebridgeapi.config.security;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("oportunidadeAuthorization")
public class OportunidadeAuthorization {
    public boolean podeGerenciar(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Usuario usuario
                && usuario.getTipoPerfil() == TipoPerfil.PROFISSIONAL_SAUDE;
    }
}