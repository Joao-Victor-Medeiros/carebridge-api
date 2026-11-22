package com.bcndeigth.carebridgeapi.config.security;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OportunidadeAuthorizationTest {

    private final OportunidadeAuthorization authorization = new OportunidadeAuthorization();

    @Test
    void devePermitirProfissionalDeSaudeAutenticado() {
        Usuario profissional = new Usuario("profissional@example.com", "senha-hash", TipoPerfil.PROFISSIONAL_SAUDE);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(profissional, null, profissional.getAuthorities());

        assertTrue(authorization.podeGerenciar(authentication));
    }

    @Test
    void deveNegarPacienteAutenticado() {
        Usuario paciente = new Usuario("paciente@example.com", "senha-hash", TipoPerfil.PACIENTE);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(paciente, null, paciente.getAuthorities());

        assertFalse(authorization.podeGerenciar(authentication));
    }

    @Test
    void deveNegarUsuarioNaoAutenticado() {
        Usuario profissional = new Usuario("profissional@example.com", "senha-hash", TipoPerfil.PROFISSIONAL_SAUDE);
        var authentication = UsernamePasswordAuthenticationToken.unauthenticated(profissional, null);

        assertFalse(authorization.podeGerenciar(authentication));
    }

    @Test
    void deveNegarAutenticacaoAusente() {
        assertFalse(authorization.podeGerenciar(null));
    }
}