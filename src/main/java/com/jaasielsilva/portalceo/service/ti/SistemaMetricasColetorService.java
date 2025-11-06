package com.jaasielsilva.portalceo.service.ti;

import com.jaasielsilva.portalceo.model.ti.SistemaMetricas;
import com.jaasielsilva.portalceo.repository.ti.SistemaMetricasRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Service
public class SistemaMetricasColetorService {

    private final SistemaMetricasRepository repository;
    private final SystemInfo systemInfo = new SystemInfo();
    private final CentralProcessor processor;
    private long[] prevTicks;

    public SistemaMetricasColetorService(SistemaMetricasRepository repository) {
        this.repository = repository;
        this.processor = systemInfo.getHardware().getProcessor();
        this.prevTicks = processor.getSystemCpuLoadTicks(); // inicializa ticks iniciais
    }

    @Scheduled(fixedRate = 60000) // ele escuta o banco de dados a cada 60 segundos
    public void coletarMetricas() {
        try {
            // CPU
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
            prevTicks = processor.getSystemCpuLoadTicks(); // atualiza ticks para a próxima rodada

            // MEMÓRIA
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            double memoryUsage = (1.0 - ((double) memory.getAvailable() / memory.getTotal())) * 100;

            // DISCO
            HWDiskStore disk = systemInfo.getHardware().getDiskStores().get(0);
            double diskUsage = ((double) disk.getWriteBytes() + disk.getReadBytes()) / (1024 * 1024 * 1024);

            // LATÊNCIA
            double networkLatency = medirLatencia("8.8.8.8");

            // SALVA NO BANCO
            SistemaMetricas metricas = new SistemaMetricas(
                    null,
                    round(cpuUsage),
                    round(memoryUsage),
                    round(diskUsage),
                    round(networkLatency),
                    LocalDateTime.now()
            );
            repository.save(metricas);

            System.out.printf("[Monitoramento] CPU=%.1f%% | MEM=%.1f%% | DISK=%.2fGB | LAT=%.1fms%n",
                    cpuUsage, memoryUsage, diskUsage, networkLatency);

        } catch (Exception e) {
            System.err.println("Erro ao coletar métricas: " + e.getMessage());
        }
    }

    private double medirLatencia(String host) {
        try {
            Process process = new ProcessBuilder("ping", "-c", "1", host).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("time=")) {
                    String time = line.split("time=")[1].split(" ")[0];
                    return Double.parseDouble(time);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao medir latência: " + e.getMessage());
        }
        return -1;
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
