package com.jaasielsilva.portalceo.service.rh.treinamentos;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.agenda.Evento;
import com.jaasielsilva.portalceo.model.treinamentos.*;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.agenda.EventoRepository;
import com.jaasielsilva.portalceo.repository.treinamentos.TreinamentoAvaliacaoRepository;
import com.jaasielsilva.portalceo.service.EmailService;
import com.jaasielsilva.portalceo.repository.treinamentos.*;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TreinamentoService {
    @Autowired
    private TreinamentoCursoRepository cursoRepository;
    @Autowired
    private TreinamentoInstrutorRepository instrutorRepository;
    @Autowired
    private TreinamentoTurmaRepository turmaRepository;
    @Autowired
    private TreinamentoMatriculaRepository matriculaRepository;
    @Autowired
    private TreinamentoFrequenciaRepository frequenciaRepository;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private ColaboradorRepository colaboradorRepository;
    @Autowired
    private TreinamentoAvaliacaoRepository avaliacaoRepository;
    @Autowired
    private EmailService emailService;

    @Transactional
    public TreinamentoCurso criarCurso(String titulo, String descricao, String categoria, Integer cargaHoraria) {
        TreinamentoCurso c = new TreinamentoCurso();
        c.setTitulo(titulo);
        c.setDescricao(descricao);
        c.setCategoria(categoria);
        c.setCargaHoraria(cargaHoraria);
        return cursoRepository.save(c);
    }

    @Transactional
    public TreinamentoInstrutor criarInstrutor(String nome, String email, String bio) {
        TreinamentoInstrutor i = new TreinamentoInstrutor();
        i.setNome(nome);
        i.setEmail(email);
        i.setBio(bio);
        return instrutorRepository.save(i);
    }

    @Transactional
    public TreinamentoTurma criarTurma(Long cursoId, Long instrutorId, LocalDateTime inicio, LocalDateTime fim, String local, Integer capacidade) {
        TreinamentoCurso curso = cursoRepository.findById(cursoId).orElseThrow();
        TreinamentoInstrutor instrutor = instrutorId != null ? instrutorRepository.findById(instrutorId).orElse(null) : null;
        TreinamentoTurma t = new TreinamentoTurma();
        t.setCurso(curso);
        t.setInstrutor(instrutor);
        t.setInicio(inicio);
        t.setFim(fim);
        t.setLocal(local);
        t.setCapacidade(capacidade);
        t.setStatus("ABERTA");
        TreinamentoTurma saved = turmaRepository.save(t);
        Evento e = new Evento();
        e.setTitulo("Treinamento: " + curso.getTitulo());
        e.setDescricao("Turma " + saved.getId());
        e.setDataHoraInicio(inicio);
        e.setDataHoraFim(fim);
        e.setLocal(local);
        e.setLembrete(true);
        Evento evSaved = eventoRepository.save(e);
        saved.setAgendaEventoId(evSaved.getId());
        return turmaRepository.save(saved);
    }

    @Transactional
    public TreinamentoMatricula matricular(Long turmaId, Long colaboradorId) {
        TreinamentoTurma turma = turmaRepository.findById(turmaId).orElseThrow();
        if (turma.getCapacidade() != null) {
            List<TreinamentoMatricula> ms = matriculaRepository.findByTurmaId(turmaId);
            if (ms.size() >= turma.getCapacidade()) throw new IllegalStateException("Turma lotada");
        }
        Colaborador c = colaboradorRepository.findById(colaboradorId).orElseThrow();
        TreinamentoMatricula m = new TreinamentoMatricula();
        m.setTurma(turma);
        m.setColaborador(c);
        m.setStatus("ATIVA");
        m.setDataMatricula(LocalDateTime.now());
        TreinamentoMatricula saved = matriculaRepository.save(m);
        if (c.getEmail() != null) {
            String assunto = "Confirmação de matrícula em treinamento";
            String corpo = "<html><body><h2>Matrícula confirmada</h2><p>Curso: " + turma.getCurso().getTitulo() + "</p><p>Período: " + turma.getInicio() + " a " + turma.getFim() + "</p><p>Local: " + (turma.getLocal()!=null?turma.getLocal():"-") + "</p></body></html>";
            emailService.enviarEmail(c.getEmail(), assunto, corpo);
        }
        return saved;
    }

    @Transactional
    public TreinamentoFrequencia registrarFrequencia(Long matriculaId, LocalDateTime dataHora, boolean presente, String observacoes) {
        TreinamentoMatricula m = matriculaRepository.findById(matriculaId).orElseThrow();
        TreinamentoFrequencia f = new TreinamentoFrequencia();
        f.setMatricula(m);
        f.setDataHora(dataHora);
        f.setPresente(presente);
        f.setObservacoes(observacoes);
        return frequenciaRepository.save(f);
    }

    @Transactional
    public TreinamentoAvaliacao avaliar(Long matriculaId, Integer nota, String feedback) {
        TreinamentoMatricula m = matriculaRepository.findById(matriculaId).orElseThrow();
        TreinamentoAvaliacao a = new TreinamentoAvaliacao();
        a.setMatricula(m);
        a.setNota(nota);
        a.setFeedback(feedback);
        return avaliacaoRepository.save(a);
    }

    @Transactional(readOnly = true)
    public Page<TreinamentoTurma> listarTurmas(String status, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 100));
        if (status != null && !status.isBlank()) return turmaRepository.findByStatus(status, p);
        return turmaRepository.findAll(p);
    }

    @Transactional(readOnly = true)
    public java.util.List<TreinamentoCurso> listarCursos() { return cursoRepository.findAll(); }

    @Transactional(readOnly = true)
    public java.util.List<TreinamentoInstrutor> listarInstrutores() { return instrutorRepository.findAll(); }

    @Transactional
    public TreinamentoTurma alterarStatusTurma(Long id, String status) {
        TreinamentoTurma t = turmaRepository.findById(id).orElseThrow();
        t.setStatus(status);
        return turmaRepository.save(t);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<TreinamentoTurma> findTurma(Long id) { return turmaRepository.findById(id); }

    @Transactional(readOnly = true)
    public java.util.List<TreinamentoMatricula> listarMatriculasPorTurma(Long turmaId) { return matriculaRepository.findByTurmaId(turmaId); }

    @Transactional(readOnly = true)
    public java.util.List<TreinamentoFrequencia> listarFrequenciasPorMatricula(Long matriculaId) { return frequenciaRepository.findByMatriculaId(matriculaId); }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> relatorioTurmas(String status) {
        List<TreinamentoTurma> turmas = (status != null && !status.isBlank())
                ? turmaRepository.findByStatus(status, PageRequest.of(0, 1000)).getContent()
                : turmaRepository.findAll();
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (TreinamentoTurma t : turmas) {
            List<TreinamentoMatricula> ms = matriculaRepository.findByTurmaId(t.getId());
            int participantes = ms.size();
            List<TreinamentoFrequencia> fs = ms.stream()
                    .flatMap(m -> frequenciaRepository.findByMatriculaId(m.getId()).stream())
                    .toList();
            long presencas = fs.stream().filter(f -> Boolean.TRUE.equals(f.getPresente())).count();
            double taxaPresenca = fs.isEmpty() ? 0.0 : (double) presencas / (double) fs.size();
            List<TreinamentoAvaliacao> avs = avaliacaoRepository.findByMatriculaTurmaId(t.getId());
            double mediaAvaliacao = avs.isEmpty() ? 0.0 : avs.stream()
                    .mapToInt(a -> a.getNota() != null ? a.getNota() : 0)
                    .average().orElse(0.0);
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("curso", t.getCurso() != null ? t.getCurso().getTitulo() : null);
            m.put("instrutor", t.getInstrutor() != null ? t.getInstrutor().getNome() : null);
            m.put("inicio", t.getInicio());
            m.put("fim", t.getFim());
            m.put("status", t.getStatus());
            m.put("participantes", participantes);
            m.put("taxaPresenca", taxaPresenca);
            m.put("mediaAvaliacao", mediaAvaliacao);
            out.add(m);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public String exportCsv(String status) {
        List<Map<String, Object>> dados = relatorioTurmas(status);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Curso,Instrutor,Inicio,Fim,Status,Participantes,TaxaPresenca,MediaAvaliacao\n");
        for (Map<String,Object> m : dados) {
            sb.append(m.get("id")).append(",")
              .append(safe(m.get("curso"))).append(",")
              .append(safe(m.get("instrutor"))).append(",")
              .append(m.get("inicio")).append(",")
              .append(m.get("fim")).append(",")
              .append(safe(m.get("status"))).append(",")
              .append(m.get("participantes")).append(",")
              .append(m.get("taxaPresenca")).append(",")
              .append(m.get("mediaAvaliacao")).append("\n");
        }
        return sb.toString();
    }

    private String safe(Object o) { return o == null ? "" : o.toString().replace(",", " "); }

    @Transactional(readOnly = true)
    public byte[] exportPdf(String status) {
        java.util.List<java.util.Map<String,Object>> dados = relatorioTurmas(status);
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<h2>Relatório de Treinamentos</h2>");
        html.append("<table border='1' cellspacing='0' cellpadding='4'>");
        html.append("<tr><th>ID</th><th>Curso</th><th>Instrutor</th><th>Início</th><th>Fim</th><th>Status</th><th>Participantes</th><th>Taxa Presença</th><th>Média Avaliação</th></tr>");
        for (java.util.Map<String,Object> m : dados) {
            html.append("<tr>")
                .append("<td>").append(m.get("id")).append("</td>")
                .append("<td>").append(safe(m.get("curso"))).append("</td>")
                .append("<td>").append(safe(m.get("instrutor"))).append("</td>")
                .append("<td>").append(safe(m.get("inicio"))).append("</td>")
                .append("<td>").append(safe(m.get("fim"))).append("</td>")
                .append("<td>").append(safe(m.get("status"))).append("</td>")
                .append("<td>").append(m.get("participantes")).append("</td>")
                .append("<td>").append(m.get("taxaPresenca")).append("</td>")
                .append("<td>").append(m.get("mediaAvaliacao")).append("</td>")
                .append("</tr>");
        }
        html.append("</table></body></html>");
        try {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            builder.withHtmlContent(html.toString(), null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Transactional
    public void enviarLembretesTurma(Long turmaId) {
        TreinamentoTurma t = turmaRepository.findById(turmaId).orElseThrow();
        java.util.List<TreinamentoMatricula> ms = matriculaRepository.findByTurmaId(turmaId);
        for (TreinamentoMatricula m : ms) {
            Colaborador c = m.getColaborador();
            if (c != null && c.getEmail() != null) {
                String assunto = "Lembrete de treinamento";
                String corpo = "<html><body><h2>Lembrete</h2><p>Curso: " + (t.getCurso()!=null?t.getCurso().getTitulo():"Treinamento") + "</p><p>Período: " + t.getInicio() + " a " + t.getFim() + "</p><p>Local: " + (t.getLocal()!=null?t.getLocal():"-") + "</p></body></html>";
                emailService.enviarEmail(c.getEmail(), assunto, corpo);
            }
        }
    }

    @Transactional(readOnly = true)
    public byte[] gerarCertificado(Long matriculaId) {
        TreinamentoMatricula m = matriculaRepository.findById(matriculaId).orElseThrow();
        TreinamentoTurma t = m.getTurma();
        String nome = m.getColaborador()!=null? m.getColaborador().getNome(): "Colaborador";
        String curso = t.getCurso()!=null? t.getCurso().getTitulo(): "Treinamento";
        String instrutor = t.getInstrutor()!=null? t.getInstrutor().getNome(): "";
        String inicio = String.valueOf(t.getInicio());
        String fim = String.valueOf(t.getFim());
        String html = "<html><head><meta charset='UTF-8'></head><body>" +
                "<div style='text-align:center;border:4px solid #333;padding:40px;font-family:sans-serif'>" +
                "<h1>Certificado de Participação</h1>" +
                "<p>Certificamos que <strong>" + nome + "</strong> participou do curso <strong>" + curso + "</strong>.</p>" +
                "<p>Período: " + inicio + " a " + fim + "</p>" +
                (instrutor.isEmpty()?"":"<p>Instrutor: " + instrutor + "</p>") +
                "<p>Emitido em: " + java.time.LocalDate.now() + "</p>" +
                "</div></body></html>";
        try {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
