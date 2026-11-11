package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "profissionais_saude")
@Getter
@Setter
@NoArgsConstructor
public class ProfissionalSaude {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String coren;

    @Column(nullable = false)
    private String especialidade;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(
            name = "usuario_id",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    public ProfissionalSaude(String coren, String especialidade, Usuario usuario) {
        this.coren = coren;
        this.especialidade = especialidade;
        this.usuario = usuario;
    }
}