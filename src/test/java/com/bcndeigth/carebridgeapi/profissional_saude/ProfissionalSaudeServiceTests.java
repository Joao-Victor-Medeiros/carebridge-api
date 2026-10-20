package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.users.TipoPerfil;
import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProfissionalSaudeServiceTests {

    @Autowired
    private ProfissionalSaudeService service;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devePersistirCadastroSemDocumentos() {
        var dados = novoCadastro();

        service.cadastrarProfissionalSaude(dados, null);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1L, contarProfissionais(dados.usuarioDto().email()));
        assertEquals(0L, contarDocumentos(dados.usuarioDto().email()));
        assertEquals("[]", consultarReferencia(dados.usuarioDto().email()));
    }

    @Test
    void devePersistirCadastroComDocumento() {
        var dados = novoCadastro();
        var documento = new MockMultipartFile(
                "documentos", "registro.txt", "text/plain", new byte[]{1, 2, 3}
        );

        service.cadastrarProfissionalSaude(dados, List.of(documento));
        entityManager.flush();
        entityManager.clear();

        assertEquals(1L, contarProfissionais(dados.usuarioDto().email()));
        assertEquals(1L, contarDocumentos(dados.usuarioDto().email()));
        assertEquals(List.of("registro.txt"), consultarNomesArquivos(dados.usuarioDto().email()));
        assertEquals("[\"registro.txt\"]", consultarReferencia(dados.usuarioDto().email()));
        verificarConteudo(dados.usuarioDto().email(), "registro.txt", "text/plain", new byte[]{1, 2, 3});
    }

    @Test
    void devePersistirNomesDeDoisDocumentos() {
        var dados = novoCadastro();
        var diploma = new MockMultipartFile("documentos", "diploma.pdf", "application/pdf", new byte[]{1});
        var registro = new MockMultipartFile("documentos", "registro.pdf", "application/pdf", new byte[]{2});

        service.cadastrarProfissionalSaude(dados, List.of(diploma, registro));
        entityManager.flush();
        entityManager.clear();

        assertEquals(1L, contarProfissionais(dados.usuarioDto().email()));
        assertEquals(List.of("diploma.pdf", "registro.pdf"), consultarNomesArquivos(dados.usuarioDto().email()));
        assertEquals("[\"diploma.pdf\",\"registro.pdf\"]", consultarReferencia(dados.usuarioDto().email()));
        verificarConteudo(dados.usuarioDto().email(), "diploma.pdf", "application/pdf", new byte[]{1});
        verificarConteudo(dados.usuarioDto().email(), "registro.pdf", "application/pdf", new byte[]{2});
    }

    @Test
    void devePersistirCadastroComListaVaziaDeDocumentos() {
        var dados = novoCadastro();

        service.cadastrarProfissionalSaude(dados, List.of());
        entityManager.flush();
        entityManager.clear();

        assertEquals(1L, contarProfissionais(dados.usuarioDto().email()));
        assertEquals(List.of(), consultarNomesArquivos(dados.usuarioDto().email()));
    }

    @Test
    void deveRejeitarMaisDeDoisDocumentosSemGravarUsuario() {
        var dados = novoCadastro();
        var documento = new MockMultipartFile("documentos", "registro.pdf", "application/pdf", new byte[]{1});

        var exception = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarProfissionalSaude(dados, List.of(documento, documento, documento)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(0L, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM usuarios WHERE email = ?", Long.class, dados.usuarioDto().email()));
        assertEquals(0L, contarProfissionais(dados.usuarioDto().email()));
        assertEquals(0L, contarDocumentos(dados.usuarioDto().email()));
    }

    @Test
    void deveRejeitarArquivoVazio() {
        var dados = novoCadastro();
        var documento = new MockMultipartFile("documentos", "vazio.pdf", "application/pdf", new byte[0]);
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.cadastrarProfissionalSaude(dados, List.of(documento)));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(0L, jdbcTemplate.queryForObject(
                "SELECT count(*) FROM usuarios WHERE email = ?", Long.class, dados.usuarioDto().email()));
    }

    @Test
    void deveUsarTipoPadraoERemoverCaminhoDoNome() {
        var dados = novoCadastro();
        var documento = new MockMultipartFile("documentos", "C:\\fakepath\\registro.pdf", null, new byte[]{3});
        service.cadastrarProfissionalSaude(dados, List.of(documento));
        entityManager.flush();
        entityManager.clear();
        verificarConteudo(dados.usuarioDto().email(), "registro.pdf", "application/octet-stream", new byte[]{3});
    }

    private void verificarConteudo(String email, String nome, String tipo, byte[] conteudo) {
        var documentos = entityManager.createQuery("""
                SELECT d FROM ProfissionalSaude p JOIN p.documentosRegistro d
                WHERE p.usuario.email = :email AND d.nomeArquivo = :nome
                """, Documento.class)
                .setParameter("email", email).setParameter("nome", nome).getResultList();
        assertEquals(1, documentos.size());
        assertEquals(nome, documentos.getFirst().getNomeArquivo());
        assertEquals(tipo, documentos.getFirst().getTipo());
        assertArrayEquals(conteudo, documentos.getFirst().getArquivo());
    }

    private CadastroProfissionalSaudeDto novoCadastro() {
        return new CadastroProfissionalSaudeDto(
                new SignUpUserDto("Teste", UUID.randomUUID() + "@example.com", "SenhaTeste123!",
                        TipoPerfil.PROFISSIONAL_SAUDE),
                null,
                "Cadastro de teste"
        );
    }

    private String consultarReferencia(String email) {
        return jdbcTemplate.queryForObject("""
                SELECT p.documento_registro FROM profissional_saude p
                JOIN usuarios u ON u.id = p.usuario_id WHERE u.email = ?
                """, String.class, email);
    }

    private Long contarProfissionais(String email) {
        return jdbcTemplate.queryForObject("""
                SELECT count(*) FROM profissional_saude p
                JOIN usuarios u ON u.id = p.usuario_id WHERE u.email = ?
                """, Long.class, email);
    }

    private Long contarDocumentos(String email) {
        return jdbcTemplate.queryForObject("""
                SELECT count(*) FROM documentos_registro d
                JOIN profissional_saude p ON p.id = d.profissional_id
                JOIN usuarios u ON u.id = p.usuario_id WHERE u.email = ?
                """, Long.class, email);
    }

    private List<String> consultarNomesArquivos(String email) {
        return jdbcTemplate.queryForList("""
                SELECT d.nome_arquivo FROM documentos_registro d
                JOIN profissional_saude p ON p.id = d.profissional_id
                JOIN usuarios u ON u.id = p.usuario_id WHERE u.email = ?
                ORDER BY d.nome_arquivo
                """, String.class, email);
    }
}