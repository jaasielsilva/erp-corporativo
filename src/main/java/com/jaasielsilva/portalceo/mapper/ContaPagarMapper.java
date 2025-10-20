package com.jaasielsilva.portalceo.mapper;

import com.jaasielsilva.portalceo.dto.ContaPagarDto;
import com.jaasielsilva.portalceo.model.ContaPagar;

public class ContaPagarMapper {

    public static ContaPagarDto toDto(ContaPagar c) {
        if (c == null) return null;
        ContaPagarDto dto = new ContaPagarDto();
        dto.id = c.getId();
        dto.descricao = c.getDescricao();
        dto.fornecedorId = c.getFornecedor() != null ? c.getFornecedor().getId() : null;
        dto.valorOriginal = c.getValorOriginal();
        dto.dataVencimento = c.getDataVencimento();
        dto.dataEmissao = c.getDataEmissao();
        dto.status = c.getStatus() != null ? c.getStatus().name() : null;
        dto.categoria = c.getCategoria() != null ? c.getCategoria().name() : null;
        dto.numeroDocumento = c.getNumeroDocumento();
        dto.observacoes = c.getObservacoes();
        return dto;
    }

    public static void updateEntityFromDto(ContaPagarDto dto, ContaPagar entity) {
        if (dto == null || entity == null) return;
        entity.setDescricao(dto.descricao);
        entity.setValorOriginal(dto.valorOriginal);
        entity.setDataVencimento(dto.dataVencimento);
        entity.setDataEmissao(dto.dataEmissao);
        entity.setNumeroDocumento(dto.numeroDocumento);
        entity.setObservacoes(dto.observacoes);
        // Note: status/categoria/fornecedor mapping handled in controller/service as needed
    }
}
