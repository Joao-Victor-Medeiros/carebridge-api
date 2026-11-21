package com.bcndeigth.carebridgeapi.oportunidade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, UUID>, JpaSpecificationExecutor<Oportunidade> {

    @Override
    @EntityGraph(attributePaths = {"ofertante", "formularioTriagem"})
    Page<Oportunidade> findAll(Specification<Oportunidade> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"ofertante", "formularioTriagem"})
    List<Oportunidade> findByOfertanteIdOrderByUltimaAtualizacaoDesc(UUID ofertanteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Oportunidade o where o.id = :id")
    Oportunidade findComBloqueio(UUID id);
}
