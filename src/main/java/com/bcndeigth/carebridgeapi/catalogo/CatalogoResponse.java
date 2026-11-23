package com.bcndeigth.carebridgeapi.catalogo;

import java.util.List;

public record CatalogoResponse(
        List<Especialidade> especialidades,
        List<Conselho> conselhos,
        List<String> ufs,
        List<Integer> distanciasKm
) {
    public record Especialidade(String id, String nome) {
    }

    public record Conselho(String sigla, String profissao) {
    }
}
