package com.bcndeigth.carebridgeapi.oportunidade.dto;

import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.ModoTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;

import java.util.List;

public record TriagemRequest(
        ModoTriagem modo,
        List<Pergunta> perguntas,
        List<DocumentoExigido> documentosExigidos
) {
}
