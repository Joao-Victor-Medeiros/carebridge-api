package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.ContaService;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalSaudeServiceTest {

    @Mock
    private ProfissionalSaudeRepository profissionalSaudeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ProfissionalSaudeService service;

    @BeforeEach
    void setUp() {
        service = new ProfissionalSaudeService(new ContaService(usuarioRepository, passwordEncoder), profissionalSaudeRepository);
    }

    @Test
    void devePersistirDadosProfissionaisNormalizarEmailERegistrarTermos() {
        CadastroProfissionalSaudeDto dto = new CadastroProfissionalSaudeDto(
                new SignUpUserDto("  MARIA.SANTOS@EXAMPLE.COM ", "Senha123!", TipoPerfil.PACIENTE),
                "Maria Santos",
                "CRM",
                "df",
                "123456",
                "Cardiologia",
                "Brasília - DF",
                "Cardiologista",
                true
        );

        when(usuarioRepository.existsByEmailIgnoreCase("maria.santos@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha123!")).thenReturn("senha-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        CadastroResponse response = service.cadastrarProfissionalSaude(dto, List.<MultipartFile>of());

        assertEquals("maria.santos@example.com", response.email());
        assertNotNull(response);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioSalvo = usuarioCaptor.getValue();

        assertEquals("maria.santos@example.com", usuarioSalvo.getEmail());
        assertEquals(TipoPerfil.PROFISSIONAL_SAUDE, usuarioSalvo.getTipoPerfil());
        assertTrue(usuarioSalvo.isAcceptedTerms());
        assertNotNull(usuarioSalvo.getTermosAceitosEm());

        ArgumentCaptor<ProfissionalSaude> profCaptor = ArgumentCaptor.forClass(ProfissionalSaude.class);
        verify(profissionalSaudeRepository).save(profCaptor.capture());
        ProfissionalSaude profSalvo = profCaptor.getValue();

        assertEquals("Maria Santos", profSalvo.getNome());
        assertEquals("CRM", profSalvo.getConselho());
        assertEquals("DF", profSalvo.getUf());
        assertEquals("123456", profSalvo.getNumeroRegistro());
        assertEquals("Cardiologia", profSalvo.getEspecialidade());
        assertEquals("Brasília - DF", profSalvo.getOndeAtende());
        assertEquals(usuarioSalvo, profSalvo.getUsuario());
    }

    @Test
    void deveRetornar409QuandoEmailJaExistirNoCadastroProfissional() {
        CadastroProfissionalSaudeDto dto = new CadastroProfissionalSaudeDto(
                new SignUpUserDto("MARIA@EXAMPLE.COM", "Senha123!", TipoPerfil.PROFISSIONAL_SAUDE),
                "Maria Santos",
                "CRM",
                "DF",
                "123456",
                "Cardiologia",
                "Brasília - DF",
                "Cardiologista",
                true
        );

        when(usuarioRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrarProfissionalSaude(dto, List.<MultipartFile>of())
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Já existe uma conta cadastrada com este e-mail", ex.getReason());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(profissionalSaudeRepository, never()).save(any(ProfissionalSaude.class));
    }

    @Test
    void deveRejeitarNumeroDeRegistroForaDoFormato() {
        CadastroProfissionalSaudeDto dto = new CadastroProfissionalSaudeDto(
                new SignUpUserDto("maria@example.com", "Senha123!", TipoPerfil.PROFISSIONAL_SAUDE),
                "Maria Santos", "CRM", "DF", "12-AB", "cardiologia", "Brasília - DF", null, true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cadastrarProfissionalSaude(dto, List.<MultipartFile>of())
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveNascerEmVerificacao() {
        CadastroProfissionalSaudeDto dto = new CadastroProfissionalSaudeDto(
                new SignUpUserDto("maria@example.com", "Senha123!", TipoPerfil.PROFISSIONAL_SAUDE),
                "Maria Santos", "CRM", "DF", "123456", "cardiologia", "Brasília - DF", null, true);
        when(usuarioRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha123!")).thenReturn("senha-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cadastrarProfissionalSaude(dto, List.<MultipartFile>of());

        ArgumentCaptor<ProfissionalSaude> captor = ArgumentCaptor.forClass(ProfissionalSaude.class);
        verify(profissionalSaudeRepository).save(captor.capture());
        assertEquals(SituacaoRegistro.EM_VERIFICACAO, captor.getValue().getSituacaoRegistro());
    }
}