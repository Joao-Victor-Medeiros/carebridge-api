package com.bcndeigth.carebridgeapi.profissional_saude;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Documento {
    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    private String tipo;

    @Column(name = "arquivo", columnDefinition = "bytea")
    private byte[] arquivo;

    public Documento() {

    }

    public Documento(String nomeArquivo, String tipo, byte[] arquivo) {
        this.nomeArquivo = nomeArquivo;
        this.tipo = tipo;
        this.arquivo = arquivo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public String getTipo() {
        return tipo;
    }

    public byte[] getArquivo() {
        return arquivo;
    }
}
