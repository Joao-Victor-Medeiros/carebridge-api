package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;

public record CadastroResponse(
        String email,
        TipoPerfil tipoPerfil,
        boolean exigeVerificacao,
        Integer tamanhoCodigo
) {
    public static CadastroResponse semVerificacao(String email, TipoPerfil tipoPerfil) {
        return new CadastroResponse(email, tipoPerfil, false, null);
    }
}
