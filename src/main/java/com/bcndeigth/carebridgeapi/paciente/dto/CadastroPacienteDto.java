package com.bcndeigth.carebridgeapi.paciente.dto;

import com.bcndeigth.carebridgeapi.users.Usuario;

import java.time.LocalDate;

public record CadastroPacienteDto(
        Usuario usuario,
        LocalDate dataNascimento,
        String telefone,
        String endereco
) {}
