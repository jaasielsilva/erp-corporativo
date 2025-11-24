package com.jaasielsilva.portalceo.controller.rh;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import com.jaasielsilva.portalceo.model.ColaboradorEscala;
import com.jaasielsilva.portalceo.model.EscalaTrabalho;
import com.jaasielsilva.portalceo.repository.ColaboradorEscalaRepository;
import com.jaasielsilva.portalceo.repository.RegistroPontoRepository;
import com.jaasielsilva.portalceo.repository.EscalaTrabalhoRepository;
import com.jaasielsilva.portalceo.service.DepartamentoService;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.model.Usuario.Status;
import com.jaasielsilva.portalceo.model.NivelAcesso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rh/ponto-escalas")
public class PontoEscalasController {

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private EscalaTrabalhoRepository escalaTrabalhoRepository;
    @Autowired
    private ColaboradorEscalaRepository colaboradorEscalaRepository;
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registros")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaRegistros() {
        return "rh/ponto-escalas/registros";
    }

    @GetMapping("/escalas")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaEscalas(Model model) {
        YearMonth ym = YearMonth.now();
        model.addAttribute("mesAtual", ym.getMonthValue());
        model.addAttribute("anoAtual", ym.getYear());
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("escalasVigentes", escalaTrabalhoRepository.findEscalasVigentes(LocalDate.now()));
        return "rh/ponto-escalas/escalas";
    }

    @GetMapping("/atribuir-massa")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaAtribuirMassa(Model model) {
        model.addAttribute("departamentos", departamentoService.listarTodos());
        model.addAttribute("escalasVigentes", escalaTrabalhoRepository.findEscalasVigentes(LocalDate.now()));
        model.addAttribute("colaboradoresAtivos", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/atribuir-massa";
    }

    @PostMapping("/api/atribuir-massa")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> atribuirMassa(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long escalaId = Long.valueOf(String.valueOf(payload.get("escalaId")));
            EscalaTrabalho escala = escalaTrabalhoRepository.findById(escalaId).orElse(null);
            if (escala == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            LocalDate dataInicio = LocalDate.parse(String.valueOf(payload.get("dataInicio")));
            Object dfObj = payload.get("dataFim");
            LocalDate dataFim = dfObj != null && !String.valueOf(dfObj).isBlank() ? LocalDate.parse(String.valueOf(dfObj)) : null;
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) payload.get("colaboradorIds");
            if (ids == null || ids.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Nenhum colaborador informado");
                return resp;
            }

            List<Long> processed = new ArrayList<>();
            for (Integer i : ids) {
                Long colId = Long.valueOf(i);
                List<ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigenteByColaboradorAndData(colId, dataInicio);
                for (ColaboradorEscala ce : vigentes) {
                    LocalDate fim = dataInicio.minusDays(1);
                    ce.setDataFim(fim);
                    colaboradorEscalaRepository.save(ce);
                }
                ColaboradorEscala novo = new ColaboradorEscala();
                Colaborador col = colaboradorService.findById(colId);
                novo.setColaborador(col);
                novo.setEscalaTrabalho(escala);
                novo.setDataInicio(dataInicio);
                novo.setDataFim(dataFim);
                novo.setAtivo(true);
                colaboradorEscalaRepository.save(novo);
                processed.add(colId);
            }
            resp.put("success", true);
            resp.put("processados", processed.size());
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro na atribuição em massa");
            return resp;
        }
    }

    @PostMapping("/api/escalas/salvar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> salvarEscala(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            EscalaTrabalho e;
            Object idObj = payload.get("id");
            if (idObj != null && !String.valueOf(idObj).isBlank()) {
                Long id = Long.valueOf(String.valueOf(idObj));
                e = escalaTrabalhoRepository.findById(id).orElseGet(EscalaTrabalho::new);
                e.setId(id);
            } else {
                e = new EscalaTrabalho();
            }

            e.setNome(String.valueOf(payload.get("nome")));
            e.setDescricao(null);
            String tipo = String.valueOf(payload.get("tipo"));
            e.setTipo(EscalaTrabalho.TipoEscala.valueOf(tipo));

            String entrada1 = String.valueOf(payload.get("horarioEntrada1"));
            String saida1 = String.valueOf(payload.get("horarioSaida1"));
            String entrada2 = String.valueOf(payload.getOrDefault("horarioEntrada2", null));
            String saida2 = String.valueOf(payload.getOrDefault("horarioSaida2", null));

            e.setHorarioEntrada1(LocalTime.parse(entrada1));
            e.setHorarioSaida1(LocalTime.parse(saida1));
            if (entrada2 != null && !entrada2.equals("null") && !entrada2.isBlank()) e.setHorarioEntrada2(LocalTime.parse(entrada2));
            else e.setHorarioEntrada2(null);
            if (saida2 != null && !saida2.equals("null") && !saida2.isBlank()) e.setHorarioSaida2(LocalTime.parse(saida2));
            else e.setHorarioSaida2(null);

            Integer intervaloMinimo = Integer.valueOf(String.valueOf(payload.getOrDefault("intervaloMinimo", 0)));
            e.setIntervaloMinimo(intervaloMinimo);

            Object dvi = payload.get("dataVigenciaInicio");
            Object dvf = payload.get("dataVigenciaFim");
            e.setDataVigenciaInicio(dvi != null && !String.valueOf(dvi).isBlank() ? LocalDate.parse(String.valueOf(dvi)) : null);
            e.setDataVigenciaFim(dvf != null && !String.valueOf(dvf).isBlank() ? LocalDate.parse(String.valueOf(dvf)) : null);

            e.setToleranciaAtraso(Boolean.valueOf(String.valueOf(payload.getOrDefault("toleranciaAtraso", true))));
            e.setMinutosTolerancia(Integer.valueOf(String.valueOf(payload.getOrDefault("minutosTolerancia", 10))));

            e.setTrabalhaSegunda(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaSegunda", true))));
            e.setTrabalhaTerca(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaTerca", true))));
            e.setTrabalhaQuarta(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaQuarta", true))));
            e.setTrabalhaQuinta(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaQuinta", true))));
            e.setTrabalhaSexta(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaSexta", true))));
            e.setTrabalhaSabado(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaSabado", false))));
            e.setTrabalhaDomingo(Boolean.valueOf(String.valueOf(payload.getOrDefault("trabalhaDomingo", false))));

            if (!e.getHorarioEntrada1().isBefore(e.getHorarioSaida1())) {
                resp.put("success", false);
                resp.put("message", "Horários do primeiro período inválidos");
                return resp;
            }
            if (e.getHorarioEntrada2() != null && e.getHorarioSaida2() != null && !e.getHorarioEntrada2().isBefore(e.getHorarioSaida2())) {
                resp.put("success", false);
                resp.put("message", "Horários do segundo período inválidos");
                return resp;
            }

            escalaTrabalhoRepository.save(e);
            resp.put("success", true);
            return resp;
        } catch (Exception ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Erro ao salvar escala");
            return err;
        }
    }

    @PostMapping("/api/escalas/encerrar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> encerrarEscala(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long id = Long.valueOf(String.valueOf(payload.get("id")));
            EscalaTrabalho e = escalaTrabalhoRepository.findById(id).orElse(null);
            if (e == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            Object dvf = payload.get("dataVigenciaFim");
            LocalDate fim = dvf != null && !String.valueOf(dvf).isBlank() ? LocalDate.parse(String.valueOf(dvf)) : LocalDate.now();
            e.setDataVigenciaFim(fim);
            escalaTrabalhoRepository.save(e);
            resp.put("success", true);
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao encerrar vigência");
            return resp;
        }
    }

    @PostMapping("/api/escalas/clonar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> clonarEscala(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long id = Long.valueOf(String.valueOf(payload.get("id")));
            EscalaTrabalho o = escalaTrabalhoRepository.findById(id).orElse(null);
            if (o == null) {
                resp.put("success", false);
                resp.put("message", "Escala não encontrada");
                return resp;
            }
            EscalaTrabalho e = new EscalaTrabalho();
            e.setNome(o.getNome() + " (cópia)");
            e.setDescricao(o.getDescricao());
            e.setTipo(o.getTipo());
            e.setHorarioEntrada1(o.getHorarioEntrada1());
            e.setHorarioSaida1(o.getHorarioSaida1());
            e.setHorarioEntrada2(o.getHorarioEntrada2());
            e.setHorarioSaida2(o.getHorarioSaida2());
            e.setIntervaloMinimo(o.getIntervaloMinimo());
            e.setTrabalhaSegunda(o.getTrabalhaSegunda());
            e.setTrabalhaTerca(o.getTrabalhaTerca());
            e.setTrabalhaQuarta(o.getTrabalhaQuarta());
            e.setTrabalhaQuinta(o.getTrabalhaQuinta());
            e.setTrabalhaSexta(o.getTrabalhaSexta());
            e.setTrabalhaSabado(o.getTrabalhaSabado());
            e.setTrabalhaDomingo(o.getTrabalhaDomingo());
            e.setToleranciaAtraso(o.getToleranciaAtraso());
            e.setMinutosTolerancia(o.getMinutosTolerancia());
            e.setAtivo(true);
            e.setDataVigenciaInicio(LocalDate.now());
            e.setDataVigenciaFim(null);
            escalaTrabalhoRepository.save(e);
            resp.put("success", true);
            return resp;
        } catch (Exception ex) {
            resp.put("success", false);
            resp.put("message", "Erro ao clonar escala");
            return resp;
        }
    }

    @GetMapping("/api/calendario")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> calendario(@RequestParam Integer mes,
                                          @RequestParam Integer ano,
                                          @RequestParam(required = false) Long departamentoId,
                                          @RequestParam(required = false) Long escalaId) {
        Map<String, Object> resp = new HashMap<>();
        try {
            YearMonth ym = YearMonth.of(ano, mes);
            List<Map<String, Object>> days = new ArrayList<>();
            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                LocalDate date = ym.atDay(d);
                List<ColaboradorEscala> vigentes = colaboradorEscalaRepository.findVigentesByData(date);
                long count = vigentes.stream()
                        .filter(ce -> ce.getEscalaTrabalho() != null && ce.getEscalaTrabalho().trabalhaEm(date.getDayOfWeek()))
                        .filter(ce -> departamentoId == null || (ce.getColaborador() != null && ce.getColaborador().getDepartamento() != null && Objects.equals(ce.getColaborador().getDepartamento().getId(), departamentoId)))
                        .filter(ce -> escalaId == null || (ce.getEscalaTrabalho() != null && Objects.equals(ce.getEscalaTrabalho().getId(), escalaId)))
                        .count();
                Map<String, Object> m = new HashMap<>();
                m.put("day", d);
                m.put("total", count);
                days.add(m);
            }
            resp.put("success", true);
            resp.put("mes", mes);
            resp.put("ano", ano);
            resp.put("days", days);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao carregar calendário");
            return resp;
        }
    }

    @GetMapping("/correcoes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaCorrecoes(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/correcoes";
    }

    @PostMapping("/api/teste/criar-usuario")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> criarUsuarioTeste() {
        Map<String, Object> resp = new HashMap<>();
        try {
            String email = "teste.ponto@sistema.com";
            Optional<Colaborador> existenteCol = colaboradorService.listarAtivos().stream()
                    .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                    .findFirst();
            if (existenteCol.isPresent()) {
                Colaborador col = existenteCol.get();
                Usuario u = col.getUsuario();
                resp.put("success", true);
                resp.put("message", "Usuário de teste já existe");
                resp.put("matricula", u != null ? u.getMatricula() : null);
                resp.put("colaboradorId", col.getId());
                return resp;
            }

            Colaborador col = new Colaborador();
            col.setNome("Teste Ponto");
            col.setEmail(email);
            col.setCpf("999.999.990-01");
            col.setTelefone("(11) 99999-9999");
            col.setDataAdmissao(LocalDate.now().minusDays(1));
            col.setStatus(Colaborador.StatusColaborador.ATIVO);
            col.setAtivo(true);
            col = colaboradorService.salvar(col);

            Usuario usuario = new Usuario();
            usuario.setNome(col.getNome());
            usuario.setEmail(email);
            usuario.setSenha("teste123");
            usuario.setMatricula(usuarioService.gerarMatriculaUnica());
            usuario.setNivelAcesso(NivelAcesso.USER);
            usuario.setStatus(Status.ATIVO);
            usuario.setColaborador(col);
            usuarioService.salvarUsuario(usuario);

            col.setUsuario(usuario);
            colaboradorService.salvar(col);

            resp.put("success", true);
            resp.put("message", "Usuário e colaborador de teste criados");
            resp.put("matricula", usuario.getMatricula());
            resp.put("colaboradorId", col.getId());
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao criar usuário de teste");
            return resp;
        }
    }

    @GetMapping("/relatorios")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public String paginaRelatorios() {
        return "rh/ponto-escalas/relatorios";
    }

    @PostMapping("/correcoes/solicitar")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> solicitarCorrecao(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Solicitação registrada e enviada para aprovação");
        return resp;
    }

    @PostMapping("/registros/validar-matricula")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> validarMatricula(@RequestParam String matricula) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Optional<Colaborador> colOpt = colaboradorService.buscarPorMatriculaUsuario(matricula);
            if (colOpt.isEmpty()) {
                resp.put("success", true);
                resp.put("valida", false);
                resp.put("message", "Matrícula não encontrada");
                return resp;
            }
            Colaborador col = colOpt.get();
            boolean ativo = Boolean.TRUE.equals(col.getAtivo()) && col.getStatus() == Colaborador.StatusColaborador.ATIVO;
            if (!ativo) {
                resp.put("success", true);
                resp.put("valida", false);
                resp.put("message", "Colaborador inativo");
                return resp;
            }
            LocalDate hoje = LocalDate.now();
            Optional<RegistroPonto> registroOpt = registroPontoRepository.findByColaboradorAndData(col, hoje);
            String proximoTipo = calcularProximoTipo(registroOpt.orElse(null));
            resp.put("success", true);
            resp.put("valida", true);
            resp.put("colaborador", col.getNome());
            resp.put("proximoTipo", proximoTipo);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", "Erro interno");
            return resp;
        }
    }

    @PostMapping("/registros/registrar-por-matricula")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> registrarPorMatricula(@RequestParam String matricula,
                                                     @RequestParam(required = false) String senha) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Optional<Colaborador> colOpt = colaboradorService.buscarPorMatriculaUsuario(matricula);
            if (colOpt.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Matrícula não encontrada");
                return resp;
            }
            Colaborador col = colOpt.get();
            boolean ativo = Boolean.TRUE.equals(col.getAtivo()) && col.getStatus() == Colaborador.StatusColaborador.ATIVO;
            if (!ativo) {
                resp.put("success", false);
                resp.put("message", "Colaborador inativo");
                return resp;
            }

            LocalDate hoje = LocalDate.now();
            LocalTime agora = LocalTime.now();
            RegistroPonto registro = registroPontoRepository.findByColaboradorAndData(col, hoje)
                    .orElseGet(() -> {
                        RegistroPonto r = new RegistroPonto();
                        r.setColaborador(col);
                        r.setData(hoje);
                        r.setDataCriacao(LocalDateTime.now());
                        return r;
                    });

            String proximoTipo = calcularProximoTipo(registro);
            if (proximoTipo == null) {
                resp.put("success", false);
                resp.put("message", "Todos os registros de hoje já foram efetuados");
                return resp;
            }

            switch (proximoTipo) {
                case "Entrada" -> registro.setEntrada1(agora);
                case "Saída" -> {
                    if (registro.getEntrada1() != null && registro.getSaida1() == null) {
                        registro.setSaida1(agora);
                    } else if (registro.getEntrada2() != null && registro.getSaida2() == null) {
                        registro.setSaida2(agora);
                    } else {
                        registro.setSaida1(agora);
                    }
                }
                case "Retorno" -> registro.setEntrada2(agora);
            }

            registro.setDataUltimaEdicao(LocalDateTime.now());
            registroPontoRepository.save(registro);

            resp.put("success", true);
            resp.put("horario", agora.toString());
            resp.put("message", "Registro de " + proximoTipo + " efetuado com sucesso");
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao registrar ponto");
            return resp;
        }
    }

    @GetMapping("/registros/ultimos")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public Map<String, Object> ultimosRegistros() {
        Map<String, Object> resp = new HashMap<>();
        try {
            LocalDate hoje = LocalDate.now();
            List<RegistroPonto> registros = registroPontoRepository.findByData(hoje);
            List<Map<String, Object>> lista = new ArrayList<>();

            for (RegistroPonto r : registros.stream().limit(20).collect(Collectors.toList())) {
                List<Map<String, Object>> eventos = new ArrayList<>();
                if (r.getEntrada1() != null) eventos.add(evento("Entrada", r.getEntrada1(), r));
                if (r.getSaida1() != null) eventos.add(evento("Saída", r.getSaida1(), r));
                if (r.getEntrada2() != null) eventos.add(evento("Retorno", r.getEntrada2(), r));
                if (r.getSaida2() != null) eventos.add(evento("Saída", r.getSaida2(), r));
                for (Map<String, Object> ev : eventos) {
                    lista.add(ev);
                }
            }

            // Ordenar por horário desc
            lista.sort((a, b) -> ((String) b.get("horarioFormatado")).compareTo((String) a.get("horarioFormatado")));

            resp.put("success", true);
            resp.put("registros", lista);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Erro ao carregar registros");
            return resp;
        }
    }

    @GetMapping("/relatorio-mensal/pdf")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_RH','ROLE_GERENCIAL')")
    public ResponseEntity<byte[]> gerarRelatorioMensalPdf(@RequestParam String matricula,
                                                          @RequestParam Integer mes,
                                                          @RequestParam Integer ano) {
        Optional<Colaborador> colOpt = colaboradorService.buscarPorMatriculaUsuario(matricula);
        if (colOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Colaborador não encontrado".getBytes());
        }
        Colaborador col = colOpt.get();
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();
        List<RegistroPonto> registros = registroPontoRepository
                .findByColaboradorAndDataBetweenOrderByDataDesc(col, inicio, fim);

        Map<LocalDate, RegistroPonto> porDia = new HashMap<>();
        for (RegistroPonto r : registros) porDia.put(r.getData(), r);

        Context ctx = new Context();
        ctx.setVariable("colaborador", col);
        ctx.setVariable("mes", mes);
        ctx.setVariable("ano", ano);
        ctx.setVariable("dias", ym.lengthOfMonth());
        ctx.setVariable("porDia", porDia);

        String html = templateEngine.process("rh/ponto-escalas/espelho-ponto-pdf", ctx);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=espelho_ponto_" + matricula + "_" + String.format("%02d", mes) + "_" + ano + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao gerar PDF".getBytes());
        }
    }

    private String calcularProximoTipo(RegistroPonto r) {
        if (r == null || r.getEntrada1() == null) return "Entrada";
        if (r.getSaida1() == null) return "Saída";
        if (r.getEntrada2() == null) return "Retorno";
        if (r.getSaida2() == null) return "Saída";
        return null;
    }

    private Map<String, Object> evento(String tipo, LocalTime hora, RegistroPonto r) {
        Map<String, Object> m = new HashMap<>();
        m.put("tipo", tipo);
        m.put("horarioFormatado", String.format("%02d:%02d", hora.getHour(), hora.getMinute()));
        m.put("dataFormatada", String.format("%02d/%02d/%04d", r.getData().getDayOfMonth(), r.getData().getMonthValue(), r.getData().getYear()));
        m.put("colaboradorNome", r.getColaborador().getNome());
        return m;
    }
}
