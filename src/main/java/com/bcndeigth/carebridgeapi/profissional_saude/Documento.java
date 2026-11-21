package com.bcndeigth.carebridgeapi.profissional_saude;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Documento {
    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    private String tipo;

    @Column(name = "arquivo", columnDefinition = "bytea")
    private byte[] arquivo;

    public Documento(String nomeArquivo, String tipo, byte[] arquivo) {
        this.nomeArquivo = nomeArquivo;
        this.tipo = tipo;
        this.arquivo = arquivo;
    }
}
