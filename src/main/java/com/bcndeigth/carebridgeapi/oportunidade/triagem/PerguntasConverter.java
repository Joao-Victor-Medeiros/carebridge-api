package com.bcndeigth.carebridgeapi.oportunidade.triagem;

import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;

@Converter
public class PerguntasConverter extends JsonListConverter<Pergunta> {
    public PerguntasConverter() {
        super(new TypeReference<>() {
        });
    }
}
