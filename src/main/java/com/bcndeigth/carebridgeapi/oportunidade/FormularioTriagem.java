package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentoExigido;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.DocumentosExigidosConverter;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.ModoTriagem;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.Pergunta;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.PerguntasConverter;
import com.bcndeigth.carebridgeapi.oportunidade.triagem.TriagemPadrao;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "formulario_triagem")
@Getter
@Setter
@NoArgsConstructor
public class FormularioTriagem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oportunidade_id", nullable = false, unique = true)
    private Oportunidade oportunidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModoTriagem modo = ModoTriagem.PADRAO;

    @Convert(converter = PerguntasConverter.class)
    @Column(columnDefinition = "text")
    private List<Pergunta> perguntas = new ArrayList<>();

    @Convert(converter = DocumentosExigidosConverter.class)
    @Column(name = "documentos_exigidos", columnDefinition = "text")
    private List<DocumentoExigido> documentosExigidos = new ArrayList<>();

    public List<Pergunta> perguntasEfetivas() {
        return modo == ModoTriagem.PADRAO ? TriagemPadrao.PERGUNTAS : perguntas;
    }
}
