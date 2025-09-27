package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class MovimentacaoEstoqueDTO {

    private String dataFormatada;
    private String produtoNome;
    private String tipo;
    private Integer quantidade;
    private String motivo;
    private String usuarioNome;

    public static MovimentacaoEstoqueDTO fromEntity(MovimentacaoEstoque entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return new MovimentacaoEstoqueDTO(
                entity.getDataHora() != null ? entity.getDataHora().format(formatter) : "-",
                entity.getProduto() != null ? entity.getProduto().getNome() : "-",
                entity.getTipo() != null ? entity.getTipo().toString() : "-",
                entity.getQuantidade(),
                entity.getMotivo(),
                entity.getUsuarioResponsavel() != null ? entity.getUsuarioResponsavel() : "-"
        );
    }
}
