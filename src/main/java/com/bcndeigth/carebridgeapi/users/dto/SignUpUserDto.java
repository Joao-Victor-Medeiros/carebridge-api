package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados usados para criar uma conta de usuário.")
public record SignUpUserDto(
        @Schema(description = "Nome completo do usuário.", example = "Ana Souza")
        String nome,

        @Schema(description = "Endereço de e-mail do usuário.", example = "ana.souza@example.com")
        String email,

        @Schema(description = "Senha da conta.", example = "SenhaSegura123!", format = "password")
        String senha,

        @Schema(description = "Perfil da conta.", example = "PROFISSIONAL_SAUDE")
        TipoPerfil tipoPerfil
) {
}