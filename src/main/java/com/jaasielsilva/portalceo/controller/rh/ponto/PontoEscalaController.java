package com.jaasielsilva.portalceo.controller.rh.ponto;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.RegistroPontoRepository;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import com.jaasielsilva.portalceo.service.EspelhoPontoProfissionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Imports para PDF
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/rh/ponto-escalas")
public class PontoEscalaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private EspelhoPontoProfissionalService espelhoPontoProfissionalService;

    @GetMapping("/correcoes")
    public String correcoes(Model model) {
        return "rh/ponto-escalas/correcoes";
    }

    @GetMapping("/registros")
    public String registros(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/registros";
    }

    // Validar matrícula
    @PostMapping("/registros/validar-matricula")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarMatricula(@RequestParam String matricula) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

            Colaborador colaborador = usuario.getColaborador();
            if (colaborador == null || colaborador.getStatus() != Colaborador.StatusColaborador.ATIVO) {
                throw new IllegalArgumentException("Colaborador não encontrado ou inativo");
            }

            response.put("success", true);
            response.put("valida", true);
            response.put("colaborador", colaborador.getNome());
        } catch (Exception e) {
            response.put("success", false);
            response.put("valida", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // Registrar ponto por matrícula (máximo 4 batidas por dia com intervalo mínimo de 1 hora)
    @PostMapping("/registros/registrar-por-matricula")
    @ResponseBody
    public Map<String, Object> registrarPorMatricula(@RequestParam String matricula, @RequestParam String senha) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

            if (!passwordEncoder.matches(senha, usuario.getSenha())) {
                throw new IllegalArgumentException("Senha inválida");
            }

            Colaborador colaborador = usuario.getColaborador();
            if (colaborador == null) {
                throw new IllegalArgumentException("Colaborador não encontrado");
            }

            RegistroPonto registro = registroPontoRepository
                    .findByColaboradorAndData(colaborador, LocalDate.now())
                    .orElse(new RegistroPonto());

            registro.setColaborador(colaborador);
            registro.setData(LocalDate.now());
            registro.setUsuarioCriacao(usuario);
            registro.setTipoRegistro(RegistroPonto.TipoRegistro.AUTOMATICO);

            LocalTime agora = LocalTime.now();
            
            // Validar máximo de 4 batidas por dia
            int totalBatidas = contarBatidasDoDia(registro);
            if (totalBatidas >= 4) {
                throw new IllegalArgumentException("Limite máximo de 4 batidas por dia já atingido");
            }
            
            // Validar intervalo mínimo de 1 hora entre batidas
            LocalTime ultimaBatida = obterUltimaBatida(registro);
            if (ultimaBatida != null) {
                long minutosDecorridos = java.time.Duration.between(ultimaBatida, agora).toMinutes();
                if (minutosDecorridos < 60) {
                    long minutosRestantes = 60 - minutosDecorridos;
                    throw new IllegalArgumentException(
                        String.format("Intervalo mínimo de 1 hora não respeitado. Aguarde mais %d minutos", minutosRestantes)
                    );
                }
            }

            String proximaBatida;

            if (registro.getEntrada1() == null) {
                registro.setEntrada1(agora);
                proximaBatida = "Entrada 1";
            } else if (registro.getSaida1() == null) {
                registro.setSaida1(agora);
                proximaBatida = "Saída 1";
            } else if (registro.getEntrada2() == null) {
                registro.setEntrada2(agora);
                proximaBatida = "Entrada 2";
            } else if (registro.getSaida2() == null) {
                registro.setSaida2(agora);
                proximaBatida = "Saída 2";
            } else {
                throw new IllegalArgumentException("Limite máximo de 4 batidas por dia já atingido");
            }

            registroPontoRepository.save(registro);

            response.put("success", true);
            response.put("horarioFormatado", agora.format(DateTimeFormatter.ofPattern("HH:mm")));
            response.put("colaboradorNome", colaborador.getNome());
            response.put("proximaBatida", proximaBatida);
            response.put("message", "Batida registrada com sucesso!");
            response.put("totalBatidas", totalBatidas + 1);
            response.put("batidasRestantes", 4 - (totalBatidas + 1));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Buscar histórico de registros do dia
    @GetMapping("/registros/hoje")
    @ResponseBody
    public ResponseEntity<List<RegistroPonto>> registrosHoje(@RequestParam String matricula) {
        Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));
        Colaborador colaborador = usuario.getColaborador();
        if (colaborador == null) {
            throw new IllegalArgumentException("Colaborador não encontrado");
        }
        List<RegistroPonto> registros = registroPontoRepository
                .findByColaboradorAndDataBetweenOrderByDataDesc(colaborador, LocalDate.now(), LocalDate.now());
        return ResponseEntity.ok(registros);
    }

    // Buscar últimos registros formatados para exibição
    @GetMapping("/registros/ultimos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ultimosRegistros() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Buscar os últimos 10 registros de todos os colaboradores
            List<RegistroPonto> registros = registroPontoRepository
                    .findTop10ByOrderByDataCriacaoDesc();
            
            List<Map<String, Object>> registrosFormatados = new ArrayList<>();
            
            for (RegistroPonto registro : registros) {
                // Processar cada batida do registro
                adicionarBatida(registrosFormatados, registro, registro.getEntrada1(), "Entrada", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida1(), "Saída", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getEntrada2(), "Retorno", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida2(), "Saída", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getEntrada3(), "Entrada", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida3(), "Saída", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getEntrada4(), "Entrada", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida4(), "Saída", registro.getData());
            }
            
            // Ordenar por data/hora mais recente primeiro
            registrosFormatados.sort((a, b) -> {
                LocalDateTime dataA = (LocalDateTime) a.get("dataHora");
                LocalDateTime dataB = (LocalDateTime) b.get("dataHora");
                return dataB.compareTo(dataA);
            });
            
            // Limitar aos 10 mais recentes
            if (registrosFormatados.size() > 10) {
                registrosFormatados = registrosFormatados.subList(0, 10);
            }
            
            response.put("success", true);
            response.put("registros", registrosFormatados);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    private void adicionarBatida(List<Map<String, Object>> lista, RegistroPonto registro, 
                                LocalTime horario, String tipo, LocalDate data) {
        if (horario != null) {
            Map<String, Object> batida = new HashMap<>();
            batida.put("horarioFormatado", horario.format(DateTimeFormatter.ofPattern("HH:mm")));
            batida.put("tipo", tipo);
            batida.put("colaboradorNome", registro.getColaborador().getNome());
            batida.put("matricula", registro.getColaborador().getUsuario().getMatricula());
            batida.put("data", data);
            batida.put("dataHora", LocalDateTime.of(data, horario));
            
            // Determinar se é hoje, ontem ou outra data
            LocalDate hoje = LocalDate.now();
            String dataTexto;
            if (data.equals(hoje)) {
                dataTexto = "Hoje";
            } else if (data.equals(hoje.minusDays(1))) {
                dataTexto = "Ontem";
            } else {
                dataTexto = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            batida.put("dataFormatada", dataTexto);
            
            lista.add(batida);
        }
    }

    // Método auxiliar para contar quantas batidas já foram registradas no dia
    private int contarBatidasDoDia(RegistroPonto registro) {
        int count = 0;
        if (registro.getEntrada1() != null) count++;
        if (registro.getSaida1() != null) count++;
        if (registro.getEntrada2() != null) count++;
        if (registro.getSaida2() != null) count++;
        return count;
    }

    // Método auxiliar para obter o horário da última batida registrada
    private LocalTime obterUltimaBatida(RegistroPonto registro) {
        LocalTime ultimaBatida = null;
        
        if (registro.getEntrada1() != null) {
            ultimaBatida = registro.getEntrada1();
        }
        if (registro.getSaida1() != null) {
            ultimaBatida = registro.getSaida1();
        }
        if (registro.getEntrada2() != null) {
            ultimaBatida = registro.getEntrada2();
        }
        if (registro.getSaida2() != null) {
            ultimaBatida = registro.getSaida2();
        }
        
        return ultimaBatida;
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        return "rh/ponto-escalas/relatorios";
    }

    @GetMapping("/relatorio-mensal/pdf")
    public ResponseEntity<byte[]> gerarRelatorioPDF(
            @RequestParam String matricula,
            @RequestParam int ano,
            @RequestParam int mes) {
        
        try {
            // Buscar colaborador pela matrícula do usuário
            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElse(null);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }
            
            Colaborador colaborador = usuario.getColaborador();
            if (colaborador == null) {
                return ResponseEntity.notFound().build();
            }

            // Buscar registros do mês
            YearMonth yearMonth = YearMonth.of(ano, mes);
            LocalDate inicioMes = yearMonth.atDay(1);
            LocalDate fimMes = yearMonth.atEndOfMonth();
            
            List<RegistroPonto> registros = registroPontoRepository
                .findByColaboradorAndDataBetweenOrderByDataDesc(colaborador, inicioMes, fimMes);

            // Gerar PDF
            byte[] pdfBytes = gerarPDFEspelhoPonto(colaborador, registros, ano, mes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("espelho_ponto_%s_%02d_%d.pdf", matricula, mes, ano));

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/relatorios/espelho-profissional/{matricula}")
    public ResponseEntity<byte[]> gerarEspelhoProfissional(
            @PathVariable String matricula,
            @RequestParam int ano,
            @RequestParam int mes) {
        
        try {
            // Buscar usuário pela matrícula
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorMatricula(matricula);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Usuario usuario = usuarioOpt.get();
            
            Colaborador colaborador = usuario.getColaborador();
            if (colaborador == null) {
                return ResponseEntity.notFound().build();
            }

            // Buscar registros do mês
            YearMonth yearMonth = YearMonth.of(ano, mes);
            LocalDate inicioMes = yearMonth.atDay(1);
            LocalDate fimMes = yearMonth.atEndOfMonth();
            
            List<RegistroPonto> registros = registroPontoRepository
                .findByColaboradorAndDataBetweenOrderByDataDesc(colaborador, inicioMes, fimMes);

            // Gerar PDF Profissional
            byte[] pdfBytes = espelhoPontoProfissionalService.gerarEspelhoProfissional(colaborador, registros, ano, mes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("espelho_ponto_profissional_%s_%02d_%d.pdf", matricula, mes, ano));

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] gerarPDFEspelhoPonto(Colaborador colaborador, List<RegistroPonto> registros, 
                                       int ano, int mes) throws DocumentException, IOException {
        
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("ESPELHO DE PONTO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" ")); // Espaço

        // Informações do colaborador
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        
        document.add(new Paragraph("Colaborador: " + colaborador.getNome(), boldFont));
        document.add(new Paragraph("Matrícula: " + colaborador.getUsuario().getMatricula(), normalFont));
        document.add(new Paragraph("Período: " + String.format("%02d/%d", mes, ano), normalFont));
        
        document.add(new Paragraph(" ")); // Espaço

        // Tabela de registros
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 2, 2, 2});

        // Cabeçalho da tabela
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        addTableHeader(table, "Data", headerFont);
        addTableHeader(table, "Entrada 1", headerFont);
        addTableHeader(table, "Saída 1", headerFont);
        addTableHeader(table, "Entrada 2", headerFont);
        addTableHeader(table, "Saída 2", headerFont);
        addTableHeader(table, "Total Horas", headerFont);

        // Dados da tabela
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (RegistroPonto registro : registros) {
            addTableCell(table, registro.getData().format(dateFormatter), cellFont);
            addTableCell(table, registro.getEntrada1() != null ? 
                registro.getEntrada1().format(timeFormatter) : "-", cellFont);
            addTableCell(table, registro.getSaida1() != null ? 
                registro.getSaida1().format(timeFormatter) : "-", cellFont);
            addTableCell(table, registro.getEntrada2() != null ? 
                registro.getEntrada2().format(timeFormatter) : "-", cellFont);
            addTableCell(table, registro.getSaida2() != null ? 
                registro.getSaida2().format(timeFormatter) : "-", cellFont);
            
            // Calcular total de horas
            String totalHoras = calcularTotalHoras(registro);
            addTableCell(table, totalHoras, cellFont);
        }

        document.add(table);

        // Rodapé
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Relatório gerado em: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
            FontFactory.getFont(FontFactory.HELVETICA, 8));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private String calcularTotalHoras(RegistroPonto registro) {
        if (registro.getEntrada1() == null || registro.getSaida1() == null) {
            return "-";
        }

        long minutosTotais = 0;

        // Período manhã
        if (registro.getEntrada1() != null && registro.getSaida1() != null) {
            minutosTotais += java.time.Duration.between(registro.getEntrada1(), registro.getSaida1()).toMinutes();
        }

        // Período tarde
        if (registro.getEntrada2() != null && registro.getSaida2() != null) {
            minutosTotais += java.time.Duration.between(registro.getEntrada2(), registro.getSaida2()).toMinutes();
        }

        long horas = minutosTotais / 60;
        long minutos = minutosTotais % 60;

        return String.format("%02d:%02d", horas, minutos);
    }

}
