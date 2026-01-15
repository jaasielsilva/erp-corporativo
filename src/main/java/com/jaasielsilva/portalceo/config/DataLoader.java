package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.Cargo;
import com.jaasielsilva.portalceo.model.Departamento;
import com.jaasielsilva.portalceo.model.Perfil;
import com.jaasielsilva.portalceo.model.Permissao;
import com.jaasielsilva.portalceo.repository.CargoRepository;
import com.jaasielsilva.portalceo.repository.DepartamentoRepository;
import com.jaasielsilva.portalceo.repository.PerfilRepository;
import com.jaasielsilva.portalceo.repository.PermissaoRepository;
import com.jaasielsilva.portalceo.service.PermissaoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataLoader implements CommandLineRunner {

    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final PermissaoService permissaoService;

    public DataLoader(CargoRepository cargoRepository,
                      DepartamentoRepository departamentoRepository,
                      PerfilRepository perfilRepository,
                      PermissaoRepository permissaoRepository,
                      PermissaoService permissaoService) {
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.permissaoService = permissaoService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        criarDepartamentos();
        criarCargos();
        criarPermissoes();
        criarPerfis();
    }

    private void criarDepartamentos() {
        List<String> departamentos = Arrays.asList(
                "TI",
                "Financeiro",
                "Recursos Humanos",
                "Administrativo",
                "Comercial",
                "Jurídico",
                "Marketing",
                "Projetos",
                "Operacional",
                "Diretoria"
        );

        for (String nome : departamentos) {
            if (departamentoRepository.findByNome(nome).isEmpty()) {
                Departamento dep = new Departamento();
                dep.setNome(nome);
                departamentoRepository.save(dep);
                System.out.println("Departamento criado: " + nome);
            }
        }
    }

    private void criarCargos() {
        List<String> cargos = Arrays.asList(
                "Estagiário",
                "Assistente",
                "Analista",
                "Coordenador",
                "Gerente",
                "Diretor",
                "Desenvolvedor Júnior",
                "Desenvolvedor Pleno",
                "Desenvolvedor Sênior",
                "Técnico de Suporte",
                "Advogado",
                "Contador",
                "Vendedor",
                "Designer"
        );

        for (String nome : cargos) {
            if (cargoRepository.findByNome(nome).isEmpty()) {
                Cargo cargo = new Cargo();
                cargo.setNome(nome);
                cargo.setAtivo(true);
                cargoRepository.save(cargo);
                System.out.println("Cargo criado: " + nome);
            }
        }
    }

    private void criarPermissoes() {
        Map<String, String> permissoesPadrao = permissaoService.getPermissoesPadrao();
        
        for (Map.Entry<String, String> entry : permissoesPadrao.entrySet()) {
            String nome = entry.getKey();
            if (permissaoRepository.findByNome(nome).isEmpty()) {
                Permissao p = new Permissao();
                p.setNome(nome);
                // Extrair categoria simples baseada no nome
                if (nome.contains("RH")) p.setCategoria("Recursos Humanos");
                else if (nome.contains("FINANCEIRO")) p.setCategoria("Financeiro");
                else if (nome.contains("TI") || nome.contains("SUPORTE")) p.setCategoria("TI");
                else if (nome.contains("JURIDICO")) p.setCategoria("Jurídico");
                else if (nome.contains("MARKETING")) p.setCategoria("Marketing");
                else if (nome.contains("CLIENTES") || nome.contains("VENDAS")) p.setCategoria("Comercial");
                else if (nome.contains("PROJETOS")) p.setCategoria("Projetos");
                else if (nome.contains("ADMIN")) p.setCategoria("Administração");
                else p.setCategoria("Geral");
                
                permissaoRepository.save(p);
                System.out.println("Permissão criada: " + nome);
            }
        }
    }

    private void criarPerfis() {
        // 1. ADMIN - Todas as permissões
        criarPerfilSeNaoExistir("ADMIN", permissaoRepository.findAll());

        // 2. USUARIO - Permissões básicas
        Set<Permissao> permissoesUsuario = buscarPermissoesPorPadrao("MENU_PESSOAL", "MENU_AJUDA", "ROLE_USER");
        criarPerfilSeNaoExistir("USUARIO", new ArrayList<>(permissoesUsuario));

        // =================================================================================
        // DEPARTAMENTO DE TI
        // =================================================================================
        
        // Estagiário TI
        Set<Permissao> permEstagiarioTI = new HashSet<>(permissoesUsuario);
        permEstagiarioTI.addAll(buscarPermissoesPorPadrao("MENU_TI_SUPORTE", "MENU_TI_DASHBOARD", "MENU_TI_SISTEMAS"));
        criarPerfilSeNaoExistir("ESTAGIARIO_TI", new ArrayList<>(permEstagiarioTI));

        // Assistente / Técnico TI
        Set<Permissao> permTecnicoTI = new HashSet<>(permEstagiarioTI);
        permTecnicoTI.addAll(buscarPermissoesPorPadrao("MENU_TI_BACKUP", "MENU_TI_SISTEMAS"));
        criarPerfilSeNaoExistir("TECNICO_TI", new ArrayList<>(permTecnicoTI));

        // Analista TI (Infra/Sistemas)
        Set<Permissao> permAnalistaTI = new HashSet<>(permTecnicoTI);
        permAnalistaTI.addAll(buscarPermissoesPorPadrao("MENU_TI_SEGURANCA", "MENU_TI_AUDITORIA"));
        criarPerfilSeNaoExistir("ANALISTA_TI", new ArrayList<>(permAnalistaTI));

        // Coordenador / Gerente TI
        Set<Permissao> permGerenteTI = new HashSet<>(permAnalistaTI);
        permGerenteTI.addAll(buscarPermissoesPorPadrao("MENU_TI", "ROLE_ADMIN")); // Admin TI
        criarPerfilSeNaoExistir("GERENTE_TI", new ArrayList<>(permGerenteTI));
        
        // Desenvolvedores (Vinculados a Projetos e TI)
        Set<Permissao> permDev = new HashSet<>(permissoesUsuario);
        permDev.addAll(buscarPermissoesPorPadrao("MENU_TI_SISTEMAS", "MENU_PROJETOS_TAREFAS", "MENU_PROJETOS_GERAL_LISTAR"));
        criarPerfilSeNaoExistir("DESENVOLVEDOR", new ArrayList<>(permDev));


        // =================================================================================
        // DEPARTAMENTO FINANCEIRO
        // =================================================================================

        // Estagiário Financeiro
        Set<Permissao> permEstagiarioFin = new HashSet<>(permissoesUsuario);
        permEstagiarioFin.addAll(buscarPermissoesPorPadrao("MENU_FINANCEIRO_DASHBOARD", "MENU_FINANCEIRO_RELATORIOS", "ROLE_FINANCEIRO_READ"));
        criarPerfilSeNaoExistir("ESTAGIARIO_FINANCEIRO", new ArrayList<>(permEstagiarioFin));

        // Assistente Financeiro
        Set<Permissao> permAssistenteFin = new HashSet<>(permEstagiarioFin);
        permAssistenteFin.addAll(buscarPermissoesPorPadrao("MENU_FINANCEIRO_CONTAS_PAGAR", "MENU_FINANCEIRO_CONTAS_RECEBER"));
        criarPerfilSeNaoExistir("ASSISTENTE_FINANCEIRO", new ArrayList<>(permAssistenteFin));

        // Analista Financeiro
        Set<Permissao> permAnalistaFin = new HashSet<>(permAssistenteFin);
        permAnalistaFin.addAll(buscarPermissoesPorPadrao("MENU_FINANCEIRO_FLUXO_CAIXA", "MENU_FINANCEIRO_TRANSFERENCIAS", "ROLE_FINANCEIRO_WRITE"));
        criarPerfilSeNaoExistir("ANALISTA_FINANCEIRO", new ArrayList<>(permAnalistaFin));
        
        // Coordenador / Gerente Financeiro
        Set<Permissao> permGerenteFin = new HashSet<>(permAnalistaFin);
        permGerenteFin.addAll(buscarPermissoesPorPadrao("MENU_FINANCEIRO", "ROLE_FINANCEIRO_ADMIN", "ROLE_FINANCEIRO_DELETE"));
        criarPerfilSeNaoExistir("GERENTE_FINANCEIRO", new ArrayList<>(permGerenteFin));


        // =================================================================================
        // DEPARTAMENTO RECURSOS HUMANOS (RH)
        // =================================================================================

        // Estagiário RH
        Set<Permissao> permEstagiarioRh = new HashSet<>(permissoesUsuario);
        permEstagiarioRh.addAll(buscarPermissoesPorPadrao("MENU_RH_COLABORADORES_LISTAR", "MENU_RH_BENEFICIOS_ADESAO", "ROLE_RH_READ"));
        criarPerfilSeNaoExistir("ESTAGIARIO_RH", new ArrayList<>(permEstagiarioRh));

        // Assistente RH
        Set<Permissao> permAssistenteRh = new HashSet<>(permEstagiarioRh);
        permAssistenteRh.addAll(buscarPermissoesPorPadrao("MENU_RH_PONTO", "MENU_RH_FERIAS_SOLICITAR", "MENU_RH_TREINAMENTOS"));
        criarPerfilSeNaoExistir("ASSISTENTE_RH", new ArrayList<>(permAssistenteRh));

        // Analista RH
        Set<Permissao> permAnalistaRh = new HashSet<>(permAssistenteRh);
        permAnalistaRh.addAll(buscarPermissoesPorPadrao("MENU_RH_FOLHA", "MENU_RH_RECRUTAMENTO", "MENU_RH_AVALIACAO", "ROLE_RH_WRITE"));
        criarPerfilSeNaoExistir("ANALISTA_RH", new ArrayList<>(permAnalistaRh));

        // Coordenador / Gerente RH
        Set<Permissao> permGerenteRh = new HashSet<>(permAnalistaRh);
        permGerenteRh.addAll(buscarPermissoesPorPadrao("MENU_RH", "MENU_RH_CONFIGURACOES", "MENU_RH_AUDITORIA", "ROLE_RH_ADMIN", "ROLE_RH_DELETE"));
        criarPerfilSeNaoExistir("GERENTE_RH", new ArrayList<>(permGerenteRh));


        // =================================================================================
        // DEPARTAMENTO ADMINISTRATIVO
        // =================================================================================
        
        // Estagiário ADM
        Set<Permissao> permEstagiarioAdm = new HashSet<>(permissoesUsuario);
        permEstagiarioAdm.addAll(buscarPermissoesPorPadrao("MENU_DOCS_GERENCIAL")); // Acesso básico a docs
        criarPerfilSeNaoExistir("ESTAGIARIO_ADM", new ArrayList<>(permEstagiarioAdm));
        
        // Assistente ADM
        Set<Permissao> permAssistenteAdm = new HashSet<>(permEstagiarioAdm);
        permAssistenteAdm.addAll(buscarPermissoesPorPadrao("MENU_CADASTROS", "MENU_UTILIDADES"));
        criarPerfilSeNaoExistir("ASSISTENTE_ADM", new ArrayList<>(permAssistenteAdm));

        // Gerente ADM
        Set<Permissao> permGerenteAdm = new HashSet<>(permAssistenteAdm);
        permGerenteAdm.addAll(buscarPermissoesPorPadrao("MENU_ADMIN_RELATORIOS", "MENU_ADMIN_CONFIGURACOES")); // Configurações gerais não sensíveis
        criarPerfilSeNaoExistir("GERENTE_ADM", new ArrayList<>(permGerenteAdm));


        // =================================================================================
        // DEPARTAMENTO COMERCIAL
        // =================================================================================

        // Estagiário Comercial
        Set<Permissao> permEstagiarioCom = new HashSet<>(permissoesUsuario);
        permEstagiarioCom.addAll(buscarPermissoesPorPadrao("MENU_CLIENTES_LISTAR", "MENU_CLIENTES_DETALHES", "MENU_VENDAS_PDV"));
        criarPerfilSeNaoExistir("ESTAGIARIO_COMERCIAL", new ArrayList<>(permEstagiarioCom));

        // Vendedor
        Set<Permissao> permVendedor = new HashSet<>(permEstagiarioCom);
        permVendedor.addAll(buscarPermissoesPorPadrao("MENU_VENDAS_PEDIDOS", "MENU_CLIENTES_NOVO", "CLIENTE_EDITAR", "MENU_CLIENTES_HISTORICO"));
        criarPerfilSeNaoExistir("VENDEDOR", new ArrayList<>(permVendedor));

        // Gerente Comercial
        Set<Permissao> permGerenteComercial = new HashSet<>(permVendedor);
        permGerenteComercial.addAll(buscarPermissoesPorPadrao("MENU_VENDAS", "MENU_CLIENTES", "CLIENTE_EXCLUIR", "MENU_MARKETING"));
        criarPerfilSeNaoExistir("GERENTE_COMERCIAL", new ArrayList<>(permGerenteComercial));


        // =================================================================================
        // DEPARTAMENTO JURÍDICO
        // =================================================================================

        // Estagiário Jurídico
        Set<Permissao> permEstagiarioJur = new HashSet<>(permissoesUsuario);
        permEstagiarioJur.addAll(buscarPermissoesPorPadrao("MENU_JURIDICO_PROCESSOS_LISTAR", "MENU_JURIDICO_DASHBOARD"));
        criarPerfilSeNaoExistir("ESTAGIARIO_JURIDICO", new ArrayList<>(permEstagiarioJur));

        // Advogado (Analista)
        Set<Permissao> permAdvogado = new HashSet<>(permEstagiarioJur);
        permAdvogado.addAll(buscarPermissoesPorPadrao("MENU_JURIDICO"));
        criarPerfilSeNaoExistir("ADVOGADO", new ArrayList<>(permAdvogado));
        
        // Gerente Jurídico
        Set<Permissao> permGerenteJur = new HashSet<>(permAdvogado);
        permGerenteJur.addAll(buscarPermissoesPorPadrao("MENU_JURIDICO_AUDITORIA", "MENU_JURIDICO_COMPLIANCE"));
        criarPerfilSeNaoExistir("GERENTE_JURIDICO", new ArrayList<>(permGerenteJur));


        // =================================================================================
        // DEPARTAMENTO MARKETING
        // =================================================================================

        // Estagiário Marketing
        Set<Permissao> permEstagiarioMkt = new HashSet<>(permissoesUsuario);
        permEstagiarioMkt.addAll(buscarPermissoesPorPadrao("MENU_MARKETING_MATERIAIS", "MENU_MARKETING_EVENTOS"));
        criarPerfilSeNaoExistir("ESTAGIARIO_MARKETING", new ArrayList<>(permEstagiarioMkt));

        // Analista Marketing
        Set<Permissao> permAnalistaMkt = new HashSet<>(permEstagiarioMkt);
        permAnalistaMkt.addAll(buscarPermissoesPorPadrao("MENU_MARKETING_CAMPANHAS", "MENU_MARKETING_LEADS"));
        criarPerfilSeNaoExistir("ANALISTA_MARKETING", new ArrayList<>(permAnalistaMkt));

        // Gerente Marketing
        Set<Permissao> permGerenteMkt = new HashSet<>(permAnalistaMkt);
        permGerenteMkt.addAll(buscarPermissoesPorPadrao("MENU_MARKETING", "MENU_MARKETING_DASHBOARD"));
        criarPerfilSeNaoExistir("GERENTE_MARKETING", new ArrayList<>(permGerenteMkt));


        // =================================================================================
        // DEPARTAMENTO PROJETOS
        // =================================================================================

        // Estagiário Projetos
        Set<Permissao> permEstagiarioProj = new HashSet<>(permissoesUsuario);
        permEstagiarioProj.addAll(buscarPermissoesPorPadrao("MENU_PROJETOS_TAREFAS_LISTAR"));
        criarPerfilSeNaoExistir("ESTAGIARIO_PROJETOS", new ArrayList<>(permEstagiarioProj));
        
        // Analista Projetos / Membro Equipe
        Set<Permissao> permAnalistaProj = new HashSet<>(permEstagiarioProj);
        permAnalistaProj.addAll(buscarPermissoesPorPadrao("MENU_PROJETOS_TAREFAS", "MENU_PROJETOS_GERAL_LISTAR", "MENU_PROJETOS_CRONOGRAMA"));
        criarPerfilSeNaoExistir("ANALISTA_PROJETOS", new ArrayList<>(permAnalistaProj));

        // Gerente Projetos
        Set<Permissao> permGerenteProj = new HashSet<>(permAnalistaProj);
        permGerenteProj.addAll(buscarPermissoesPorPadrao("MENU_PROJETOS", "MENU_PROJETOS_EQUIPES_CADASTRAR", "MENU_PROJETOS_RELATORIOS"));
        criarPerfilSeNaoExistir("GERENTE_PROJETOS", new ArrayList<>(permGerenteProj));


        // =================================================================================
        // DEPARTAMENTO OPERACIONAL
        // =================================================================================
        
        // Assistente Operacional (foco em Estoque/Compras)
        Set<Permissao> permAssistenteOp = new HashSet<>(permissoesUsuario);
        permAssistenteOp.addAll(buscarPermissoesPorPadrao("MENU_ESTOQUE_PRODUTOS", "MENU_ESTOQUE_CATEGORIAS_LISTAR", "MENU_COMPRAS"));
        criarPerfilSeNaoExistir("ASSISTENTE_OPERACIONAL", new ArrayList<>(permAssistenteOp));

        // Gerente Operacional
        Set<Permissao> permGerenteOp = new HashSet<>(permAssistenteOp);
        permGerenteOp.addAll(buscarPermissoesPorPadrao("MENU_ESTOQUE", "MENU_COMPRAS", "MENU_SERVICOS"));
        criarPerfilSeNaoExistir("GERENTE_OPERACIONAL", new ArrayList<>(permGerenteOp));


        // =================================================================================
        // DIRETORIA
        // =================================================================================
        
        // Diretor (Visão Estratégica de Tudo)
        Set<Permissao> permDiretor = new HashSet<>(permissoesUsuario);
        permDiretor.addAll(buscarPermissoesPorPadrao("DASHBOARD", "RELATORIO", "MENU_ADMIN_METAS")); // Acesso a todos dashboards e relatórios
        permDiretor.addAll(buscarPermissoesPorPadrao("ROLE_RH_READ", "ROLE_FINANCEIRO_READ", "ROLE_JURIDICO")); // Leitura geral
        criarPerfilSeNaoExistir("DIRETOR", new ArrayList<>(permDiretor));
    }

    private void criarPerfilSeNaoExistir(String nome, List<Permissao> permissoes) {
        var existenteOpt = perfilRepository.findByNome(nome);
        if (existenteOpt.isEmpty()) {
            Perfil perfil = new Perfil();
            perfil.setNome(nome);
            perfil.setPermissoes(new HashSet<>(permissoes));
            perfilRepository.save(perfil);
            System.out.println("Perfil criado: " + nome + " com " + permissoes.size() + " permissões.");
        } else {
            Perfil perfil = existenteOpt.get();
            if (perfil.getPermissoes() == null) {
                perfil.setPermissoes(new HashSet<>());
            }
            int antes = perfil.getPermissoes().size();
            perfil.getPermissoes().addAll(permissoes);
            if (perfil.getPermissoes().size() > antes) {
                perfilRepository.save(perfil);
                System.out.println("Perfil atualizado: " + nome + " agora com " + perfil.getPermissoes().size() + " permissões.");
            }
        }
    }

    private Set<Permissao> buscarPermissoesPorPadrao(String... padroes) {
        List<Permissao> todas = permissaoRepository.findAll();
        Set<Permissao> filtradas = new HashSet<>();
        
        for (String padrao : padroes) {
            for (Permissao p : todas) {
                if (p.getNome().contains(padrao)) {
                    filtradas.add(p);
                }
            }
        }
        return filtradas;
    }
}
