package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record BuscaOportunidades(
        String q,
        TipoServico tipoServico,
        List<String> especialidade,
        Modalidade modalidade,
        List<ModalidadeAcesso> modalidadeAcesso,
        List<PublicoAlvo> publicoAlvo
) {
    private static final String COM_ACENTO = "áàâãäéèêëíìîïóòôõöúùûüç";
    private static final String SEM_ACENTO = "aaaaaeeeeiiiiooooouuuuc";

    public Specification<Oportunidade> ativas() {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            filtros.add(cb.equal(root.get("status"), StatusOportunidade.ATIVA));
            if (tipoServico != null) {
                filtros.add(cb.equal(root.get("tipoServico"), tipoServico));
            }
            if (temItens(especialidade)) {
                filtros.add(root.get("especialidade").in(especialidade));
            }
            if (modalidade != null) {
                filtros.add(cb.equal(root.get("modalidade"), modalidade));
            }
            if (temItens(modalidadeAcesso)) {
                filtros.add(root.get("modalidadeAcesso").in(modalidadeAcesso));
            }
            if (temItens(publicoAlvo)) {
                Expression<String> publico = root.get("publicoAlvoTexto");
                filtros.add(cb.or(publicoAlvo.stream()
                        .map(item -> cb.like(publico, "%" + item.name() + "%"))
                        .toArray(Predicate[]::new)));
            }
            String termo = normalizar(q);
            if (!termo.isEmpty()) {
                Join<Oportunidade, ProfissionalSaude> ofertante = root.join("ofertante");
                String padrao = "%" + termo + "%";
                filtros.add(cb.or(
                        cb.like(semAcento(cb, root.get("titulo")), padrao),
                        cb.like(semAcento(cb, root.get("especialidade")), padrao),
                        cb.like(semAcento(cb, root.get("cidade")), padrao),
                        cb.like(semAcento(cb, root.get("endereco")), padrao),
                        cb.like(semAcento(cb, ofertante.get("nome")), padrao),
                        cb.like(semAcento(cb, ofertante.get("ondeAtende")), padrao)
                ));
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        };
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private static Expression<String> semAcento(CriteriaBuilder cb, Expression<String> campo) {
        return cb.function("translate", String.class, cb.lower(campo), cb.literal(COM_ACENTO), cb.literal(SEM_ACENTO));
    }

    private static boolean temItens(List<?> lista) {
        return lista != null && !lista.isEmpty();
    }
}
