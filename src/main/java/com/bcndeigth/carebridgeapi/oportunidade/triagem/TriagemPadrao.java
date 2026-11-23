package com.bcndeigth.carebridgeapi.oportunidade.triagem;

import java.util.List;

public final class TriagemPadrao {

    public static final List<Pergunta> PERGUNTAS = List.of(
            Pergunta.texto("motivo", "Qual o motivo principal?", TipoPergunta.TEXTO_LONGO, 10, 300),
            Pergunta.multipla("tempo", "Há quanto tempo?", true, false, null,
                    "Menos de 1 semana", "De 1 a 4 semanas", "De 1 a 6 meses", "Mais de 6 meses"),
            Pergunta.simNao("diagnostico", "Já tem diagnóstico?"),
            Pergunta.condicional("qual-diagnostico", "Qual diagnóstico?", "diagnostico"),
            Pergunta.simNao("medicacao", "Usa medicação contínua?"),
            Pergunta.condicional("quais-medicacoes", "Quais medicações?", "medicacao"),
            Pergunta.multipla("urgencia", "Urgência percebida", true, false,
                    "Alta: atrapalha suas atividades do dia a dia.", "Baixa", "Média", "Alta"),
            Pergunta.multipla("horario", "Melhor horário", false, true, null, "Manhã", "Tarde", "Noite")
    );

    private TriagemPadrao() {
    }
}
