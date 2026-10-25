package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.opportunities.FormularioTriagem;
import com.bcndeigth.carebridgeapi.opportunities.Oportunidade;
import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profissional_saude")
@Getter
@Setter
@NoArgsConstructor
public class ProfissionalSaude {
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    @ElementCollection
    @CollectionTable(
            name = "documentos_registro",
            joinColumns = @JoinColumn(name = "profissional_id")
    )
    private List<Documento> documentosRegistro = new ArrayList<>();

    @Column(name = "documento_registro", length = 4096)
    private String documentoRegistro;

    @PrePersist
    @PreUpdate
    private void atualizarReferenciaDocumentos() {
        documentoRegistro = JSON_MAPPER.writeValueAsString(
                documentosRegistro.stream().map(Documento::getNomeArquivo).toList());
    }

    @Column(name = "biografia_ou_descricao", columnDefinition = "text")
    private String biografiaOuDescricao;

    @Column(name = "validado_institucionalmente", nullable = false)
    private boolean validadoInstitucionalmente = false;

    @OneToMany(mappedBy = "ofertante", fetch = FetchType.LAZY)
    private List<Oportunidade> oportunidades = new ArrayList<>();

    @OneToMany(mappedBy = "ofertante", fetch = FetchType.LAZY)
    private List<FormularioTriagem> formulariosTriagem = new ArrayList<>();

    public ProfissionalSaude(String nome, List<Documento> documentosRegistro, String biografiaOuDescricao, Usuario usuario) {
        this.nome = nome;
        this.documentosRegistro = documentosRegistro;
        this.biografiaOuDescricao = biografiaOuDescricao;
        this.usuario = usuario;
    }
}