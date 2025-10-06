package com.jaasielsilva.portalceo.mapper;

import com.jaasielsilva.portalceo.dto.VendaDTO;
import com.jaasielsilva.portalceo.dto.VendaItemDTO;
import com.jaasielsilva.portalceo.model.Venda;
import com.jaasielsilva.portalceo.model.VendaItem;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class VendaMapper {

    public VendaDTO toDTO(Venda venda) {
        if (venda == null) {
            return null;
        }

        VendaDTO dto = new VendaDTO();
        dto.setId(venda.getId());
        dto.setNumeroVenda(venda.getNumeroVenda());
        dto.setTotal(venda.getTotal());
        dto.setSubtotal(venda.getSubtotal());
        dto.setDesconto(venda.getDesconto());
        dto.setFormaPagamento(venda.getFormaPagamento());
        dto.setParcelas(venda.getParcelas());
        dto.setValorPago(venda.getValorPago());
        dto.setTroco(venda.getTroco());
        dto.setDataVenda(venda.getDataVenda());
        dto.setStatus(venda.getStatus());
        dto.setObservacoes(venda.getObservacoes());
        dto.setCupomFiscal(venda.getCupomFiscal());

        if (venda.getCliente() != null) {
            dto.setClienteId(venda.getCliente().getId());
            dto.setClienteNome(venda.getCliente().getNome());
        }

        if (venda.getUsuario() != null) {
            dto.setUsuarioId(venda.getUsuario().getId());
        }

        if (venda.getCaixa() != null) {
            dto.setCaixaId(venda.getCaixa().getId());
        }

        if (venda.getItens() != null) {
            dto.setItens(venda.getItens().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public VendaItemDTO toItemDTO(VendaItem item) {
        if (item == null) {
            return null;
        }

        VendaItemDTO dto = new VendaItemDTO();
        dto.setId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        dto.setSubtotal(item.getSubtotal());

        if (item.getProduto() != null) {
            dto.setProdutoId(item.getProduto().getId());
            dto.setProdutoNome(item.getProduto().getNome());
            dto.setProdutoEan(item.getProduto().getEan());
            dto.setProdutoPreco(item.getProduto().getPreco());
        }

        return dto;
    }

    public Venda toEntity(VendaDTO dto) {
        if (dto == null) {
            return null;
        }

        Venda venda = new Venda();
        venda.setId(dto.getId());
        venda.setNumeroVenda(dto.getNumeroVenda());
        venda.setTotal(dto.getTotal());
        venda.setSubtotal(dto.getSubtotal());
        venda.setDesconto(dto.getDesconto());
        venda.setFormaPagamento(dto.getFormaPagamento());
        venda.setParcelas(dto.getParcelas());
        venda.setValorPago(dto.getValorPago());
        venda.setTroco(dto.getTroco());
        venda.setDataVenda(dto.getDataVenda());
        venda.setStatus(dto.getStatus());
        venda.setObservacoes(dto.getObservacoes());
        venda.setCupomFiscal(dto.getCupomFiscal());

        if (dto.getItens() != null) {
            venda.getItens().addAll(dto.getItens().stream()
                    .map(this::toItemEntity)
                    .collect(Collectors.toList()));
        }

        return venda;
    }

    public VendaItem toItemEntity(VendaItemDTO dto) {
        if (dto == null) {
            return null;
        }

        VendaItem item = new VendaItem();
        item.setId(dto.getId());
        item.setQuantidade(dto.getQuantidade());
        item.setPrecoUnitario(dto.getPrecoUnitario());
        item.setSubtotal(dto.getSubtotal());

        return item;
    }
}