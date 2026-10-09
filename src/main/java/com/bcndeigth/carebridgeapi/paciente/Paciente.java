package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.opportunities.Inscricao;
import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String telefone;

    @Column(columnDefinition = "text")
    private String endereco;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes = new ArrayList<>();
}
