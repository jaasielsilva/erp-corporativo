package com.jaasielsilva.portalceo.mapper;

import com.jaasielsilva.portalceo.dto.ContratoLegalDTO;
import com.jaasielsilva.portalceo.model.ContratoLegal;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContratoLegalMapper {

    public static ContratoLegalDTO toDto(ContratoLegal contrato) {
        if (contrato == null) return null;
        ContratoLegalDTO dto = new ContratoLegalDTO();
        dto.setId(contrato.getId());
        dto.setNumeroContrato(contrato.getNumeroContrato());
        dto.setTitulo(contrato.getTitulo());
        dto.setDescricao(contrato.getDescricao());
        dto.setTipo(contrato.getTipo());
        dto.setStatus(contrato.getStatus());
        dto.setDataInicio(contrato.getDataInicio());
        dto.setDataFim(contrato.getDataFim());
        dto.setDataVencimento(contrato.getDataVencimento());
        dto.setValorMensal(contrato.getValorMensal());
        dto.setValorContrato(contrato.getValorContrato());
        dto.setContraparte(contrato.getNomeContraparte());
        dto.setUsuarioResponsavel(contrato.getUsuarioResponsavel() != null ? contrato.getUsuarioResponsavel().getNome() : null);
        return dto;
    }

    public static List<ContratoLegalDTO> toDtoList(List<ContratoLegal> contratos) {
        return contratos == null ? List.of() : contratos.stream()
                .filter(Objects::nonNull)
                .map(ContratoLegalMapper::toDto)
                .collect(Collectors.toList());
    }
}