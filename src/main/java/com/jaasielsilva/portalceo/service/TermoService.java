package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.EstatisticasTermosDTO;
import com.jaasielsilva.portalceo.dto.TermoDTO;
import com.jaasielsilva.portalceo.dto.TermoAceiteDTO;
import com.jaasielsilva.portalceo.model.Termo;
import com.jaasielsilva.portalceo.model.TermoAceite;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.TermoRepository;
import com.jaasielsilva.portalceo.repository.TermoAceiteRepository;
import com.jaasielsilva.portalceo.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TermoService {

    @Autowired
    private TermoRepository termoRepository;

    @Autowired
    private TermoAceiteRepository termoAceiteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ===============================
    // MÉTODOS PARA GERENCIAR TERMOS
    // ===============================

    @Transactional
    public Termo salvarTermo(Termo termo) {
        if (termo.getId() == null) {
            termo.setDataCriacao(LocalDateTime.now());
        }
        return termoRepository.save(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public Termo criarTermo(TermoDTO termoDTO, Usuario criadoPor) {
        // Validações de negócio
        if (termoDTO.getDataVigenciaInicio() != null && termoDTO.getDataVigenciaFim() != null) {
            if (termoDTO.getDataVigenciaInicio().isAfter(termoDTO.getDataVigenciaFim())) {
                throw new IllegalArgumentException("Data de vigência inicial não pode ser após a final");
            }
        }
        // Versão única por tipo (recomendado)
        if (termoRepository.existsByTipoAndVersao(termoDTO.getTipo(), termoDTO.getVersao())) {
            throw new IllegalArgumentException("Já existe termo desse tipo com a mesma versão");
        }

        Termo termo = new Termo();
        termo.setTitulo(termoDTO.getTitulo());
        termo.setConteudo(termoDTO.getConteudo());
        termo.setVersao(termoDTO.getVersao());
        termo.setTipo(termoDTO.getTipo());
        termo.setStatus(Termo.StatusTermo.RASCUNHO);
        termo.setCriadoPor(criadoPor);
        termo.setObrigatorioAceite(termoDTO.isObrigatorioAceite());
        termo.setNotificarUsuarios(termoDTO.isNotificarUsuarios());
        termo.setObservacoes(termoDTO.getObservacoes());
        termo.setDataVigenciaInicio(termoDTO.getDataVigenciaInicio());
        termo.setDataVigenciaFim(termoDTO.getDataVigenciaFim());

        return salvarTermo(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public Termo atualizarTermo(Long id, TermoDTO termoDTO) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        // Validações de negócio
        if (termoDTO.getDataVigenciaInicio() != null && termoDTO.getDataVigenciaFim() != null) {
            if (termoDTO.getDataVigenciaInicio().isAfter(termoDTO.getDataVigenciaFim())) {
                throw new IllegalArgumentException("Data de vigência inicial não pode ser após a final");
            }
        }

        termo.setTitulo(termoDTO.getTitulo());
        termo.setConteudo(termoDTO.getConteudo());
        termo.setVersao(termoDTO.getVersao());
        termo.setTipo(termoDTO.getTipo());
        termo.setObrigatorioAceite(termoDTO.isObrigatorioAceite());
        termo.setNotificarUsuarios(termoDTO.isNotificarUsuarios());
        termo.setObservacoes(termoDTO.getObservacoes());
        termo.setDataVigenciaInicio(termoDTO.getDataVigenciaInicio());
        termo.setDataVigenciaFim(termoDTO.getDataVigenciaFim());

        return salvarTermo(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public void aprovarTermo(Long id, Usuario aprovadoPor) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        if (termo.getStatus() != Termo.StatusTermo.PENDENTE_APROVACAO) {
            throw new IllegalStateException("Apenas termos pendentes de aprovação podem ser aprovados");
        }

        termo.setStatus(Termo.StatusTermo.APROVADO);
        termo.setAprovadoPor(aprovadoPor);
        termo.setDataAprovacao(LocalDateTime.now());

        salvarTermo(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public void enviarParaAprovacao(Long id) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        if (termo.getStatus() != Termo.StatusTermo.RASCUNHO) {
            throw new RuntimeException("Apenas rascunhos podem ser enviados para aprovação");
        }

        termo.setStatus(Termo.StatusTermo.PENDENTE_APROVACAO);
        salvarTermo(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public void publicarTermo(Long id) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        if (termo.getStatus() != Termo.StatusTermo.APROVADO) {
            throw new RuntimeException("Apenas termos aprovados podem ser publicados");
        }

        termo.setStatus(Termo.StatusTermo.PUBLICADO);
        termo.setDataPublicacao(LocalDateTime.now());

        // Atualizar contadores
        atualizarContadoresAceite(termo);

        salvarTermo(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public void cancelarTermo(Long id) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        if (termo.getStatus() == Termo.StatusTermo.ARQUIVADO) {
            throw new RuntimeException("Não é possível cancelar um termo arquivado");
        }

        if (termo.getStatus() == Termo.StatusTermo.CANCELADO) {
            throw new RuntimeException("Termo já está cancelado");
        }

        termo.setStatus(Termo.StatusTermo.CANCELADO);
        salvarTermo(termo);
    }

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public void arquivarTermo(Long id) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));
        // Permite arquivar de qualquer estado, exceto já arquivado/cancelado
        if (termo.getStatus() == Termo.StatusTermo.ARQUIVADO) {
            throw new IllegalStateException("Termo já está arquivado");
        }
        termo.setStatus(Termo.StatusTermo.ARQUIVADO);
        salvarTermo(termo);
    }

    // ===============================
    // MÉTODOS DE CONSULTA
    // ===============================

    public Optional<Termo> buscarPorId(Long id) {
        return termoRepository.findById(id);
    }

    public List<Termo> buscarTodos() {
        return termoRepository.findAll();
    }

    public List<Termo> buscarPorStatus(Termo.StatusTermo status) {
        return termoRepository.findByStatusOrderByDataCriacaoDesc(status);
    }

    public List<Termo> buscarPorTipo(Termo.TipoTermo tipo) {
        return termoRepository.findByTipoOrderByDataCriacaoDesc(tipo);
    }

    public List<Termo> buscarTermosAtivos() {
        return termoRepository.findTermosAtivos(LocalDateTime.now());
    }

    public Page<Termo> buscarTermosAtivosPage(Termo.TipoTermo tipo, Pageable pageable) {
        return termoRepository.findTermosAtivosPage(LocalDateTime.now(), tipo, pageable);
    }

    // Aceites filtrados em DTO para exportação
    public List<TermoAceiteDTO> buscarAceitesFiltradosDTO(
            TermoAceite.StatusAceite status,
            Long termoId,
            java.time.LocalDate inicio,
            java.time.LocalDate fim,
            String usuarioLike
    ) {
        List<TermoAceite> base = termoAceiteRepository.findUltimosAceites();
        java.util.stream.Stream<TermoAceite> stream = base.stream();

        if (status != null) {
            stream = stream.filter(ta -> ta.getStatus() == status);
        }
        if (termoId != null) {
            stream = stream.filter(ta -> ta.getTermo() != null && termoId.equals(ta.getTermo().getId()));
        }
        if (inicio != null) {
            LocalDateTime ini = inicio.atStartOfDay();
            stream = stream.filter(ta -> ta.getDataAceite() != null && !ta.getDataAceite().isBefore(ini));
        }
        if (fim != null) {
            LocalDateTime end = fim.plusDays(1).atStartOfDay().minusSeconds(1);
            stream = stream.filter(ta -> ta.getDataAceite() != null && !ta.getDataAceite().isAfter(end));
        }
        if (usuarioLike != null && !usuarioLike.isBlank()) {
            String q = usuarioLike.toLowerCase();
            stream = stream.filter(ta -> {
                String nome = ta.getUsuario() != null ? ta.getUsuario().getNome() : null;
                String email = ta.getUsuario() != null ? ta.getUsuario().getEmail() : null;
                return (nome != null && nome.toLowerCase().contains(q)) || (email != null && email.toLowerCase().contains(q));
            });
        }

        return stream.map(ta -> {
                    TermoAceiteDTO dto = new TermoAceiteDTO();
                    if (ta.getUsuario() != null) {
                        dto.setUsuarioNome(ta.getUsuario().getNome());
                        dto.setUsuarioEmail(ta.getUsuario().getEmail());
                        dto.setUsuarioMatricula(ta.getUsuario().getMatricula());
                    }
                    if (ta.getTermo() != null) {
                        dto.setTermoTitulo(ta.getTermo().getTitulo());
                        dto.setTermoVersao(ta.getVersaoTermo() != null ? ta.getVersaoTermo() : ta.getTermo().getVersao());
                    } else {
                        dto.setTermoVersao(ta.getVersaoTermo());
                    }
                    dto.setDataAceite(ta.getDataAceite());
                    dto.setStatus(ta.getStatus());
                    dto.setStatusDescricao(ta.getStatus() != null ? ta.getStatus().getDescricao() : "");
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Termo> buscarTermoMaisRecentePorTipo(Termo.TipoTermo tipo) {
        return termoRepository.findFirstByTipoAndStatusOrderByDataCriacaoDesc(tipo, Termo.StatusTermo.PUBLICADO);
    }

    public List<Termo> buscarTermosPendentesAprovacao() {
        return termoRepository.findTermosPendentesAprovacao();
    }

    // ===============================
    // MÉTODOS PARA ACEITES
    // ===============================

    @Transactional
    @CacheEvict(value = "estatisticasTermos", allEntries = true)
    public TermoAceite aceitarTermo(Long termoId, Usuario usuario, String ipAceite, String userAgent) {
        Termo termo = buscarPorId(termoId)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        if (!termo.isVigente()) {
            throw new RuntimeException("Este termo não está mais vigente");
        }

        // Verificar se já existe aceite
        Optional<TermoAceite> aceiteExistente = termoAceiteRepository.findByTermoAndUsuario(termo, usuario);
        if (aceiteExistente.isPresent()) {
            throw new RuntimeException("Usuário já aceitou este termo");
        }

        TermoAceite aceite = new TermoAceite();
        aceite.setTermo(termo);
        aceite.setUsuario(usuario);
        aceite.setDataAceite(LocalDateTime.now());
        aceite.setIpAceite(ipAceite);
        aceite.setUserAgent(userAgent);
        aceite.setStatus(TermoAceite.StatusAceite.ACEITO);
        aceite.setVersaoTermo(termo.getVersao());

        // Assinatura digital (hash SHA-256 dos dados essenciais)
        String payload = (termo.getId() + "|" +
                safe(termo.getVersao()) + "|" + safe(termo.getConteudo()) + "|" +
                (usuario != null ? safe(usuario.getEmail()) : "") + "|" +
                (aceite.getDataAceite() != null ? aceite.getDataAceite().toString() : "") + "|" +
                safe(ipAceite) + "|" + safe(userAgent));
        aceite.setAssinaturaDigital(hashSha256(payload));

        TermoAceite aceiteSalvo = termoAceiteRepository.save(aceite);

        // Atualizar contadores do termo
        atualizarContadoresAceite(termo);

        return aceiteSalvo;
    }

    public boolean usuarioAceitouTermo(Long termoId, Usuario usuario) {
        Termo termo = buscarPorId(termoId).orElse(null);
        if (termo == null) return false;

        return termoAceiteRepository.existsByTermoAndUsuarioAndStatus(
            termo, usuario, TermoAceite.StatusAceite.ACEITO);
    }

    public List<TermoAceite> buscarAceitesDoTermo(Long termoId) {
        Termo termo = buscarPorId(termoId)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));
        return termoAceiteRepository.findByTermoOrderByDataAceiteDesc(termo);
    }

    public List<TermoAceite> buscarAceitesDoUsuario(Usuario usuario) {
        return termoAceiteRepository.findByUsuarioOrderByDataAceiteDesc(usuario);
    }

    public List<Usuario> buscarUsuariosSemAceite(Long termoId) {
        Termo termo = buscarPorId(termoId)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));
        return termoAceiteRepository.findUsuariosSemAceite(termo);
    }

    // ===============================
    // MÉTODOS DE ESTATÍSTICAS
    // ===============================

    @Cacheable(value = "estatisticasTermos")
    public EstatisticasTermosDTO buscarEstatisticas() {
        EstatisticasTermosDTO stats = new EstatisticasTermosDTO();

        // Estatísticas gerais
        stats.setTotalTermos(termoRepository.count());
        stats.setTermosAtivos(termoRepository.countByStatus(Termo.StatusTermo.PUBLICADO));
        stats.setTermosRascunho(termoRepository.countByStatus(Termo.StatusTermo.RASCUNHO));
        stats.setTermosArquivados(termoRepository.countByStatus(Termo.StatusTermo.ARQUIVADO));

        // Estatísticas de aceites
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioHoje = agora.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime inicioSemana = agora.minusDays(7);
        LocalDateTime inicioMes = agora.minusDays(30);

        stats.setTotalAceites(termoAceiteRepository.count());
        stats.setAceitesHoje(termoAceiteRepository.countAceitesPorPeriodo(inicioHoje, agora));
        stats.setAceitesSemana(termoAceiteRepository.countAceitesPorPeriodo(inicioSemana, agora));
        stats.setAceitesMes(termoAceiteRepository.countAceitesPorPeriodo(inicioMes, agora));

        // Estatísticas de usuários
        stats.setTotalUsuarios(usuarioRepository.count());
        List<Usuario> usuariosPendentes = termoAceiteRepository.findUsuariosComAceitesPendentes(agora);
        stats.setUsuariosComAceitesPendentes((long) usuariosPendentes.size());
        stats.setUsuariosComTodosAceites(stats.getTotalUsuarios() - stats.getUsuariosComAceitesPendentes());

        // Termo mais recente
        List<Termo> termosRecentes = termoRepository.findUltimosTermos();
        if (!termosRecentes.isEmpty()) {
            Termo termoRecente = termosRecentes.get(0);
            stats.setTermoMaisRecenteTitulo(termoRecente.getTitulo());
            stats.setTermoMaisRecenteVersao(termoRecente.getVersao());
            stats.setTermoMaisRecenteData(termoRecente.getDataCriacao());
        }

        // Termo com mais aceites (desempate por data de criação)
        Optional<Termo> termoMaisAceito = termoRepository.findTopByOrderByTotalAceitesDescDataCriacaoDesc();
        if (termoMaisAceito.isPresent()) {
            stats.setTermoMaisAceitoTitulo(termoMaisAceito.get().getTitulo());
            stats.setTermoMaisAceitoQuantidade(termoMaisAceito.get().getTotalAceites());
        }

        // Calcular percentuais
        stats.calcularPercentuais();

        return stats;
    }

    // Últimos aceites (limitados) em DTO para exibição
    public List<TermoAceiteDTO> buscarUltimosAceitesDTO(int limit) {
        List<TermoAceite> aceites = termoAceiteRepository.findUltimosAceites();
        return aceites.stream()
                .limit(limit)
                .map(ta -> {
                    TermoAceiteDTO dto = new TermoAceiteDTO();
                    dto.setId(ta.getId());
                    if (ta.getUsuario() != null) {
                        dto.setUsuarioNome(ta.getUsuario().getNome());
                        dto.setUsuarioEmail(ta.getUsuario().getEmail());
                    }
                    if (ta.getTermo() != null) {
                        dto.setTermoTitulo(ta.getTermo().getTitulo());
                        // Preferir versao registrada no aceite, fallback para versao do termo
                        dto.setTermoVersao(ta.getVersaoTermo() != null ? ta.getVersaoTermo() : ta.getTermo().getVersao());
                    } else {
                        dto.setTermoVersao(ta.getVersaoTermo());
                    }
                    dto.setDataAceite(ta.getDataAceite());
                    dto.setStatus(ta.getStatus());
                    dto.setStatusDescricao(ta.getStatus() != null ? ta.getStatus().getDescricao() : "");
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // Termo mais recente em DTO para exibição
    public java.util.Optional<TermoDTO> buscarTermoMaisRecenteDTO() {
        List<Termo> termosRecentes = termoRepository.findUltimosTermos();
        if (termosRecentes.isEmpty()) {
            return java.util.Optional.empty();
        }
        Termo maisRecente = termosRecentes.get(0);
        return java.util.Optional.of(converterParaDTO(maisRecente));
    }

    // Próxima revisão: termo ativo mais próximo do fim da vigência
    public java.util.Optional<TermoDTO> buscarProximoTermoExpiracaoDTO() {
        List<Termo> ativos = termoRepository.findTermosAtivos(java.time.LocalDateTime.now());
        return ativos.stream()
                .filter(t -> t.getDataVigenciaFim() != null)
                .sorted(java.util.Comparator.comparing(Termo::getDataVigenciaFim))
                .findFirst()
                .map(this::converterParaDTO);
    }

    

    // ===============================
    // MÉTODOS AUXILIARES
    // ===============================

    @Transactional
    private void atualizarContadoresAceite(Termo termo) {
        long totalAceites = termoAceiteRepository.countByTermoAndStatus(termo, TermoAceite.StatusAceite.ACEITO);
        long totalUsuarios = usuarioRepository.countByStatus(Usuario.Status.ATIVO);
        long totalPendentes = totalUsuarios - totalAceites;

        termo.setTotalAceites(totalAceites);
        termo.setTotalPendentes(Math.max(0, totalPendentes));

        termoRepository.save(termo);
    }


    private String safe(String s) { return s != null ? s : ""; }
    private String hashSha256(String payload) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return "SHA-256:" + hex.toString();
        } catch (Exception e) {
            return "SHA-256:ERROR";
        }
    }

    public List<TermoDTO> converterParaDTO(List<Termo> termos) {
        return termos.stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }

    public TermoDTO converterParaDTO(Termo termo) {
        TermoDTO dto = new TermoDTO();
        dto.setId(termo.getId());
        dto.setTitulo(termo.getTitulo());
        dto.setConteudo(termo.getConteudo());
        dto.setVersao(termo.getVersao());
        dto.setTipo(termo.getTipo());
        dto.setStatus(termo.getStatus());
        dto.setDataCriacao(termo.getDataCriacao());
        dto.setDataPublicacao(termo.getDataPublicacao());
        dto.setDataVigenciaInicio(termo.getDataVigenciaInicio());
        dto.setDataVigenciaFim(termo.getDataVigenciaFim());
        dto.setObservacoes(termo.getObservacoes());
        dto.setObrigatorioAceite(termo.isObrigatorioAceite());
        dto.setNotificarUsuarios(termo.isNotificarUsuarios());
        dto.setTotalAceites(termo.getTotalAceites());
        dto.setTotalPendentes(termo.getTotalPendentes());
        dto.setAtivo(termo.isAtivo());
        dto.setVigente(termo.isVigente());

        if (termo.getCriadoPor() != null) {
            dto.setCriadoPorId(termo.getCriadoPor().getId());
            dto.setCriadoPorNome(termo.getCriadoPor().getNome());
        }

        if (termo.getAprovadoPor() != null) {
            dto.setAprovadoPorId(termo.getAprovadoPor().getId());
            dto.setAprovadoPorNome(termo.getAprovadoPor().getNome());
            dto.setDataAprovacao(termo.getDataAprovacao());
        }

        dto.setTipoDescricao(termo.getTipo().getDescricao());
        dto.setStatusDescricao(termo.getStatus().getDescricao());
        dto.calcularPercentualAceite();

        return dto;
    }
}