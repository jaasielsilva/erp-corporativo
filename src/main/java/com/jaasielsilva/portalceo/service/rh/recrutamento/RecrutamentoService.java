package com.jaasielsilva.portalceo.service.rh.recrutamento;

import com.jaasielsilva.portalceo.model.agenda.Evento;
import com.jaasielsilva.portalceo.model.recrutamento.*;
import com.jaasielsilva.portalceo.repository.agenda.EventoRepository;
import com.jaasielsilva.portalceo.repository.ColaboradorRepository;
import com.jaasielsilva.portalceo.repository.recrutamento.*;
import com.jaasielsilva.portalceo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class RecrutamentoService {
    @Autowired private RecrutamentoCandidatoRepository candidatoRepo;
    @Autowired private RecrutamentoCandidatoExperienciaRepository expRepo;
    @Autowired private RecrutamentoCandidatoFormacaoRepository formRepo;
    @Autowired private RecrutamentoCandidatoHabilidadeRepository habRepo;
    @Autowired private RecrutamentoVagaRepository vagaRepo;
    @Autowired private RecrutamentoCandidaturaRepository candRepo;
    @Autowired private RecrutamentoEntrevistaRepository entrevistaRepo;
    @Autowired private RecrutamentoAvaliacaoRepository avaliacaoRepo;
    @Autowired private RecrutamentoDivulgacaoRepository divulgacaoRepo;
    @Autowired private EventoRepository eventoRepo;
    @Autowired private EmailService emailService;
    @Autowired private ColaboradorRepository colaboradorRepository;

    @Transactional
    public RecrutamentoCandidato criarCandidato(String nome, String email, String telefone, String genero, LocalDate dataNascimento) {
        RecrutamentoCandidato c = new RecrutamentoCandidato();
        c.setNome(nome); c.setEmail(email); c.setTelefone(telefone); c.setGenero(genero); c.setDataNascimento(dataNascimento);
        return candidatoRepo.save(c);
    }

    @Transactional
    public RecrutamentoCandidatoExperiencia adicionarExperiencia(Long candidatoId, String empresa, String cargo, LocalDate inicio, LocalDate fim, String descricao) {
        RecrutamentoCandidato candidato = candidatoRepo.findById(candidatoId).orElseThrow();
        RecrutamentoCandidatoExperiencia e = new RecrutamentoCandidatoExperiencia();
        e.setCandidato(candidato); e.setEmpresa(empresa); e.setCargo(cargo); e.setInicio(inicio); e.setFim(fim); e.setDescricao(descricao);
        return expRepo.save(e);
    }

    @Transactional
    public RecrutamentoCandidatoFormacao adicionarFormacao(Long candidatoId, String instituicao, String curso, String nivel, LocalDate inicio, LocalDate fim) {
        RecrutamentoCandidato candidato = candidatoRepo.findById(candidatoId).orElseThrow();
        RecrutamentoCandidatoFormacao f = new RecrutamentoCandidatoFormacao();
        f.setCandidato(candidato); f.setInstituicao(instituicao); f.setCurso(curso); f.setNivel(nivel); f.setInicio(inicio); f.setFim(fim);
        return formRepo.save(f);
    }

    @Transactional
    public RecrutamentoCandidatoHabilidade adicionarHabilidade(Long candidatoId, String nome, String nivel) {
        RecrutamentoCandidato candidato = candidatoRepo.findById(candidatoId).orElseThrow();
        RecrutamentoCandidatoHabilidade h = new RecrutamentoCandidatoHabilidade();
        h.setCandidato(candidato); h.setNome(nome); h.setNivel(nivel);
        return habRepo.save(h);
    }

    @Transactional
    public RecrutamentoVaga criarVaga(String titulo, String descricao, String departamento, String senioridade, String localidade, String tipoContrato,
                                      String responsabilidades, String requisitos, String diferenciais, String beneficios) {
        RecrutamentoVaga v = new RecrutamentoVaga();
        v.setTitulo(titulo); v.setDescricao(descricao); v.setDepartamento(departamento); v.setSenioridade(senioridade); v.setLocalidade(localidade); v.setTipoContrato(tipoContrato); v.setStatus("ABERTA");
        v.setResponsabilidades(responsabilidades);
        v.setRequisitos(requisitos);
        v.setDiferenciais(diferenciais);
        v.setBeneficios(beneficios);
        return vagaRepo.save(v);
    }

    @Transactional
    public RecrutamentoCandidatura candidatar(Long candidatoId, Long vagaId, String origem) {
        if (candRepo.existsByCandidatoIdAndVagaId(candidatoId, vagaId)) {
            throw new IllegalArgumentException("Candidato já possui candidatura nesta vaga");
        }
        RecrutamentoCandidato candidato = candidatoRepo.findById(candidatoId).orElseThrow();
        RecrutamentoVaga vaga = vagaRepo.findById(vagaId).orElseThrow();
        RecrutamentoCandidatura c = new RecrutamentoCandidatura();
        c.setCandidato(candidato); c.setVaga(vaga); c.setEtapa("TRIAGEM"); c.setInicio(LocalDateTime.now()); c.setOrigem(origem);
        return candRepo.save(c);
    }

    @Transactional
    public RecrutamentoEntrevista agendarEntrevista(Long candidaturaId, LocalDateTime inicio, LocalDateTime fim, String local, String tipo) {
        RecrutamentoCandidatura c = candRepo.findById(candidaturaId).orElseThrow();
        RecrutamentoEntrevista e = new RecrutamentoEntrevista();
        e.setCandidatura(c); e.setInicio(inicio); e.setFim(fim); e.setLocal(local); e.setTipo(tipo);
        Evento ev = new Evento(); ev.setTitulo("Entrevista: " + c.getVaga().getTitulo()); ev.setDescricao("Candidato " + c.getCandidato().getNome()); ev.setDataHoraInicio(inicio); ev.setDataHoraFim(fim); ev.setLocal(local); ev.setLembrete(true);
        Evento saved = eventoRepo.save(ev); e.setAgendaEventoId(saved.getId());
        RecrutamentoEntrevista out = entrevistaRepo.save(e);
        if (c.getCandidato().getEmail()!=null) {
            String assunto = "Agendamento de entrevista";
            String corpo = "<html><body><h2>Entrevista agendada</h2><p>Vaga: " + c.getVaga().getTitulo() + "</p><p>Período: " + inicio + " a " + fim + "</p><p>Local: " + (local!=null?local:"-") + "</p></body></html>";
            emailService.enviarEmail(c.getCandidato().getEmail(), assunto, corpo);
        }
        return out;
    }

    @Transactional
    public RecrutamentoAvaliacao avaliar(Long candidaturaId, Integer nota, String feedback) {
        RecrutamentoCandidatura c = candRepo.findById(candidaturaId).orElseThrow();
        RecrutamentoAvaliacao a = new RecrutamentoAvaliacao(); a.setCandidatura(c); a.setNota(nota); a.setFeedback(feedback);
        return avaliacaoRepo.save(a);
    }

    @Transactional
    public RecrutamentoCandidatura alterarEtapa(Long candidaturaId, String etapa) {
        RecrutamentoCandidatura c = candRepo.findById(candidaturaId).orElseThrow();
        c.setEtapa(etapa);
        if ("CONTRATADO".equalsIgnoreCase(etapa) || "REPROVADO".equalsIgnoreCase(etapa)) c.setConclusao(LocalDateTime.now());
        return candRepo.save(c);
    }

    @Transactional
    public RecrutamentoDivulgacao registrarDivulgacao(Long vagaId, String plataforma, String url) {
        RecrutamentoVaga v = vagaRepo.findById(vagaId).orElseThrow();
        RecrutamentoDivulgacao d = new RecrutamentoDivulgacao(); d.setVaga(v); d.setPlataforma(plataforma); d.setUrl(url);
        return divulgacaoRepo.save(d);
    }

    @Transactional(readOnly = true)
    public Page<RecrutamentoVaga> listarVagas(String status, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 100));
        if (status != null && !status.isBlank()) return vagaRepo.findByStatus(status, p);
        return vagaRepo.findAll(p);
    }

    @Transactional(readOnly = true)
    public Page<RecrutamentoCandidato> listarCandidatos(String q, String nome, String email, String telefone, String genero, LocalDate nasc, int page, int size) {
        Pageable p = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 100));
        return candidatoRepo.buscarComFiltros(q!=null && !q.isBlank()? q.trim(): null,
                nome!=null && !nome.isBlank()? nome.trim(): null,
                email!=null && !email.isBlank()? email.trim(): null,
                telefone!=null && !telefone.isBlank()? telefone.trim(): null,
                genero!=null && !genero.isBlank()? genero.trim(): null,
                nasc,
                p);
    }

    @Transactional
    public Map<String,Object> importarColaboradoresComoCandidatos(boolean apenasAtivos) {
        List<com.jaasielsilva.portalceo.model.Colaborador> colaboradores = colaboradorRepository.findAll();
        int criados = 0;
        int ignorados = 0;
        java.util.List<RecrutamentoCandidato> novos = new java.util.ArrayList<>();
        for (var col : colaboradores) {
            if (apenasAtivos && (col.getAtivo() == null || !col.getAtivo())) { ignorados++; continue; }
            String email = col.getEmail();
            if (email != null && candidatoRepo.existsByEmailIgnoreCase(email)) { ignorados++; continue; }
            RecrutamentoCandidato c = new RecrutamentoCandidato();
            c.setNome(col.getNome());
            c.setEmail(email);
            c.setTelefone(col.getTelefone());
            String genero = null;
            if (col.getSexo() != null) {
                switch (col.getSexo()) {
                    case FEMININO -> genero = "Feminino";
                    case MASCULINO -> genero = "Masculino";
                    default -> genero = "Outro";
                }
            }
            c.setGenero(genero);
            c.setDataNascimento(col.getDataNascimento());
            novos.add(c);
        }
        if (!novos.isEmpty()) {
            candidatoRepo.saveAll(novos);
            criados = novos.size();
        }
        return java.util.Map.of("success", true, "criados", criados, "ignorados", ignorados);
    }

    @Transactional(readOnly = true)
    public java.util.List<RecrutamentoCandidatura> listarCandidaturas(Long vagaId, String etapa) {
        if (vagaId != null) return candRepo.findByVagaId(vagaId);
        if (etapa != null && !etapa.isBlank()) return candRepo.findByEtapa(etapa);
        return candRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Map<String,Object>> relatorioMetrics(String etapa) {
        List<RecrutamentoCandidatura> cs = etapa!=null && !etapa.isBlank()? candRepo.findByEtapa(etapa): candRepo.findAll();
        long total = cs.size();
        long contratados = cs.stream().filter(c -> "CONTRATADO".equalsIgnoreCase(c.getEtapa())).count();
        long reprovados = cs.stream().filter(c -> "REPROVADO".equalsIgnoreCase(c.getEtapa())).count();
        double taxaConversao = total==0?0.0: (double) contratados / (double) total;
        double tempoMedioContratacao = cs.stream().filter(c -> c.getConclusao()!=null && c.getInicio()!=null)
                .mapToLong(c -> Duration.between(c.getInicio(), c.getConclusao()).toDays()).average().orElse(0.0);
        long diversidadeFeminino = cs.stream().filter(c -> c.getCandidato()!=null && "Feminino".equalsIgnoreCase(c.getCandidato().getGenero())).count();
        long diversidadeMasculino = cs.stream().filter(c -> c.getCandidato()!=null && "Masculino".equalsIgnoreCase(c.getCandidato().getGenero())).count();
        java.util.Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("total", total); m.put("contratados", contratados); m.put("reprovados", reprovados);
        m.put("taxaConversao", taxaConversao); m.put("tempoMedioContratacaoDias", tempoMedioContratacao);
        m.put("diversidadeFeminino", diversidadeFeminino); m.put("diversidadeMasculino", diversidadeMasculino);
        return java.util.List.of(m);
    }

    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String,Object>> relatorioPorEtapa() {
        String[] etapas = new String[]{"TRIAGEM","ENTREVISTA","OFERTA","CONTRATADO","REPROVADO"};
        java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
        for (String e : etapas) {
            java.util.List<RecrutamentoCandidatura> list = candRepo.findByEtapa(e);
            java.util.Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("etapa", e);
            m.put("count", list.size());
            long contratados = list.stream().filter(c -> "CONTRATADO".equalsIgnoreCase(c.getEtapa())).count();
            long reprovados = list.stream().filter(c -> "REPROVADO".equalsIgnoreCase(c.getEtapa())).count();
            m.put("contratados", contratados);
            m.put("reprovados", reprovados);
            out.add(m);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public String exportCsv(String etapa) {
        List<Map<String,Object>> d = relatorioMetrics(etapa);
        StringBuilder sb = new StringBuilder();
        sb.append("total,contratados,reprovados,taxaConversao,tempoMedioContratacaoDias,diversidadeFeminino,diversidadeMasculino\n");
        for (Map<String,Object> m : d) {
            sb.append(m.get("total")).append(",")
              .append(m.get("contratados")).append(",")
              .append(m.get("reprovados")).append(",")
              .append(m.get("taxaConversao")).append(",")
              .append(m.get("tempoMedioContratacaoDias")).append(",")
              .append(m.get("diversidadeFeminino")).append(",")
              .append(m.get("diversidadeMasculino")).append("\n");
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public byte[] exportPdf(String etapa) {
        java.util.List<java.util.Map<String,Object>> d = relatorioMetrics(etapa);
        String html = "<html><head><meta charset='UTF-8'></head><body><h2>Relatório de Recrutamento</h2><table border='1' cellspacing='0' cellpadding='4'><tr><th>Total</th><th>Contratados</th><th>Reprovados</th><th>Taxa Conversão</th><th>Tempo Médio (dias)</th><th>Diversidade F</th><th>Diversidade M</th></tr>" +
                d.stream().map(m -> "<tr><td>"+m.get("total")+"</td><td>"+m.get("contratados")+"</td><td>"+m.get("reprovados")+"</td><td>"+m.get("taxaConversao")+"</td><td>"+m.get("tempoMedioContratacaoDias")+"</td><td>"+m.get("diversidadeFeminino")+"</td><td>"+m.get("diversidadeMasculino")+"</td></tr>").reduce("", (a,b)->a+b) + "</table></body></html>";
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

    @Transactional
    public java.util.Map<String,Object> enviarDivulgacaoExterna(Long vagaId, String plataforma, String endpointUrl) {
        RecrutamentoVaga v = vagaRepo.findById(vagaId).orElseThrow();
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String payload = "{\"titulo\":\"" + v.getTitulo() + "\",\"descricao\":\"" + (v.getDescricao()!=null?v.getDescricao():"") + "\",\"departamento\":\"" + (v.getDepartamento()!=null?v.getDepartamento():"") + "\",\"localidade\":\"" + (v.getLocalidade()!=null?v.getLocalidade():"") + "\"}";
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(endpointUrl))
                    .header("Content-Type","application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            java.net.http.HttpResponse<String> resp = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            java.util.Map<String,Object> result = new java.util.LinkedHashMap<>();
            result.put("success", resp.statusCode()>=200 && resp.statusCode()<300);
            result.put("status", resp.statusCode());
            result.put("body", resp.body());
            return result;
        } catch (Exception ex) {
            return java.util.Map.of("success", false, "error", ex.getMessage());
        }
    }
}
