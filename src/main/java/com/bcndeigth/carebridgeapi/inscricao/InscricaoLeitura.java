package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.MensagemResponse;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
class InscricaoLeitura {

    private final MensagemRepository mensagemRepository;

    InscricaoLeitura(MensagemRepository mensagemRepository) {
        this.mensagemRepository = mensagemRepository;
    }

    InscricaoResponse resumo(Inscricao inscricao, TipoPerfil leitor) {
        return montar(inscricao, leitor, null);
    }

    InscricaoResponse detalhe(Inscricao inscricao, TipoPerfil leitor) {
        List<MensagemResponse> mensagens = mensagemRepository.findByInscricaoIdOrderByEnviadaEmAsc(inscricao.getId())
                .stream().map(MensagemResponse::de).toList();
        return montar(inscricao, leitor, mensagens);
    }

    List<MensagemResponse> mensagens(Inscricao inscricao) {
        return mensagemRepository.findByInscricaoIdOrderByEnviadaEmAsc(inscricao.getId())
                .stream().map(MensagemResponse::de).toList();
    }

    void marcarComoLidas(Inscricao inscricao, TipoPerfil leitor) {
        mensagemRepository.marcarComoLidas(inscricao.getId(), leitor);
    }

    MensagemResponse enviar(Inscricao inscricao, TipoPerfil autor, String texto) {
        if (texto == null || texto.isBlank()) {
            throw Erros.invalido("Escreva a mensagem.");
        }
        if (inscricao.getStatus() != StatusInscricao.APROVADA) {
            throw Erros.conflito("As mensagens ficam disponíveis depois da aprovação.");
        }
        return MensagemResponse.de(mensagemRepository.save(new Mensagem(inscricao, autor, texto.trim(), agora())));
    }

    static Instant agora() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private InscricaoResponse montar(Inscricao inscricao, TipoPerfil leitor, List<MensagemResponse> mensagens) {
        long naoLidas = mensagemRepository.countByInscricaoIdAndAutorNotAndLidaFalse(inscricao.getId(), leitor);
        MensagemResponse ultima = mensagemRepository.findFirstByInscricaoIdOrderByEnviadaEmDesc(inscricao.getId())
                .map(MensagemResponse::de).orElse(null);
        return InscricaoResponse.de(inscricao, naoLidas, ultima, mensagens);
    }
}
