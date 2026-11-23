package com.bcndeigth.carebridgeapi.oportunidade;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class PublicoAlvoConverter implements AttributeConverter<List<PublicoAlvo>, String> {

    @Override
    public String convertToDatabaseColumn(List<PublicoAlvo> publico) {
        return publico == null || publico.isEmpty() ? null
                : publico.stream().distinct().sorted().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public List<PublicoAlvo> convertToEntityAttribute(String valor) {
        return valor == null || valor.isBlank() ? List.of()
                : Arrays.stream(valor.split(",")).map(PublicoAlvo::valueOf).toList();
    }
}
