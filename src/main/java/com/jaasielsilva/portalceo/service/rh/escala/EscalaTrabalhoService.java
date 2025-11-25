package com.jaasielsilva.portalceo.service.rh.escala;

import com.jaasielsilva.portalceo.dto.rh.escala.EscalaTrabalhoDTO;
import com.jaasielsilva.portalceo.model.EscalaTrabalho;
import com.jaasielsilva.portalceo.model.EscalaTrabalho.TipoEscala;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.EscalaTrabalhoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalaTrabalhoService {

    private final EscalaTrabalhoRepository repository;

    public EscalaTrabalhoService(EscalaTrabalhoRepository repository) {
        this.repository = repository;
    }

    public EscalaTrabalho criarEscala(EscalaTrabalhoDTO dto, Usuario usuario) {
        EscalaTrabalho escala = new EscalaTrabalho();
        escala.setNome(dto.nome);
        escala.setDescricao(dto.descricao);
        escala.setTipo(TipoEscala.valueOf(dto.tipo.toUpperCase()));
        escala.setHorarioEntrada1(dto.horarioEntrada1);
        escala.setHorarioSaida1(dto.horarioSaida1);
        escala.setHorarioEntrada2(dto.horarioEntrada2);
        escala.setHorarioSaida2(dto.horarioSaida2);
        escala.setIntervaloMinimo(dto.intervaloMinimo);
        escala.setToleranciaAtraso(dto.toleranciaAtraso != null ? dto.toleranciaAtraso : true);
        escala.setMinutosTolerancia(dto.minutosTolerancia != null ? dto.minutosTolerancia : 10);
        escala.setDataVigenciaInicio(dto.dataVigenciaInicio);
        escala.setDataVigenciaFim(dto.dataVigenciaFim);
        escala.setUsuarioCriacao(usuario);

        // Configura dias trabalhados
        List<String> dias = dto.diasTrabalhados;
        escala.setTrabalhaSegunda(dias.contains("SEG"));
        escala.setTrabalhaTerca(dias.contains("TER"));
        escala.setTrabalhaQuarta(dias.contains("QUA"));
        escala.setTrabalhaQuinta(dias.contains("QUI"));
        escala.setTrabalhaSexta(dias.contains("SEX"));
        escala.setTrabalhaSabado(dias.contains("SAB"));
        escala.setTrabalhaDomingo(dias.contains("DOM"));

        // TODO: salvar departamentos e turnos se você tiver entidades para isso

        return repository.save(escala);
    }
}
