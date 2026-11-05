package com.bcndeigth.carebridgeapi.users.dto;

public record LoginResponseDTO(
        String token,
        boolean autenticado,
        String tipoPerfil,
        boolean validadoInstitucionalmente
) {
}