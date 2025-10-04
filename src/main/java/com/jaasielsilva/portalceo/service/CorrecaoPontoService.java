package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.dto.CorrecaoPontoListDTO;
import com.jaasielsilva.portalceo.dto.CorrecaoPontoCreateDTO;
import com.jaasielsilva.portalceo.dto.CorrecaoPontoResumoDTO;
import com.jaasielsilva.portalceo.model.CorrecaoPonto;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.CorrecaoPontoRepository;
import com.jaasielsilva.portalceo.repository.RegistroPontoRepository;

import jakarta.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CorrecaoPontoService {

    private final CorrecaoPontoRepository repository;
    private final RegistroPontoRepository registroPontoRepository;
    private final ColaboradorService colaboradorService;
    private final UsuarioService usuarioService;

    public CorrecaoPontoService(CorrecaoPontoRepository repository,
                                RegistroPontoRepository registroPontoRepository,
                                ColaboradorService colaboradorService,
                                UsuarioService usuarioService) {
        this.repository = repository;
        this.registroPontoRepository = registroPontoRepository;
        this.colaboradorService = colaboradorService;
        this.usuarioService = usuarioService;
    }

    public Page<CorrecaoPontoListDTO> listar(LocalDate inicio, LocalDate fim, Long colaboradorId, String status, String tipo,
                                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<CorrecaoPonto> spec = Specification.where(null);

        if (inicio != null && fim != null) {
            spec = spec.and((root, query, cb) -> {
                var registroJoin = root.join("registroPonto", JoinType.LEFT);
                return cb.between(registroJoin.get("data"), inicio, fim);
            });
        }

        if (colaboradorId != null) {
            spec = spec.and((root, query, cb) -> {
                var colJoin = root.join("colaboradorSolicitante", JoinType.LEFT);
                return cb.equal(colJoin.get("id"), colaboradorId);
            });
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), CorrecaoPonto.StatusCorrecao.valueOf(status)));
        }

        if (tipo != null && !tipo.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoCorrecao"), CorrecaoPonto.TipoCorrecao.valueOf(tipo)));
        }

        Page<CorrecaoPonto> pageData = repository.findAll(spec, pageable);
        List<CorrecaoPontoListDTO> dtos = pageData.getContent().stream().map(this::toListDTO).collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, pageData.getTotalElements());
    }

    /**
     * Cria uma nova solicitação de correção de ponto.
     */
    public CorrecaoPonto criar(CorrecaoPontoCreateDTO dto, String loginUsuario) {
        if (dto == null) throw new IllegalArgumentException("Dados da correção são obrigatórios");
        if (dto.getColaboradorId() == null) throw new IllegalArgumentException("Colaborador é obrigatório");
        if (dto.getData() == null) throw new IllegalArgumentException("Data é obrigatória");
        if (dto.getTipoCorrecao() == null || dto.getTipoCorrecao().isBlank()) throw new IllegalArgumentException("Tipo de correção é obrigatório");
        if (dto.getJustificativa() == null || dto.getJustificativa().isBlank()) throw new IllegalArgumentException("Justificativa é obrigatória");

        Colaborador colaborador = colaboradorService.buscarPorId(dto.getColaboradorId());

        RegistroPonto registro = registroPontoRepository
                .findByColaboradorAndData(colaborador, dto.getData())
                .orElseGet(() -> {
                    RegistroPonto novo = new RegistroPonto();
                    novo.setColaborador(colaborador);
                    novo.setData(dto.getData());
                    novo.setTipoRegistro(RegistroPonto.TipoRegistro.MANUAL);
                    return registroPontoRepository.save(novo);
                });

        CorrecaoPonto.TipoCorrecao tipo = CorrecaoPonto.TipoCorrecao.valueOf(dto.getTipoCorrecao());

        CorrecaoPonto correcao = new CorrecaoPonto();
        correcao.setRegistroPonto(registro);
        correcao.setColaboradorSolicitante(colaborador);
        correcao.setTipoCorrecao(tipo);
        correcao.setHorarioAnterior(dto.getHorarioAnterior());
        correcao.setHorarioNovo(dto.getHorarioNovo());
        correcao.setJustificativa(dto.getJustificativa());
        correcao.setStatus(CorrecaoPonto.StatusCorrecao.PENDENTE);

        // Atribuir usuário de criação, buscando por email/matrícula
        Usuario usuarioCriacao = null;
        if (loginUsuario != null && !loginUsuario.isBlank()) {
            usuarioCriacao = usuarioService.buscarPorEmail(loginUsuario).orElseGet(() ->
                    usuarioService.buscarPorMatricula(loginUsuario).orElse(null));
        }
        correcao.setUsuarioCriacao(usuarioCriacao);

        return repository.save(correcao);
    }

    public CorrecaoPontoResumoDTO resumo(LocalDate inicio, LocalDate fim) {
        Specification<CorrecaoPonto> base = Specification.where(null);
        if (inicio != null && fim != null) {
            base = base.and((root, query, cb) -> {
                var registroJoin = root.join("registroPonto", JoinType.LEFT);
                return cb.between(registroJoin.get("data"), inicio, fim);
            });
        }

        long pendentes = repository.count(base.and((root, query, cb) -> cb.equal(root.get("status"), CorrecaoPonto.StatusCorrecao.PENDENTE)));
        long aprovadas = repository.count(base.and((root, query, cb) -> cb.equal(root.get("status"), CorrecaoPonto.StatusCorrecao.APROVADA)));
        long rejeitadas = repository.count(base.and((root, query, cb) -> cb.equal(root.get("status"), CorrecaoPonto.StatusCorrecao.REJEITADA)));

        // Tempo médio de aprovação em dias: média de (dataAprovacao - dataCriacao) para aprovadas no período
        List<CorrecaoPonto> aprovadasList = repository.findAll(base.and((root, query, cb) -> cb.equal(root.get("status"), CorrecaoPonto.StatusCorrecao.APROVADA)));
        Double mediaDias = null;
        if (!aprovadasList.isEmpty()) {
            double media = aprovadasList.stream()
                    .map(c -> {
                        if (c.getDataCriacao() == null || c.getDataAprovacao() == null) return null;
                        Duration d = Duration.between(c.getDataCriacao(), c.getDataAprovacao());
                        return d.toDaysPart() + (d.toHoursPart() % 24) / 24.0; // aproximar em dias
                    })
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(Double.NaN);
            mediaDias = Double.isNaN(media) ? null : media;
        }

        return new CorrecaoPontoResumoDTO(pendentes, aprovadas, rejeitadas, mediaDias);
    }

    private CorrecaoPontoListDTO toListDTO(CorrecaoPonto c) {
        RegistroPonto r = c.getRegistroPonto();
        Colaborador col = c.getColaboradorSolicitante();
        String justificativaResumo = c.getJustificativa();
        if (justificativaResumo != null && justificativaResumo.length() > 80) {
            justificativaResumo = justificativaResumo.substring(0, 77) + "...";
        }
        return new CorrecaoPontoListDTO(
                c.getId(),
                String.format("CP-%06d", c.getId()),
                col != null ? col.getId() : null,
                col != null ? col.getNome() : null,
                r != null ? r.getData() : null,
                c.getTipoCorrecao() != null ? c.getTipoCorrecao().name() : null,
                c.getHorarioAnterior(),
                c.getHorarioNovo(),
                justificativaResumo,
                c.getStatus() != null ? c.getStatus().name() : null,
                c.getDataCriacao()
        );
    }
}