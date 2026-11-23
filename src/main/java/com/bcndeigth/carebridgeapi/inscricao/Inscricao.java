package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;
import com.bcndeigth.carebridgeapi.paciente.Paciente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inscricoes")
@Getter
@Setter
@NoArgsConstructor
public class Inscricao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String protocolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oportunidade_id", nullable = false)
    private Oportunidade oportunidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusInscricao status;

    @Convert(converter = RespostasTriagemConverter.class)
    @Column(name = "dados_respostas_triagem", columnDefinition = "text")
    private List<RespostaTriagem> respostasTriagem = new ArrayList<>();

    @OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("enviadoEm ASC")
    private List<DocumentoInscricao> documentos = new ArrayList<>();

    @Column(name = "consentimento_dados_saude_em")
    private Instant consentimentoDadosSaudeEm;

    @Column(name = "data_inscricao", nullable = false, updatable = false)
    private Instant dataInscricao;

    @Column(name = "prazo_resposta", nullable = false)
    private Instant prazoResposta;

    @Column(name = "decidida_em")
    private Instant decididaEm;

    @Column(name = "motivo_decisao", columnDefinition = "text")
    private String motivoDecisao;

    @Column(name = "pedido_informacao_texto", columnDefinition = "text")
    private String pedidoInformacaoTexto;

    @Column(name = "pedido_informacao_prazo")
    private Instant pedidoInformacaoPrazo;

    @Column(name = "combinado_quando")
    private String combinadoQuando;

    @Column(name = "combinado_onde")
    private String combinadoOnde;

    @Column(name = "combinado_levar")
    private String combinadoLevar;

    public void adicionarDocumento(DocumentoInscricao documento) {
        documento.setInscricao(this);
        documentos.add(documento);
    }

    public boolean doPaciente(UUID pacienteId) {
        return paciente.getId().equals(pacienteId);
    }

    public boolean doProfissional(UUID profissionalId) {
        return oportunidade.getOfertante().getId().equals(profissionalId);
    }
}
