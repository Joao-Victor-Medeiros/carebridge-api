package com.bcndeigth.carebridgeapi.inscricao;

import java.util.Set;

public enum StatusInscricao {
    RECEBIDA,
    EM_ANALISE,
    AGUARDANDO_INFORMACOES,
    APROVADA,
    RECUSADA,
    CANCELADA;

    public static final Set<StatusInscricao> ABERTAS = Set.of(RECEBIDA, EM_ANALISE, AGUARDANDO_INFORMACOES);

    public boolean aberta() {
        return ABERTAS.contains(this);
    }
}
