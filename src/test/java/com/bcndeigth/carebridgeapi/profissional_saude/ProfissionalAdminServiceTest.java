package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.dto.MeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalAdminServiceTest {

    @Mock
    private ProfissionalSaudeRepository repository;

    @InjectMocks
    private ProfissionalAdminService service;

    @Test
    void deveAprovarProfissionalEmVerificacao() {
        ProfissionalSaude profissional = profissional(SituacaoRegistro.EM_VERIFICACAO);
        when(repository.findById(profissional.getId())).thenReturn(Optional.of(profissional));

        MeResponse resposta = service.alterarSituacao(profissional.getId(), SituacaoRegistro.VERIFICADO);

        assertEquals(SituacaoRegistro.VERIFICADO, profissional.getSituacaoRegistro());
        assertEquals(SituacaoRegistro.VERIFICADO, resposta.situacaoRegistro());
        assertEquals("CRM", resposta.conselho());
    }

    @Test
    void naoDeveReavaliarProfissionalJaAvaliado() {
        ProfissionalSaude profissional = profissional(SituacaoRegistro.RECUSADO);
        when(repository.findById(profissional.getId())).thenReturn(Optional.of(profissional));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.alterarSituacao(profissional.getId(), SituacaoRegistro.VERIFICADO));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void deveListarSoOsPendentes() {
        ProfissionalSaude profissional = profissional(SituacaoRegistro.EM_VERIFICACAO);
        when(repository.findBySituacaoRegistroOrderByNomeAsc(SituacaoRegistro.EM_VERIFICACAO))
                .thenReturn(List.of(profissional));

        List<MeResponse> pendentes = service.pendentes();

        assertEquals(1, pendentes.size());
        assertEquals("maria@example.com", pendentes.getFirst().email());
    }

    private ProfissionalSaude profissional(SituacaoRegistro situacao) {
        Usuario usuario = new Usuario("maria@example.com", "hash", TipoPerfil.PROFISSIONAL_SAUDE, Instant.now());
        usuario.setId(UUID.randomUUID());
        ProfissionalSaude profissional = new ProfissionalSaude("Maria Santos", "CRM", "DF", "123456",
                "cardiologia", "Brasília - DF", List.of(), null, usuario);
        profissional.setId(usuario.getId());
        profissional.setSituacaoRegistro(situacao);
        return profissional;
    }
}
