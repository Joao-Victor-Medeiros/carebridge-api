package com.bcndeigth.carebridgeapi.auth;

import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "codigos_verificacao_email")
@Getter
@Setter
@NoArgsConstructor
public class CodigoVerificacaoEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false)
    private String codigoHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FinalidadeCodigo finalidade;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "utilizado_em")
    private Instant utilizadoEm;

    @Column(nullable = false)
    private int tentativas;

    public CodigoVerificacaoEmail(
            Usuario usuario,
            String codigoHash,
            FinalidadeCodigo finalidade,
            Instant criadoEm,
            Instant expiraEm
    ) {
        this.usuario = usuario;
        this.codigoHash = codigoHash;
        this.finalidade = finalidade;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
    }
}