package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalSaudeRepository profissionalSaudeRepository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void adminTemPerfilParaConseguirFazerLogin() {
        Usuario admin = new Usuario("admin@carebridge.com", "hash", TipoPerfil.ADMIN);
        admin.setId(UUID.randomUUID());

        var perfil = service.obterPerfil(admin);

        assertEquals(TipoPerfil.ADMIN, perfil.tipoPerfil());
        assertEquals("admin@carebridge.com", perfil.email());
    }
}
