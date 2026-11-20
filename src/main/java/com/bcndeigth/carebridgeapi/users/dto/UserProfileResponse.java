package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String nome,
        String email,
        TipoPerfil tipoPerfil,
        LocalDate dataNascimento,
        String telefone,
        String verification
) {
}