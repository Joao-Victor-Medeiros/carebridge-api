package com.bcndeigth.carebridgeapi.profissional_saude.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import lombok.NonNull;

import java.util.List;

public record CadastroProfissionalSaudeDto(
        @NonNull
        SignUpUserDto usuarioDto,

        List<String> documentosRegistro,
        String biografiaOuDescricao
) {}
