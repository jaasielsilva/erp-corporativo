package com.jaasielsilva.portalceo.dto;

import com.jaasielsilva.portalceo.model.MovimentacaoEstoque;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class MovimentacaoEstoqueDTO {

    private String dataHora;          // formatada como "dd/MM/yyyy HH:mm"
    private String produto;           // nome do produto
    private String tipo;              // ENTRADA, SAIDA, AJUSTE
    private Integer quantidade;
    private String motivo;
    private String usuario;

    public static MovimentacaoEstoqueDTO fromEntity(MovimentacaoEstoque entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return new MovimentacaoEstoqueDTO(
                entity.getDataHora().format(formatter),
                entity.getProduto().getNome(),              // supondo que Produto tem getNome()
                entity.getTipo().toString(),
                entity.getQuantidade(),
                entity.getMotivo(),
                entity.getUsuarioResponsavel()
        );
    }
}
