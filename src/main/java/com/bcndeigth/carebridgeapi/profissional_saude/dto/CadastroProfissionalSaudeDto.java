package com.bcndeigth.carebridgeapi.profissional_saude.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;

public record CadastroProfissionalSaudeDto(
        SignUpUserDto usuarioDto,
        String documentoRegistro,
        String biografiaOuDescricao
) {}