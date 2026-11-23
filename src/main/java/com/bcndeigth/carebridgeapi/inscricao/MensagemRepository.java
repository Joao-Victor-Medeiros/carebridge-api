package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensagemRepository extends JpaRepository<Mensagem, UUID> {

    List<Mensagem> findByInscricaoIdOrderByEnviadaEmAsc(UUID inscricaoId);

    Optional<Mensagem> findFirstByInscricaoIdOrderByEnviadaEmDesc(UUID inscricaoId);

    long countByInscricaoIdAndAutorNotAndLidaFalse(UUID inscricaoId, TipoPerfil leitor);

    @Modifying
    @Query("update Mensagem m set m.lida = true where m.inscricao.id = :inscricaoId and m.autor <> :leitor and m.lida = false")
    void marcarComoLidas(UUID inscricaoId, TipoPerfil leitor);
}
