package com.bcndeigth.carebridgeapi.inscricao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "inscricao_documentos")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoInscricao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscricao_id", nullable = false)
    private Inscricao inscricao;

    @Column(name = "documento_id", length = 60)
    private String documentoId;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(name = "enviado_em", nullable = false)
    private Instant enviadoEm;

    public DocumentoInscricao(String documentoId, String nomeArquivo, long tamanhoBytes, Instant enviadoEm) {
        this.documentoId = documentoId;
        this.nomeArquivo = nomeArquivo;
        this.tamanhoBytes = tamanhoBytes;
        this.enviadoEm = enviadoEm;
    }
}
