package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String nome,
        String email,
        TipoPerfil tipoPerfil,
        LocalDate dataNascimento,
        String telefone,
        @JsonInclude(JsonInclude.Include.NON_NULL) String verification
) {
}
