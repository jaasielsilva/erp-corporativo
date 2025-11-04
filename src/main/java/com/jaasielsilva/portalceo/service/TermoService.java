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
import org.springframework.stereotype.Service;

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
    public Termo criarTermo(TermoDTO termoDTO, Usuario criadoPor) {
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
    public Termo atualizarTermo(Long id, TermoDTO termoDTO) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

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
    public void aprovarTermo(Long id, Usuario aprovadoPor) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        termo.setStatus(Termo.StatusTermo.APROVADO);
        termo.setAprovadoPor(aprovadoPor);
        termo.setDataAprovacao(LocalDateTime.now());

        salvarTermo(termo);
    }

    @Transactional
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
    public void arquivarTermo(Long id) {
        Termo termo = buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

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

        // Termo com mais aceites
        Optional<Termo> termoMaisAceito = termoRepository.findTermoComMaisAceites();
        if (termoMaisAceito.isPresent()) {
            stats.setTermoMaisAceitoTitulo(termoMaisAceito.get().getTitulo());
            stats.setTermoMaisAceitoQuantidade(termoMaisAceito.get().getTotalAceites());
        }

        // Calcular percentuais
        stats.calcularPercentuais();

        return stats;
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