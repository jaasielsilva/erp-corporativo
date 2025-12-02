package com.jaasielsilva.portalceo.service.cnpj;

import com.jaasielsilva.portalceo.dto.cnpj.CnaeDto;
import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;
import com.jaasielsilva.portalceo.dto.cnpj.EnderecoDto;
import com.jaasielsilva.portalceo.model.cnpj.CnaeSecundarioEmbeddable;
import com.jaasielsilva.portalceo.model.cnpj.CnpjConsulta;
import com.jaasielsilva.portalceo.repository.cnpj.CnpjConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CnpjConsultaLogService {

    @Autowired
    private CnpjConsultaRepository repository;

    @Value("${erp.receita.base-url}")
    private String source;

    public void salvarSnapshot(String cnpj, CnpjConsultaDto dto, String protocol) {
        CnpjConsulta entity = new CnpjConsulta();
        entity.setCnpj(cnpj);
        entity.setRazaoSocial(dto.getRazaoSocial());
        entity.setNomeFantasia(dto.getNomeFantasia());
        entity.setSituacaoCadastral(dto.getSituacaoCadastral());
        EnderecoDto e = dto.getEndereco();
        if (e != null) {
            entity.setLogradouro(e.getLogradouro());
            entity.setNumero(e.getNumero());
            entity.setComplemento(e.getComplemento());
            entity.setBairro(e.getBairro());
            entity.setMunicipio(e.getMunicipio());
            entity.setUf(e.getUf());
            entity.setCep(e.getCep());
        }
        CnaeDto p = dto.getCnaePrincipal();
        if (p != null) {
            entity.setCnaePrincipalCodigo(p.getCodigo());
            entity.setCnaePrincipalDescricao(p.getDescricao());
        }
        List<CnaeSecundarioEmbeddable> sec = new ArrayList<>();
        if (dto.getCnaesSecundarios() != null) {
            for (CnaeDto c : dto.getCnaesSecundarios()) {
                sec.add(new CnaeSecundarioEmbeddable(c.getCodigo(), c.getDescricao()));
            }
        }
        entity.setCnaesSecundarios(sec);
        entity.setSource(source);
        entity.setProtocol(protocol);
        repository.save(entity);
    }
}

