package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.TipoPergunta;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.TriagemPadrao;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTriagemTest {

    @Test
    void aceitaFormularioPadraoCompletoEIgnoraPerguntaEscondida() {
        List<RespostaTriagem> aceitas = ValidadorTriagem.validar(TriagemPadrao.PERGUNTAS, respostas("Não", "Não"));

        assertTrue(aceitas.stream().noneMatch(r -> r.perguntaId().equals("qual-diagnostico")));
        assertEquals("Qual o motivo principal?", aceitas.getFirst().pergunta());
    }

    @Test
    void perguntaCondicionalPassaASerObrigatoria() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ValidadorTriagem.validar(TriagemPadrao.PERGUNTAS, respostas("Sim", "Não")));

        assertTrue(ex.getReason().startsWith("Qual diagnóstico?"));
    }

    @Test
    void recusaOpcaoForaDaListaEVariasNaoPermitidas() {
        List<RespostaTriagem> base = respostas("Não", "Não");
        base.set(1, new RespostaTriagem("tempo", "", "Há anos"));
        assertThrows(ResponseStatusException.class, () -> ValidadorTriagem.validar(TriagemPadrao.PERGUNTAS, base));

        List<RespostaTriagem> varias = respostas("Não", "Não");
        varias.add(new RespostaTriagem("horario", "", "Manhã, Noite"));
        assertEquals("Manhã, Noite", ValidadorTriagem.validar(TriagemPadrao.PERGUNTAS, varias).getLast().resposta());
    }

    @Test
    void respeitaMinimoDeCaracteres() {
        List<RespostaTriagem> curta = respostas("Não", "Não");
        curta.set(0, new RespostaTriagem("motivo", "", "Dor"));

        assertThrows(ResponseStatusException.class, () -> ValidadorTriagem.validar(TriagemPadrao.PERGUNTAS, curta));
    }

    @Test
    void dependenciaSemRespostaNaoDerrubaAInscricao() {
        Pergunta escola = new Pergunta("escola", "Frequenta a escola?", TipoPergunta.SIM_NAO, true, null, null, null, null, null, null);
        Pergunta serie = new Pergunta("serie", "Qual a série?", TipoPergunta.TEXTO_CURTO, true, null, null, null, null, null,
                new Pergunta.Dependencia("escola", null));

        List<RespostaTriagem> aceitas = ValidadorTriagem.validar(List.of(escola, serie),
                List.of(new RespostaTriagem("escola", "", "Sim")));

        assertEquals(1, aceitas.size());
    }

    static List<RespostaTriagem> respostas(String diagnostico, String medicacao) {
        return new ArrayList<>(List.of(
                new RespostaTriagem("motivo", "", "Dor no joelho direito ao caminhar"),
                new RespostaTriagem("tempo", "", "De 1 a 6 meses"),
                new RespostaTriagem("diagnostico", "", diagnostico),
                new RespostaTriagem("medicacao", "", medicacao),
                new RespostaTriagem("urgencia", "", "Média")
        ));
    }
}
