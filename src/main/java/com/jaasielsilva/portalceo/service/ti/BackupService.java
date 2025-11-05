package com.jaasielsilva.portalceo.service.ti;

import com.jaasielsilva.portalceo.model.ti.BackupAgendamento;
import com.jaasielsilva.portalceo.model.ti.BackupRegistro;
import com.jaasielsilva.portalceo.repository.ti.BackupAgendamentoRepository;
import com.jaasielsilva.portalceo.repository.ti.BackupRegistroRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BackupService {
    private final BackupRegistroRepository registroRepository;
    private final BackupAgendamentoRepository agendamentoRepository;

    @Value("${spring.ti.backup.total-capacity-gb:200}")
    private double totalCapacityGb;

    public BackupService(BackupRegistroRepository registroRepository, BackupAgendamentoRepository agendamentoRepository) {
        this.registroRepository = registroRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<Map<String, Object>> listarBackupsRecentes() {
        List<BackupRegistro> registros = registroRepository.listarRecentes();
        if (registros.isEmpty()) {
            // Seed mínimo para não ficar vazio na primeira execução
            registroRepository.saveAll(Arrays.asList(
                new BackupRegistro(null, "FULL", "SUCESSO", LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusMinutes(30), "Backup full diário", 10240.0),
                new BackupRegistro(null, "INCREMENTAL", "SUCESSO", LocalDateTime.now().minusHours(6), LocalDateTime.now().minusHours(6).plusMinutes(10), "Backup incremental", 512.0)
            ));
            registros = registroRepository.listarRecentes();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (BackupRegistro r : registros) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("tipo", r.getTipo());
            m.put("status", r.getStatus());
            // Ajustar chaves para o template: usar "data" e "tamanho"
            m.put("data", r.getDataInicio());
            m.put("descricao", r.getDescricao());
            m.put("tamanho", formatSize(r.getTamanhoMb()));
            out.add(m);
        }
        return out;
    }

    public List<Map<String, Object>> listarAgendamentos() {
        List<BackupAgendamento> agendamentos = agendamentoRepository.findAll();
        if (agendamentos.isEmpty()) {
            agendamentoRepository.saveAll(Arrays.asList(
                new BackupAgendamento(null, "FULL", "DIARIO", LocalDateTime.now().plusDays(1).withHour(2).withMinute(0), true),
                new BackupAgendamento(null, "INCREMENTAL", "HORARIO", LocalDateTime.now().plusHours(1), true)
            ));
            agendamentos = agendamentoRepository.findAll();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (BackupAgendamento a : agendamentos) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("tipo", a.getTipo());
            // Ajustar chave para o template: "frequencia"
            m.put("frequencia", a.getPeriodicidade());
            m.put("proximaExecucao", a.getProximaExecucao());
            m.put("ativo", a.isAtivo());
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> obterEspacoUtilizado() {
        // Em produção, consultar storage real; aqui sumariza dos registros e calcula percentual com capacidade total.
        Double totalMb = registroRepository.findAll().stream()
            .filter(r -> Objects.equals(r.getStatus(), "SUCESSO"))
            .map(BackupRegistro::getTamanhoMb)
            .filter(Objects::nonNull)
            .reduce(0.0, Double::sum);
        double utilizadoGb = (totalMb != null ? totalMb : 0.0) / 1024.0;
        double restanteGb = Math.max(totalCapacityGb - utilizadoGb, 0.0);
        double percentualUso = totalCapacityGb > 0.0 ? Math.min((utilizadoGb / totalCapacityGb) * 100.0, 100.0) : 0.0;

        Map<String, Object> espaco = new HashMap<>();
        espaco.put("utilizado", String.format(Locale.getDefault(), "%.1f GB", utilizadoGb));
        espaco.put("disponivel", String.format(Locale.getDefault(), "%.1f GB", restanteGb));
        espaco.put("percentualUso", percentualUso);
        return espaco;
    }

    public Map<String, Object> iniciarBackup(String tipo, String descricao) {
        BackupRegistro reg = new BackupRegistro();
        reg.setTipo(tipo);
        reg.setStatus("EM_ANDAMENTO");
        reg.setDataInicio(LocalDateTime.now());
        reg.setDescricao(descricao);
        registroRepository.save(reg);

        Map<String, Object> out = new HashMap<>();
        out.put("id", reg.getId());
        out.put("tipo", reg.getTipo());
        out.put("status", reg.getStatus());
        out.put("dataInicio", reg.getDataInicio());
        out.put("descricao", reg.getDescricao());
        return out;
    }

    private String formatSize(Double tamanhoMb) {
        if (tamanhoMb == null) return "-";
        if (tamanhoMb >= 1024.0) {
            double gb = tamanhoMb / 1024.0;
            return String.format(Locale.getDefault(), "%.1f GB", gb);
        }
        return String.format(Locale.getDefault(), "%.0f MB", tamanhoMb);
    }
}