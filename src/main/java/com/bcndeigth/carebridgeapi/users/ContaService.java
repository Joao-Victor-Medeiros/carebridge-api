package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
public class ContaService {

    private static final String EMAIL_EM_USO = "Já existe uma conta cadastrada com este e-mail";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario criar(SignUpUserDto conta, Boolean aceitouTermos, TipoPerfil perfil) {
        if (conta == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados da conta são obrigatórios.");
        }
        exigir(conta.email(), "O e-mail é obrigatório.");
        exigir(conta.senha(), "A senha é obrigatória.");
        PoliticaSenha.validar(conta.senha());
        if (!Boolean.TRUE.equals(aceitouTermos)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário aceitar os termos de uso e privacidade.");
        }

        String email = conta.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_EM_USO);
        }

        try {
            return usuarioRepository.save(new Usuario(email, passwordEncoder.encode(conta.senha()), perfil, Instant.now()));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_EM_USO, ex);
        }
    }

    public static void exigir(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
    }
}
