package com.bcndeigth.carebridgeapi.oportunidade.triagem;

import jakarta.persistence.AttributeConverter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

abstract class JsonListConverter<T> implements AttributeConverter<List<T>, String> {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final TypeReference<List<T>> tipo;

    JsonListConverter(TypeReference<List<T>> tipo) {
        this.tipo = tipo;
    }

    @Override
    public String convertToDatabaseColumn(List<T> lista) {
        return lista == null ? null : JSON.writeValueAsString(lista);
    }

    @Override
    public List<T> convertToEntityAttribute(String json) {
        return json == null || json.isBlank() ? List.of() : JSON.readValue(json, tipo);
    }
}
