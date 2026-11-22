package com.bcndeigth.carebridgeapi.auth.dto;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipoPerfil")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CadastroPacienteDto.class, name = "PACIENTE"),
        @JsonSubTypes.Type(value = CadastroProfissionalSaudeDto.class, name = "PROFISSIONAL_SAUDE")
})
@Schema(
        description = "Cadastro polimórfico definido por tipoPerfil.",
        oneOf = {CadastroPacienteDto.class, CadastroProfissionalSaudeDto.class},
        discriminatorProperty = "tipoPerfil"
)
public interface CadastroUsuarioRequest {

    SignUpUserDto usuarioDto();

    Boolean acceptedTerms();
}