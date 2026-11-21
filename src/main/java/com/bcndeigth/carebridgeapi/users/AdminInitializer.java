package com.bcndeigth.carebridgeapi.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByTipoPerfil(TipoPerfil.ADMIN)) {
            return;
        }

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Nenhum usuário ADMIN encontrado e credenciais iniciais (ADMIN_EMAIL / ADMIN_PASSWORD) não foram informadas.");
            return;
        }

        String emailNormalizado = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            log.warn("E-mail do administrador inicial já está em uso por outra conta; criação automática ignorada.");
            return;
        }

        Usuario admin = new Usuario(
                emailNormalizado,
                passwordEncoder.encode(adminPassword),
                TipoPerfil.ADMIN,
                true,
                Instant.now()
        );

        usuarioRepository.save(admin);
        log.info("Usuário administrador inicial criado com sucesso para {}", emailNormalizado);
    }
}