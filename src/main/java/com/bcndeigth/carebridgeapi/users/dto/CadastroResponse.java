package com.bcndeigth.carebridgeapi.users.dto;

public record CadastroResponse(
        String email,
        String profile,
        int codeLength
) {
}