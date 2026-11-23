package com.bcndeigth.carebridgeapi.oportunidade.triagem;

import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;

@Converter
public class DocumentosExigidosConverter extends JsonListConverter<DocumentoExigido> {
    public DocumentosExigidosConverter() {
        super(new TypeReference<>() {
        });
    }
}
