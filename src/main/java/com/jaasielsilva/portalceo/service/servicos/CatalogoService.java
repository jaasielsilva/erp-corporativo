package com.jaasielsilva.portalceo.service.servicos;

import com.jaasielsilva.portalceo.dto.servicos.ServicoDTO;
import com.jaasielsilva.portalceo.model.servicos.Servico;
import com.jaasielsilva.portalceo.repository.servicos.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoService {
    private final ServicoRepository servicoRepository;

    public CatalogoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public List<ServicoDTO> listarCatalogo() {
        List<Servico> ativos = servicoRepository.findByAtivoTrueOrderByNomeAsc();
        return ativos.stream()
                .map(s -> new ServicoDTO(
                        s.getId(), s.getNome(), s.getDescricaoBreve(), s.getCategoria(),
                        s.getSlaRespostaHoras(), s.getSlaSolucaoHoras(), s.getCustoBase()))
                .collect(Collectors.toList());
    }
}