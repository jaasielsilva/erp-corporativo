package com.jaasielsilva.portalceo.provider.cnpj;

import com.fasterxml.jackson.databind.JsonNode;
import com.jaasielsilva.portalceo.dto.cnpj.CnaeDto;
import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;
import com.jaasielsilva.portalceo.dto.cnpj.EnderecoDto;
import com.jaasielsilva.portalceo.util.CnpjUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class BrasilApiCnpjProvider implements CnpjProvider {

    private final WebClient receitaWebClient;

    public BrasilApiCnpjProvider(@Qualifier("receitaWebClient") WebClient receitaWebClient) {
        this.receitaWebClient = receitaWebClient;
    }

    @Override
    public CnpjConsultaDto consultar(String cnpj) {
        String s = CnpjUtils.sanitize(cnpj);
        JsonNode root = receitaWebClient.get()
                .uri("/{cnpj}", s)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null) {
            throw new RuntimeException("Resposta vazia da API de CNPJ");
        }

        String razaoSocial = getText(root, "razao_social");
        String nomeFantasia = getText(root, "nome_fantasia");
        String situacao = getText(root, "descricao_situacao_cadastral");

        EnderecoDto endereco = new EnderecoDto(
                getText(root, "logradouro"),
                getText(root, "numero"),
                getText(root, "complemento"),
                getText(root, "bairro"),
                getText(root, "municipio"),
                getText(root, "uf"),
                getText(root, "cep")
        );

        CnaeDto principal = new CnaeDto(
                textOrNumber(root.get("cnae_fiscal")),
                getText(root, "cnae_fiscal_descricao")
        );

        List<CnaeDto> secundarios = new ArrayList<>();
        JsonNode arr = root.get("cnaes_secundarios");
        if (arr != null && arr.isArray()) {
            for (JsonNode n : arr) {
                secundarios.add(new CnaeDto(
                        textOrNumber(n.get("codigo")),
                        getText(n, "descricao")
                ));
            }
        }

        return new CnpjConsultaDto(
                razaoSocial,
                nomeFantasia,
                endereco,
                situacao,
                principal,
                secundarios
        );
    }

    private String getText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private String textOrNumber(JsonNode node) {
        if (node == null || node.isNull()) return null;
        return node.isNumber() ? String.valueOf(node.asLong()) : node.asText();
    }
}

