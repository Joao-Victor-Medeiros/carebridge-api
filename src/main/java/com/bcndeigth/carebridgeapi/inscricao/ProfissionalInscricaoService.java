package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.inscricao.dto.DecisaoRequest;
import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.MensagemResponse;
import com.bcndeigth.carebridgeapi.oportunidade.OportunidadeRepository;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ProfissionalInscricaoService {

    static final int PRAZO_PEDIDO_INFORMACAO_DIAS = 3;
    private static final String A_COMBINAR = "A combinar";

    private final InscricaoRepository inscricaoRepository;
    private final OportunidadeRepository oportunidadeRepository;
    private final InscricaoLeitura leitura;

    public ProfissionalInscricaoService(InscricaoRepository inscricaoRepository,
                                        OportunidadeRepository oportunidadeRepository, InscricaoLeitura leitura) {
        this.inscricaoRepository = inscricaoRepository;
        this.oportunidadeRepository = oportunidadeRepository;
        this.leitura = leitura;
    }

    @Transactional(readOnly = true)
    public List<InscricaoResponse> listar(StatusInscricao status, Usuario usuario) {
        List<Inscricao> inscricoes = status == null
                ? inscricaoRepository.findByOportunidadeOfertanteIdOrderByDataInscricaoDesc(usuario.getId())
                : inscricaoRepository.findByOportunidadeOfertanteIdAndStatusOrderByDataInscricaoDesc(usuario.getId(), status);
        return inscricoes.stream().map(i -> leitura.resumo(i, TipoPerfil.PROFISSIONAL_SAUDE)).toList();
    }

    @Transactional
    public InscricaoResponse detalhar(UUID id, Usuario usuario) {
        Inscricao inscricao = doProfissional(id, usuario);
        leitura.marcarComoLidas(inscricao, TipoPerfil.PROFISSIONAL_SAUDE);
        return leitura.detalhe(inscricao, TipoPerfil.PROFISSIONAL_SAUDE);
    }

    @Transactional
    public InscricaoResponse decidir(UUID id, DecisaoRequest request, Usuario usuario) {
        StatusInscricao decisao = request == null ? null : request.status();
        if (decisao != StatusInscricao.APROVADA && decisao != StatusInscricao.RECUSADA) {
            throw Erros.invalido("A decisão precisa ser APROVADA ou RECUSADA.");
        }
        String motivo = request.motivo() == null ? "" : request.motivo().trim();
        if (decisao == StatusInscricao.RECUSADA && motivo.isEmpty()) {
            throw Erros.invalido("Informe o motivo da recusa.");
        }
        Inscricao inscricao = doProfissional(id, usuario);
        if (!inscricao.getStatus().aberta()) {
            throw Erros.conflito(Erros.JA_DECIDIDA);
        }

        if (decisao == StatusInscricao.APROVADA) {
            if (oportunidadeRepository.ocuparVaga(inscricao.getOportunidade().getId()) == 0) {
                throw Erros.conflito("Não há vagas restantes nesta oportunidade.");
            }
            var combinado = request.combinado();
            inscricao.setCombinadoQuando(ouACombinar(combinado == null ? null : combinado.quando()));
            inscricao.setCombinadoOnde(ouACombinar(combinado == null ? null : combinado.onde()));
            inscricao.setCombinadoLevar(combinado == null || combinado.levar() == null ? "" : combinado.levar().trim());
        } else {
            inscricao.setMotivoDecisao(motivo);
        }
        inscricao.setStatus(decisao);
        inscricao.setDecididaEm(InscricaoLeitura.agora());
        inscricao.setPedidoInformacaoTexto(null);
        inscricao.setPedidoInformacaoPrazo(null);
        return leitura.detalhe(inscricaoRepository.saveAndFlush(inscricao), TipoPerfil.PROFISSIONAL_SAUDE);
    }

    @Transactional
    public InscricaoResponse pedirInformacao(UUID id, String texto, Usuario usuario) {
        if (texto == null || texto.isBlank()) {
            throw Erros.invalido("Escreva o que falta.");
        }
        Inscricao inscricao = doProfissional(id, usuario);
        if (!inscricao.getStatus().aberta()) {
            throw Erros.conflito(Erros.JA_DECIDIDA);
        }
        Instant agora = InscricaoLeitura.agora();
        inscricao.setStatus(StatusInscricao.AGUARDANDO_INFORMACOES);
        inscricao.setPedidoInformacaoTexto(texto.trim());
        inscricao.setPedidoInformacaoPrazo(agora.plus(PRAZO_PEDIDO_INFORMACAO_DIAS, ChronoUnit.DAYS));
        return leitura.detalhe(inscricaoRepository.saveAndFlush(inscricao), TipoPerfil.PROFISSIONAL_SAUDE);
    }

    @Transactional
    public MensagemResponse enviarMensagem(UUID id, String texto, Usuario usuario) {
        return leitura.enviar(doProfissional(id, usuario), TipoPerfil.PROFISSIONAL_SAUDE, texto);
    }

    private Inscricao doProfissional(UUID id, Usuario usuario) {
        return inscricaoRepository.findById(id)
                .filter(i -> i.doProfissional(usuario.getId()))
                .orElseThrow(Erros::naoEncontrada);
    }

    private static String ouACombinar(String valor) {
        return valor == null || valor.isBlank() ? A_COMBINAR : valor.trim();
    }
}
