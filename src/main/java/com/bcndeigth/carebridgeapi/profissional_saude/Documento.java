package com.bcndeigth.carebridgeapi.profissional_saude;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Documento {
    private String tipo; // Ex.: "Diploma" ou "Carteirinha"
    private String arquivo; // Caminho ou referência ao arquivo (ex.: URL ou base64)

    public Documento(String tipo, String arquivo) {
        this.tipo = tipo;
        this.arquivo = arquivo;
    }
}
