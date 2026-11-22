package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.auth.CodigoVerificacaoEmailRepository;
import com.bcndeigth.carebridgeapi.paciente.Paciente;
import com.bcndeigth.carebridgeapi.paciente.PacienteRepository;
import com.bcndeigth.carebridgeapi.profissional_saude.Documento;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaudeRepository;
import com.bcndeigth.carebridgeapi.users.dto.DataExportResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MinhaContaService {
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalSaudeRepository profissionalRepository;
    private final CodigoVerificacaoEmailRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;

    public MinhaContaService(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            ProfissionalSaudeRepository profissionalRepository,
            CodigoVerificacaoEmailRepository codigoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.codigoRepository = codigoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public DataExportResponse exportar(Usuario principal) {
        Usuario usuario = buscarUsuarioAtual(principal);
        Map<String, Object> perfil = new LinkedHashMap<>();
        List<Map<String, Object>> registrosRelacionados;

        if (usuario.getTipoPerfil() == TipoPerfil.PACIENTE) {
            Paciente paciente = buscarPaciente(usuario);
            perfil.put("nome", paciente.getNome());
            perfil.put("dataNascimento", paciente.getDataNascimento());
            perfil.put("telefone", paciente.getTelefone());
            perfil.put("endereco", paciente.getEndereco());
            registrosRelacionados = paciente.getInscricoes().stream().map(inscricao -> {
                Map<String, Object> registro = new LinkedHashMap<>();
                registro.put("tipo", "INSCRICAO");
                registro.put("id", inscricao.getId());
                registro.put("oportunidadeId", inscricao.getOportunidade().getId());
                registro.put("status", inscricao.getStatus());
                registro.put("dataInscricao", inscricao.getDataInscricao());
                return registro;
            }).toList();
        } else if (usuario.getTipoPerfil() == TipoPerfil.PROFISSIONAL_SAUDE) {
            ProfissionalSaude profissional = buscarProfissional(usuario);
            perfil.put("nome", profissional.getNome());
            perfil.put("conselho", profissional.getConselho());
            perfil.put("uf", profissional.getUf());
            perfil.put("numeroRegistro", profissional.getNumeroRegistro());
            perfil.put("especialidade", profissional.getEspecialidade());
            perfil.put("ondeAtende", profissional.getOndeAtende());
            perfil.put("biografiaOuDescricao", profissional.getBiografiaOuDescricao());
            perfil.put("statusVerificacao", profissional.getStatusVerificacao());
            perfil.put("documentos", profissional.getDocumentosRegistro().stream()
                    .map(this::metadadosDocumento).toList());
            registrosRelacionados = profissional.getOportunidades().stream().map(oportunidade -> {
                Map<String, Object> registro = new LinkedHashMap<>();
                registro.put("tipo", "OPORTUNIDADE");
                registro.put("id", oportunidade.getId());
                registro.put("titulo", oportunidade.getTitulo());
                registro.put("statusAtiva", oportunidade.isStatusAtiva());
                registro.put("ultimaAtualizacao", oportunidade.getUltimaAtualizacao());
                return registro;
            }).toList();
        } else {
            throw operacaoIndisponivel();
        }

        return new DataExportResponse(
                usuario.getId(), usuario.getEmail(), usuario.getTipoPerfil(), usuario.isEmailVerificado(),
                usuario.getTermosAceitosEm(), usuario.getCriadoEm(), usuario.getAtualizadoEm(),
                perfil, registrosRelacionados, Instant.now());
    }

    @Transactional
    public void removerDocumentosPaciente(Usuario principal) {
        Usuario usuario = buscarUsuarioAtual(principal);
        if (usuario.getTipoPerfil() != TipoPerfil.PACIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A remoção de documentos é exclusiva para pacientes.");
        }
        buscarPaciente(usuario);
        // O modelo atual ainda não possui DocumentoInscricao. A operação é idempotente
        // e não há conteúdo físico de paciente para remover nesta etapa do domínio.
    }

    @Transactional
    public void anonimizar(Usuario principal, String senhaAtual) {
        Usuario usuario = buscarUsuarioAtual(principal);
        if (senhaAtual == null || senhaAtual.isBlank()
                || !passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A senha atual está incorreta.");
        }

        if (usuario.getTipoPerfil() == TipoPerfil.PACIENTE) {
            Paciente paciente = buscarPaciente(usuario);
            paciente.setNome("Conta anonimizada");
            paciente.setDataNascimento(null);
            paciente.setTelefone(null);
            paciente.setEndereco(null);
            pacienteRepository.save(paciente);
        } else if (usuario.getTipoPerfil() == TipoPerfil.PROFISSIONAL_SAUDE) {
            ProfissionalSaude profissional = buscarProfissional(usuario);
            profissional.setNome("Profissional anonimizado");
            profissional.setConselho(null);
            profissional.setUf(null);
            profissional.setNumeroRegistro(null);
            profissional.setEspecialidade(null);
            profissional.setOndeAtende(null);
            profissional.setBiografiaOuDescricao(null);
            profissional.getDocumentosRegistro().clear();
            profissionalRepository.save(profissional);
        } else {
            throw operacaoIndisponivel();
        }

        codigoRepository.deleteByUsuarioId(usuario.getId());
        usuario.anonimizar(
                "anonimizado+" + usuario.getId() + "@carebridge.invalid",
                passwordEncoder.encode(UUID.randomUUID().toString())
        );
        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuarioAtual(Usuario principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }
        return usuarioRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuário não autenticado."));
    }

    private Paciente buscarPaciente(Usuario usuario) {
        return pacienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil de paciente não encontrado."));
    }

    private ProfissionalSaude buscarProfissional(Usuario usuario) {
        return profissionalRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Perfil de profissional de saúde não encontrado."));
    }

    private Map<String, Object> metadadosDocumento(Documento documento) {
        Map<String, Object> metadados = new LinkedHashMap<>();
        metadados.put("nomeArquivo", documento.getNomeArquivo());
        metadados.put("tipo", documento.getTipo());
        metadados.put("tamanhoBytes", documento.getArquivo() == null ? 0 : documento.getArquivo().length);
        return metadados;
    }

    private ResponseStatusException operacaoIndisponivel() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "A operação não está disponível para este perfil.");
    }
}