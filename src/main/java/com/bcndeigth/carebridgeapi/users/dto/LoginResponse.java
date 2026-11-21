package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;

public record LoginResponse(
        String token,
        boolean autenticado,
        TipoPerfil tipoPerfil,
        boolean validadoInstitucionalmente,
        long expiraEmSegundos
) {
}
