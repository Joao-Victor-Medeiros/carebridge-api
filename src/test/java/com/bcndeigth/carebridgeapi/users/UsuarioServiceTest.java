package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.paciente.Paciente;
import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;
import com.bcndeigth.carebridgeapi.users.dto.MeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalSaudeRepository profissionalSaudeRepository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void perfilDoPacienteNaoTrazDadosDeRegistro() {
        Usuario usuario = usuario("ana@example.com", TipoPerfil.PACIENTE);
        Paciente paciente = new Paciente("Ana Souza", LocalDate.of(1988, 3, 12), "61999990000", null, usuario);
        when(pacienteRepository.findById(usuario.getId())).thenReturn(Optional.of(paciente));

        MeResponse me = service.obterPerfil(usuario);

        assertEquals("Ana Souza", me.nome());
        assertEquals(LocalDate.of(1988, 3, 12), me.dataNascimento());
        assertNull(me.situacaoRegistro());
        assertNull(me.conselho());
    }

    @Test
    void perfilDoProfissionalTrazRegistroESituacao() {
        Usuario usuario = usuario("marcos@example.com", TipoPerfil.PROFISSIONAL_SAUDE);
        when(profissionalSaudeRepository.findById(usuario.getId()))
                .thenReturn(Optional.of(profissional(usuario, SituacaoRegistro.EM_VERIFICACAO)));

        MeResponse me = service.obterPerfil(usuario);

        assertEquals(SituacaoRegistro.EM_VERIFICACAO, me.situacaoRegistro());
        assertEquals("123456", me.numeroRegistro());
        assertNull(me.dataNascimento());
    }

    @Test
    void soProfissionalVerificadoEhValidadoInstitucionalmente() {
        Usuario usuario = usuario("marcos@example.com", TipoPerfil.PROFISSIONAL_SAUDE);
        when(profissionalSaudeRepository.findById(usuario.getId()))
                .thenReturn(Optional.of(profissional(usuario, SituacaoRegistro.EM_VERIFICACAO)))
                .thenReturn(Optional.of(profissional(usuario, SituacaoRegistro.VERIFICADO)));

        assertFalse(service.validadoInstitucionalmente(usuario));
        assertTrue(service.validadoInstitucionalmente(usuario));
        assertFalse(service.validadoInstitucionalmente(usuario("ana@example.com", TipoPerfil.PACIENTE)));
    }

    @Test
    void adminTemPerfilParaConseguirFazerLogin() {
        MeResponse perfil = service.obterPerfil(usuario("admin@carebridge.com", TipoPerfil.ADMIN));

        assertEquals(TipoPerfil.ADMIN, perfil.tipoPerfil());
        assertEquals("admin@carebridge.com", perfil.email());
    }

    private Usuario usuario(String email, TipoPerfil perfil) {
        Usuario usuario = new Usuario(email, "hash", perfil, Instant.now());
        usuario.setId(UUID.randomUUID());
        return usuario;
    }

    private ProfissionalSaude profissional(Usuario usuario, SituacaoRegistro situacao) {
        ProfissionalSaude profissional = new ProfissionalSaude("Marcos Vieira", "CRM", "DF", "123456",
                "cardiologia", "Instituto Movida", List.of(), null, usuario);
        profissional.setSituacaoRegistro(situacao);
        return profissional;
    }
}
