package com.bcndeigth.carebridgeapi.inscricao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InscricaoRepository extends JpaRepository<Inscricao, UUID> {

    @EntityGraph(attributePaths = {"oportunidade", "oportunidade.ofertante", "paciente"})
    List<Inscricao> findByPacienteIdOrderByDataInscricaoDesc(UUID pacienteId);

    @EntityGraph(attributePaths = {"oportunidade", "oportunidade.ofertante", "paciente"})
    List<Inscricao> findByOportunidadeOfertanteIdOrderByDataInscricaoDesc(UUID profissionalId);

    @EntityGraph(attributePaths = {"oportunidade", "oportunidade.ofertante", "paciente"})
    List<Inscricao> findByOportunidadeOfertanteIdAndStatusOrderByDataInscricaoDesc(UUID profissionalId,
                                                                                    StatusInscricao status);

    boolean existsByPacienteIdAndOportunidadeIdAndStatusIn(UUID pacienteId, UUID oportunidadeId,
                                                           Collection<StatusInscricao> status);

    @Query(value = "select nextval('inscricao_protocolo_seq')", nativeQuery = true)
    long proximoProtocolo();
}
