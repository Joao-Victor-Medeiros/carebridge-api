package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ValidadorTriagem {

    private static final Set<String> SIM_NAO = Set.of("Sim", "Não");
    private static final String SEPARADOR = ", ";

    private ValidadorTriagem() {
    }

    static List<RespostaTriagem> validar(List<Pergunta> perguntas, List<RespostaTriagem> enviadas) {
        Map<String, String> respostas = new HashMap<>();
        if (enviadas != null) {
            enviadas.stream()
                    .filter(r -> r != null && r.perguntaId() != null && r.resposta() != null)
                    .forEach(r -> respostas.put(r.perguntaId(), r.resposta().trim()));
        }

        List<RespostaTriagem> aceitas = new ArrayList<>();
        for (Pergunta pergunta : perguntas) {
            if (!visivel(pergunta, respostas)) {
                continue;
            }
            String resposta = respostas.getOrDefault(pergunta.id(), "");
            if (resposta.isEmpty()) {
                if (pergunta.obrigatoria()) {
                    throw invalida(pergunta, "precisa de resposta.");
                }
                continue;
            }
            conferir(pergunta, resposta);
            aceitas.add(new RespostaTriagem(pergunta.id(), pergunta.rotulo(), resposta));
        }
        return aceitas;
    }

    private static boolean visivel(Pergunta pergunta, Map<String, String> respostas) {
        var dependencia = pergunta.dependeDe();
        return dependencia == null || Objects.equals(dependencia.resposta(), respostas.get(dependencia.perguntaId()));
    }

    private static void conferir(Pergunta pergunta, String resposta) {
        switch (pergunta.tipo()) {
            case SIM_NAO -> {
                if (!SIM_NAO.contains(resposta)) {
                    throw invalida(pergunta, "aceita só Sim ou Não.");
                }
            }
            case MULTIPLA -> {
                List<String> escolhidas = Boolean.TRUE.equals(pergunta.permiteVarias())
                        ? Arrays.asList(resposta.split(SEPARADOR)) : List.of(resposta);
                List<String> opcoes = pergunta.opcoes() == null ? List.of() : pergunta.opcoes();
                if (!opcoes.containsAll(escolhidas)) {
                    throw invalida(pergunta, "precisa de uma das opções da lista.");
                }
            }
            case TEXTO_CURTO, TEXTO_LONGO -> {
                if (pergunta.minCaracteres() != null && resposta.length() < pergunta.minCaracteres()) {
                    throw invalida(pergunta, "precisa de pelo menos " + pergunta.minCaracteres() + " caracteres.");
                }
                if (pergunta.maxCaracteres() != null && resposta.length() > pergunta.maxCaracteres()) {
                    throw invalida(pergunta, "aceita no máximo " + pergunta.maxCaracteres() + " caracteres.");
                }
            }
        }
    }

    private static ResponseStatusException invalida(Pergunta pergunta, String problema) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, pergunta.rotulo() + " " + problema);
    }
}
