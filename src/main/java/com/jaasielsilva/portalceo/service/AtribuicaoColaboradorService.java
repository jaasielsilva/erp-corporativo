package com.jaasielsilva.portalceo.service;

import com.jaasielsilva.portalceo.model.Chamado;
import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.repository.ChamadoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AtribuicaoColaboradorService {

    private static final Logger logger = LoggerFactory.getLogger(AtribuicaoColaboradorService.class);
    
    // Limite máximo de chamados simultâneos por colaborador
    private static final int LIMITE_CHAMADOS_POR_COLABORADOR = 5;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private NotificacaoSuporteService notificacaoSuporteService;

    /**
     * Atribui um colaborador específico a um chamado
     */
    public boolean atribuirColaborador(Long chamadoId, Long colaboradorId) {
        try {
            Optional<Chamado> chamadoOpt = chamadoRepository.findById(chamadoId);
            Optional<Colaborador> colaboradorOpt = colaboradorRepository.findById(colaboradorId);

            if (chamadoOpt.isEmpty()) {
                logger.error("Chamado não encontrado: {}", chamadoId);
                return false;
            }

            if (colaboradorOpt.isEmpty()) {
                logger.error("Colaborador não encontrado: {}", colaboradorId);
                return false;
            }

            Chamado chamado = chamadoOpt.get();
            Colaborador colaborador = colaboradorOpt.get();

            // Validar se colaborador pode assumir o chamado
            if (!podeAssumirChamado(colaborador)) {
                logger.warn("Colaborador {} já atingiu o limite de chamados simultâneos", colaborador.getNome());
                return false;
            }

            // Atribuir colaborador
            chamado.setColaboradorResponsavel(colaborador);
            chamado.setTecnicoResponsavel(colaborador.getNome()); // Manter compatibilidade
            chamado.setStatus(Chamado.StatusChamado.EM_ANDAMENTO);
            chamado.setDataInicioAtendimento(LocalDateTime.now());

            // Salvar no banco
            chamadoRepository.save(chamado);

            // Enviar notificações
            notificacaoSuporteService.notificarNovoColaborador(chamado, colaborador);

            logger.info("Chamado {} atribuído ao colaborador {}", chamado.getNumero(), colaborador.getNome());
            return true;

        } catch (Exception e) {
            logger.error("Erro ao atribuir colaborador ao chamado {}: {}", chamadoId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o melhor colaborador disponível para um chamado
     */
    public Optional<Colaborador> buscarMelhorColaborador(Chamado chamado) {
        try {
            // Buscar colaboradores ativos de suporte
            List<Colaborador> colaboradoresSupporte = buscarColaboradoresSuporteAtivos();

            if (colaboradoresSupporte.isEmpty()) {
                logger.warn("Nenhum colaborador de suporte ativo encontrado");
                return Optional.empty();
            }

            // Aplicar algoritmo de seleção
            Colaborador melhorColaborador = null;
            int menorCargaTrabalho = Integer.MAX_VALUE;

            for (Colaborador colaborador : colaboradoresSupporte) {
                if (!podeAssumirChamado(colaborador)) {
                    continue;
                }

                int cargaAtual = contarChamadosAtivos(colaborador);
                
                // Priorizar por especialização
                if (isEspecialistaParaCategoria(colaborador, chamado.getCategoria())) {
                    cargaAtual -= 1; // Bonus para especialista
                }

                if (cargaAtual < menorCargaTrabalho) {
                    menorCargaTrabalho = cargaAtual;
                    melhorColaborador = colaborador;
                }
            }

            return Optional.ofNullable(melhorColaborador);

        } catch (Exception e) {
            logger.error("Erro ao buscar melhor colaborador para chamado {}: {}", chamado.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Atribuição automática de colaborador
     */
    public boolean atribuicaoAutomatica(Long chamadoId) {
        try {
            Optional<Chamado> chamadoOpt = chamadoRepository.findById(chamadoId);
            
            if (chamadoOpt.isEmpty()) {
                logger.error("Chamado não encontrado para atribuição automática: {}", chamadoId);
                return false;
            }

            Chamado chamado = chamadoOpt.get();
            
            // Buscar melhor colaborador
            Optional<Colaborador> melhorColaboradorOpt = buscarMelhorColaborador(chamado);
            
            if (melhorColaboradorOpt.isEmpty()) {
                logger.warn("Nenhum colaborador disponível para atribuição automática do chamado {}", chamado.getNumero());
                return false;
            }

            // Atribuir colaborador
            return atribuirColaborador(chamadoId, melhorColaboradorOpt.get().getId());

        } catch (Exception e) {
            logger.error("Erro na atribuição automática do chamado {}: {}", chamadoId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se colaborador pode assumir mais um chamado
     */
    private boolean podeAssumirChamado(Colaborador colaborador) {
        if (!colaborador.getAtivo()) {
            return false;
        }

        int chamadosAtivos = contarChamadosAtivos(colaborador);
        return chamadosAtivos < LIMITE_CHAMADOS_POR_COLABORADOR;
    }

    /**
     * Conta quantos chamados ativos o colaborador possui
     */
    private int contarChamadosAtivos(Colaborador colaborador) {
        return chamadoRepository.countByColaboradorResponsavelAndStatusIn(
            colaborador, 
            List.of(Chamado.StatusChamado.EM_ANDAMENTO, Chamado.StatusChamado.RESOLVIDO)
        );
    }

    public int contarChamadosAtivos(Long colaboradorId) {
        Optional<Colaborador> colaboradorOpt = colaboradorRepository.findById(colaboradorId);
        if (colaboradorOpt.isPresent()) {
            return contarChamadosAtivos(colaboradorOpt.get());
        }
        return 0;
    }

    public boolean verificarDisponibilidade(Long colaboradorId) {
        Optional<Colaborador> colaboradorOpt = colaboradorRepository.findById(colaboradorId);
        if (colaboradorOpt.isPresent()) {
            return verificarDisponibilidade(colaboradorOpt.get());
        }
        return false;
    }

    public List<Chamado> listarChamadosColaborador(Long colaboradorId) {
        Optional<Colaborador> colaboradorOpt = colaboradorRepository.findById(colaboradorId);
        if (colaboradorOpt.isPresent()) {
            return chamadoRepository.findByColaboradorResponsavel(colaboradorOpt.get());
        }
        return List.of();
    }

    /**
     * Busca colaboradores ativos que trabalham com suporte
     */
    private List<Colaborador> buscarColaboradoresSuporteAtivos() {
        // Buscar colaboradores com cargos relacionados a suporte
        return colaboradorRepository.findByAtivoTrueAndCargoNomeContainingIgnoreCase("suporte");
    }

    /**
     * Verifica se colaborador é especialista na categoria
     */
    private boolean isEspecialistaParaCategoria(Colaborador colaborador, String categoria) {
        if (categoria == null || colaborador.getCargo() == null) {
            return false;
        }

        String cargoNome = colaborador.getCargo().getNome().toLowerCase();
        String categoriaLower = categoria.toLowerCase();

        // Lógica de especialização por categoria
        switch (categoriaLower) {
            case "hardware":
            case "infraestrutura":
                return cargoNome.contains("técnico") || cargoNome.contains("hardware");
            case "software":
            case "sistema":
                return cargoNome.contains("desenvolvedor") || cargoNome.contains("software");
            case "rede":
            case "conectividade":
                return cargoNome.contains("rede") || cargoNome.contains("infraestrutura");
            case "segurança":
                return cargoNome.contains("segurança") || cargoNome.contains("especialista");
            default:
                return cargoNome.contains("suporte"); // Suporte geral
        }
    }

    /**
     * Lista colaboradores disponíveis para atribuição
     */
    public List<Colaborador> listarColaboradoresDisponiveis() {
        List<Colaborador> colaboradores = buscarColaboradoresSuporteAtivos();
        return colaboradores.stream()
            .filter(this::podeAssumirChamado)
            .toList();
    }

    /**
     * Obter estatísticas de carga de trabalho
     */
    public String obterEstatisticasCarga(Colaborador colaborador) {
        int chamadosAtivos = contarChamadosAtivos(colaborador);
        return String.format("%d/%d chamados ativos", chamadosAtivos, LIMITE_CHAMADOS_POR_COLABORADOR);
    }

    /**
     * Atribui um colaborador específico a um chamado e retorna o chamado atualizado
     */
    public Chamado atribuirColaboradorEspecifico(Long chamadoId, Long colaboradorId) {
        try {
            Optional<Chamado> chamadoOpt = chamadoRepository.findById(chamadoId);
            Optional<Colaborador> colaboradorOpt = colaboradorRepository.findById(colaboradorId);

            if (chamadoOpt.isEmpty()) {
                throw new RuntimeException("Chamado não encontrado: " + chamadoId);
            }

            if (colaboradorOpt.isEmpty()) {
                throw new RuntimeException("Colaborador não encontrado: " + colaboradorId);
            }

            Chamado chamado = chamadoOpt.get();
            Colaborador colaborador = colaboradorOpt.get();

            // Validar se colaborador pode assumir o chamado
            if (!podeAssumirChamado(colaborador)) {
                throw new RuntimeException("Colaborador " + colaborador.getNome() + " já atingiu o limite de chamados simultâneos");
            }

            // Atribuir colaborador
            chamado.setColaboradorResponsavel(colaborador);
            chamado.setTecnicoResponsavel(colaborador.getNome()); // Manter compatibilidade
            chamado.setStatus(Chamado.StatusChamado.EM_ANDAMENTO);
            chamado.setDataInicioAtendimento(LocalDateTime.now());

            // Salvar no banco
            Chamado chamadoSalvo = chamadoRepository.save(chamado);

            // Enviar notificações
            notificacaoSuporteService.notificarNovoColaborador(chamado, colaborador);

            logger.info("Chamado {} atribuído ao colaborador {}", chamado.getNumero(), colaborador.getNome());
            return chamadoSalvo;

        } catch (Exception e) {
            logger.error("Erro ao atribuir colaborador específico ao chamado {}: {}", chamadoId, e.getMessage());
            throw new RuntimeException("Erro ao atribuir colaborador: " + e.getMessage());
        }
    }

    /**
     * Atribuição automática de colaborador e retorna o chamado atualizado
     */
    public Chamado atribuirColaboradorAutomatico(Long chamadoId) {
        try {
            Optional<Chamado> chamadoOpt = chamadoRepository.findById(chamadoId);
            
            if (chamadoOpt.isEmpty()) {
                throw new RuntimeException("Chamado não encontrado para atribuição automática: " + chamadoId);
            }

            Chamado chamado = chamadoOpt.get();
            
            // Buscar melhor colaborador
            Optional<Colaborador> melhorColaboradorOpt = buscarMelhorColaborador(chamado);
            
            if (melhorColaboradorOpt.isEmpty()) {
                throw new RuntimeException("Nenhum colaborador disponível para atribuição automática do chamado " + chamado.getNumero());
            }

            // Atribuir colaborador usando o método específico
            return atribuirColaboradorEspecifico(chamadoId, melhorColaboradorOpt.get().getId());

        } catch (Exception e) {
            logger.error("Erro na atribuição automática do chamado {}: {}", chamadoId, e.getMessage());
            throw new RuntimeException("Erro na atribuição automática: " + e.getMessage());
        }
    }

    /**
     * Remover atribuição de um chamado
     */
    public boolean removerAtribuicao(Long chamadoId) {
        try {
            Optional<Chamado> chamadoOpt = chamadoRepository.findById(chamadoId);
            
            if (chamadoOpt.isEmpty()) {
                return false;
            }

            Chamado chamado = chamadoOpt.get();
            chamado.setColaboradorResponsavel(null);
            chamado.setTecnicoResponsavel(null);
            chamado.setStatus(Chamado.StatusChamado.ABERTO);
            chamado.setDataInicioAtendimento(null);

            chamadoRepository.save(chamado);
            
            logger.info("Atribuição removida do chamado {}", chamado.getNumero());
            return true;

        } catch (Exception e) {
            logger.error("Erro ao remover atribuição do chamado {}: {}", chamadoId, e.getMessage());
            return false;
        }
    }
}