package com.bcndeigth.carebridgeapi.paciente.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados necessários para cadastrar um paciente.")
public record CadastroPacienteDto(
        @Schema(description = "Dados da conta do paciente.")
        SignUpUserDto usuarioDto,

        @Schema(description = "Data de nascimento do paciente no formato ISO-8601.", example = "1990-05-21")
        LocalDate dataNascimento,

        @Schema(description = "Telefone de contato.", example = "+55 11 99999-9999")
        String telefone,

        @Schema(description = "Endereço do paciente.", example = "Rua das Flores, 123, São Paulo - SP")
        String endereco
) {}
