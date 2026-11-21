package com.bcndeigth.carebridgeapi.inscricao.dto;

public record RespostaPedidoRequest(String texto, Anexo anexo) {
    public record Anexo(String nomeArquivo, Long tamanhoBytes) {
    }
}
