package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
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
@Table(name = "mensagens")
@Getter
@Setter
@NoArgsConstructor
public class Mensagem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscricao_id", nullable = false)
    private Inscricao inscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPerfil autor;

    @Column(nullable = false, columnDefinition = "text")
    private String texto;

    @Column(name = "enviada_em", nullable = false)
    private Instant enviadaEm;

    @Column(nullable = false)
    private boolean lida;

    public Mensagem(Inscricao inscricao, TipoPerfil autor, String texto, Instant enviadaEm) {
        this.inscricao = inscricao;
        this.autor = autor;
        this.texto = texto;
        this.enviadaEm = enviadaEm;
    }
}
