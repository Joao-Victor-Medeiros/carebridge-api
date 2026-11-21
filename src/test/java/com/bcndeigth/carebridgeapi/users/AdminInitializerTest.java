package com.bcndeigth.carebridgeapi.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarAdminQuandoNaoExistirAdminEVariaveisEstiveremConfiguradas() {
        when(usuarioRepository.existsByTipoPerfil(TipoPerfil.ADMIN)).thenReturn(false);
        when(usuarioRepository.existsByEmailIgnoreCase("admin@carebridge.com")).thenReturn(false);
        when(passwordEncoder.encode("SenhaForte@123")).thenReturn("hash-bcrypt");

        AdminInitializer initializer = new AdminInitializer(
                usuarioRepository,
                passwordEncoder,
                "  ADMIN@CAREBRIDGE.COM  ",
                "SenhaForte@123"
        );

        initializer.run(new DefaultApplicationArguments());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();

        assertEquals("admin@carebridge.com", salvo.getEmail());
        assertEquals("hash-bcrypt", salvo.getSenhaHash());
        assertEquals(TipoPerfil.ADMIN, salvo.getTipoPerfil());
        assertTrue(salvo.isAcceptedTerms());
        assertNotNull(salvo.getTermosAceitosEm());
    }

    @Test
    void naoDeveCriarAdminQuandoJaExistirUmAdmin() {
        when(usuarioRepository.existsByTipoPerfil(TipoPerfil.ADMIN)).thenReturn(true);

        AdminInitializer initializer = new AdminInitializer(
                usuarioRepository,
                passwordEncoder,
                "admin@carebridge.com",
                "SenhaForte@123"
        );

        initializer.run(new DefaultApplicationArguments());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}