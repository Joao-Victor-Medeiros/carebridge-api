package com.bcndeigth.carebridgeapi.inscricao.dto;

import com.bcndeigth.carebridgeapi.inscricao.StatusInscricao;

public record DecisaoRequest(StatusInscricao status, String motivo, InscricaoResponse.Combinado combinado) {
}
