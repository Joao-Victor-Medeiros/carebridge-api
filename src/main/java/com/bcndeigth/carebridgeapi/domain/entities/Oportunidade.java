package com.bcndeigth.carebridgeapi.domain.entities;

public class Oportunidade {
    private String id;
    private String ofertanteId;
    private String modalidade;
    private String tipoOferta;
    private int quantidadeVagas;

    public Oportunidade(String id, String ofertanteId, String modalidade, String tipoOferta, int quantidadeVagas) {
        this.id = id;
        this.ofertanteId = ofertanteId;
        this.modalidade = modalidade;
        this.tipoOferta = tipoOferta;
        this.quantidadeVagas = quantidadeVagas;
    }

    public String getId() { return id; }
    public String getOfertanteId() { return ofertanteId; }
    public String getModalidade() { return modalidade; }
    public String getTipoOferta() { return tipoOferta; }
    public int getQuantidadeVagas() { return quantidadeVagas; }
}