package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.ContaService;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService(new ContaService(usuarioRepository, passwordEncoder), pacienteRepository);
    }

    @Test
    void deveRetornarConflitoQuandoEmailJaEstiverCadastrado() {
        CadastroPacienteDto cadastro = criarCadastro("PACIENTE@EXAMPLE.COM", "+55 (11) 99999-9999", true);
        when(usuarioRepository.existsByEmailIgnoreCase("paciente@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pacienteService.cadastrarPaciente(cadastro)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Já existe uma conta cadastrada com este e-mail", exception.getReason());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveCadastrarPacienteNormalizandoEmailTelefoneEForcandoPerfilPaciente() {
        CadastroPacienteDto cadastro = new CadastroPacienteDto(
                new SignUpUserDto("  ANA.SOUZA@EXAMPLE.COM  ", "Senha123!", TipoPerfil.PROFISSIONAL_SAUDE, true),
                "Ana de Souza",
                LocalDate.of(1990, 5, 21),
                "+55 (11) 99999-9999",
                "Rua das Flores, 123",
                true
        );

        when(usuarioRepository.existsByEmailIgnoreCase("ana.souza@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha123!")).thenReturn("senha-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.cadastrarPaciente(cadastro);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioSalvo = usuarioCaptor.getValue();

        assertEquals("ana.souza@example.com", usuarioSalvo.getEmail());
        assertEquals("senha-hash", usuarioSalvo.getSenhaHash());
        assertEquals(TipoPerfil.PACIENTE, usuarioSalvo.getTipoPerfil());
        assertTrue(usuarioSalvo.isAcceptedTerms());
        assertNotNull(usuarioSalvo.getTermosAceitosEm());

        ArgumentCaptor<Paciente> pacienteCaptor = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).save(pacienteCaptor.capture());
        Paciente pacienteSalvo = pacienteCaptor.getValue();

        assertEquals("5511999999999", pacienteSalvo.getTelefone());
        assertEquals(usuarioSalvo, pacienteSalvo.getUsuario());
    }

    @Test
    void deveRejeitarQuandoTermosNaoForemAceitos() {
        CadastroPacienteDto cadastro = criarCadastro("paciente@example.com", "(11) 99999-9999", false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pacienteService.cadastrarPaciente(cadastro)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    private CadastroPacienteDto criarCadastro(String email, String telefone, Boolean acceptedTerms) {
        return new CadastroPacienteDto(
                new SignUpUserDto(email, "Senha123!", TipoPerfil.PACIENTE, true),
                "Paciente Teste",
                LocalDate.of(1990, 5, 21),
                telefone,
                "Endereço de teste, 123",
                acceptedTerms
        );
    }
}