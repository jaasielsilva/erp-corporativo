package com.jaasielsilva.portalceo.model.ti;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ti_sistema_metricas")
public class SistemaMetricas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double cpuUsage; // percentual
    private Double memoryUsage; // percentual
    private Double diskUsage; // percentual
    private Double networkLatency; // ms

    private LocalDateTime createdAt;

    public SistemaMetricas() {}

    public SistemaMetricas(Long id, Double cpuUsage, Double memoryUsage, Double diskUsage, Double networkLatency, LocalDateTime createdAt) {
        this.id = id;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.networkLatency = networkLatency;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
    public Double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }
    public Double getDiskUsage() { return diskUsage; }
    public void setDiskUsage(Double diskUsage) { this.diskUsage = diskUsage; }
    public Double getNetworkLatency() { return networkLatency; }
    public void setNetworkLatency(Double networkLatency) { this.networkLatency = networkLatency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}