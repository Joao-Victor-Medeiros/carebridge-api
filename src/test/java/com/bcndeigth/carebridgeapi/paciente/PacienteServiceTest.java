package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.users.UsuarioRepository;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        pacienteService = new PacienteService(usuarioRepository, pacienteRepository, passwordEncoder);
    }

    @Test
    void deveRetornarConflitoQuandoEmailJaEstiverCadastrado() {
        CadastroPacienteDto cadastro = criarCadastro("paciente@example.com");
        when(usuarioRepository.existsByEmail("paciente@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pacienteService.cadastrarPaciente(cadastro)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Já existe um usuário cadastrado com este e-mail.", exception.getReason());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void deveCadastrarPacienteQuandoEmailEstiverDisponivel() {
        CadastroPacienteDto cadastro = criarCadastro("novo.paciente@example.com");
        when(usuarioRepository.existsByEmail("novo.paciente@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha123!")).thenReturn("senha-hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pacienteService.cadastrarPaciente(cadastro);

        verify(usuarioRepository).save(any(Usuario.class));
        verify(pacienteRepository).save(any(Paciente.class));
    }

    private CadastroPacienteDto criarCadastro(String email) {
        return new CadastroPacienteDto(
                new SignUpUserDto(email, "Senha123!", TipoPerfil.PACIENTE),
                "Paciente Teste",
                LocalDate.of(1990, 5, 21),
                "+55 11 99999-9999",
                "Endereço de teste, 123"
        );
    }
}