package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.opportunities.Inscricao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {
    @Id
    private Long id;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String telefone;

    @Column(columnDefinition = "text")
    private String endereco;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes = new ArrayList<>();
}
