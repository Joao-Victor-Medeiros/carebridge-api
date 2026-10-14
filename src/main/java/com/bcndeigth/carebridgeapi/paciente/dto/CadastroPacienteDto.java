package com.bcndeigth.carebridgeapi.paciente.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;

import java.time.LocalDate;

public record CadastroPacienteDto(
        SignUpUserDto usuarioDto,
        LocalDate dataNascimento,
        String telefone,
        String endereco
) {}
