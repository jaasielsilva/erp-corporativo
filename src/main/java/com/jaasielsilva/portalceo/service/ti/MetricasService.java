package com.jaasielsilva.portalceo.service.ti;

import com.jaasielsilva.portalceo.model.ti.SistemaMetricas;
import com.jaasielsilva.portalceo.repository.ti.SistemaMetricasRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MetricasService {
    private final SistemaMetricasRepository metricasRepository;

    public MetricasService(SistemaMetricasRepository metricasRepository) {
        this.metricasRepository = metricasRepository;
    }

    public Map<String, Object> obterMetricasAtuais() {
        SistemaMetricas atual = metricasRepository.findTopByOrderByCreatedAtDesc();
        if (atual == null) {
            semearMetricasIniciais();
            atual = metricasRepository.findTopByOrderByCreatedAtDesc();
        }
        Map<String, Object> m = new HashMap<>();
        m.put("cpuUsage", atual.getCpuUsage());
        m.put("memoryUsage", atual.getMemoryUsage());
        m.put("diskUsage", atual.getDiskUsage());
        m.put("networkLatency", atual.getNetworkLatency());
        m.put("createdAt", atual.getCreatedAt());
        return m;
    }

    public List<Map<String, Object>> listarHistorico(int limit) {
        List<SistemaMetricas> lista = metricasRepository.findTop20ByOrderByCreatedAtDesc();
        List<Map<String, Object>> out = new ArrayList<>();
        for (SistemaMetricas s : lista) {
            Map<String, Object> m = new HashMap<>();
            m.put("cpu", s.getCpuUsage());
            m.put("mem", s.getMemoryUsage());
            m.put("disk", s.getDiskUsage());
            m.put("latency", s.getNetworkLatency());
            m.put("ts", s.getCreatedAt());
            out.add(m);
        }
        return out;
    }

    private void semearMetricasIniciais() {
        LocalDateTime base = LocalDateTime.now().minusHours(2);
        List<SistemaMetricas> seeds = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < 12; i++) {
            double cpu = 30 + r.nextDouble() * 40; // 30-70%
            double mem = 40 + r.nextDouble() * 40; // 40-80%
            double disk = 20 + r.nextDouble() * 30; // 20-50%
            double lat = 8 + r.nextDouble() * 10; // 8-18ms
            seeds.add(new SistemaMetricas(null, round(cpu), round(mem), round(disk), round(lat), base.plusMinutes(i * 10)));
        }
        metricasRepository.saveAll(seeds);
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}