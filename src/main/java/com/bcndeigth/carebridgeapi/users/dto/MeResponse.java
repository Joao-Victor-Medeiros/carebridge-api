package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.paciente.Paciente;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.SituacaoRegistro;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;

import java.time.LocalDate;
import java.util.UUID;

public record MeResponse(
        UUID id,
        String email,
        TipoPerfil tipoPerfil,
        String nome,
        LocalDate dataNascimento,
        String telefone,
        SituacaoRegistro situacaoRegistro,
        String conselho,
        String uf,
        String numeroRegistro,
        String especialidade,
        String ondeAtende
) {
    public static MeResponse de(Usuario usuario, Paciente paciente) {
        return new MeResponse(usuario.getId(), usuario.getEmail(), usuario.getTipoPerfil(), paciente.getNome(),
                paciente.getDataNascimento(), paciente.getTelefone(), null, null, null, null, null, null);
    }

    public static MeResponse de(ProfissionalSaude profissional) {
        Usuario usuario = profissional.getUsuario();
        return new MeResponse(usuario.getId(), usuario.getEmail(), usuario.getTipoPerfil(), profissional.getNome(),
                null, null, profissional.getSituacaoRegistro(), profissional.getConselho(), profissional.getUf(),
                profissional.getNumeroRegistro(), profissional.getEspecialidade(), profissional.getOndeAtende());
    }

    public static MeResponse de(Usuario usuario) {
        return new MeResponse(usuario.getId(), usuario.getEmail(), usuario.getTipoPerfil(), null,
                null, null, null, null, null, null, null, null);
    }
}
