package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.oportunidade.triagem.JsonListConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;

@Converter
public class RespostasTriagemConverter extends JsonListConverter<RespostaTriagem> {
    public RespostasTriagemConverter() {
        super(new TypeReference<>() {
        });
    }
}
