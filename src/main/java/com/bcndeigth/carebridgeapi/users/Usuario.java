package com.bcndeigth.carebridgeapi.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_perfil", nullable = false)
    private TipoPerfil tipoPerfil;

<<<<<<< HEAD
    @Column(name = "accepted_terms", nullable = false)
    private boolean acceptedTerms;

    @Column(name = "termos_aceitos_em")
    private Instant termosAceitosEm;

=======
    @Column(name = "termos_aceitos_em", nullable = false, updatable = false)
    private Instant termosAceitosEm;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado;

>>>>>>> origin/develop
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

<<<<<<< HEAD
    public Usuario(String email, String hashedPassword, TipoPerfil tipoPerfil) {
        setEmail(email);
=======
    public Usuario(String email, String hashedPassword, TipoPerfil tipoPerfil, Instant termosAceitosEm) {
        this.email = email.toLowerCase(java.util.Locale.ROOT);
>>>>>>> origin/develop
        this.senhaHash = hashedPassword;
        this.tipoPerfil = tipoPerfil;
        this.termosAceitosEm = termosAceitosEm;
        this.emailVerificado = false;
    }

    public Usuario(String email, String hashedPassword, TipoPerfil tipoPerfil) {
        this(email, hashedPassword, tipoPerfil, Instant.now());
    }

    public Usuario(String email, String hashedPassword, TipoPerfil tipoPerfil, boolean acceptedTerms, Instant termosAceitosEm) {
        setEmail(email);
        this.senhaHash = hashedPassword;
        this.tipoPerfil = tipoPerfil;
        this.acceptedTerms = acceptedTerms;
        this.termosAceitosEm = termosAceitosEm;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getEmail() {
        return email;
    }

    public TipoPerfil getTipoPerfil() {
        return tipoPerfil;
    }

    @PrePersist
    protected void aoCriar() {
        setEmail(this.email);
        if (this.acceptedTerms && this.termosAceitosEm == null) {
            this.termosAceitosEm = Instant.now();
        }
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        setEmail(this.email);
        atualizadoEm = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (tipoPerfil == null) {
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + tipoPerfil.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}