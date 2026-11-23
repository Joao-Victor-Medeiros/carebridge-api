package com.bcndeigth.carebridgeapi.inscricao.dto;

import com.bcndeigth.carebridgeapi.inscricao.Mensagem;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;

import java.time.Instant;
import java.util.UUID;

public record MensagemResponse(UUID id, TipoPerfil autor, String nomeAutor, String texto, Instant enviadaEm) {

    public static MensagemResponse de(Mensagem mensagem) {
        var inscricao = mensagem.getInscricao();
        String nome = mensagem.getAutor() == TipoPerfil.PACIENTE
                ? inscricao.getPaciente().getNome()
                : inscricao.getOportunidade().getOfertante().getNome();
        return new MensagemResponse(mensagem.getId(), mensagem.getAutor(), nome, mensagem.getTexto(),
                mensagem.getEnviadaEm());
    }
}
