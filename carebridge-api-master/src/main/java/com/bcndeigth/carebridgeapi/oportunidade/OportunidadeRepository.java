package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, UUID> {
    List<Oportunidade> findByStatusAtivaTrueOrderByUltimaAtualizacaoDesc();
    List<Oportunidade> findByOfertanteOrderByUltimaAtualizacaoDesc(ProfissionalSaude ofertante);
}