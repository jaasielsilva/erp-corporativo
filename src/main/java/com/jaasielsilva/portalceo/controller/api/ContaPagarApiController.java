package com.jaasielsilva.portalceo.controller.api;

import com.jaasielsilva.portalceo.dto.ContaPagarDto;
import com.jaasielsilva.portalceo.mapper.ContaPagarMapper;
import com.jaasielsilva.portalceo.model.ContaPagar;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.service.ContaPagarService;
import com.jaasielsilva.portalceo.service.FornecedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/financeiro/contas-pagar")
public class ContaPagarApiController {

    @Autowired
    private ContaPagarService contaPagarService;

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping
    public ResponseEntity<Page<ContaPagarDto>> listar(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ContaPagar> all = contaPagarService.listarTodas();
        int start = Math.min((int) pageable.getOffset(), all.size());
        int end = Math.min((start + pageable.getPageSize()), all.size());
        List<ContaPagarDto> dtos = all.subList(start, end).stream().map(ContaPagarMapper::toDto)
                .collect(Collectors.toList());
        Page<ContaPagarDto> pageRes = new PageImpl<>(dtos, pageable, all.size());
        return ResponseEntity.ok(pageRes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaPagarDto> buscar(@PathVariable Long id) {
        return contaPagarService.buscarPorId(id)
                .map(ContaPagarMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ContaPagarDto> criar(@RequestBody ContaPagarDto dto,
                                               @ModelAttribute("usuarioLogado") Usuario usuario) {
        ContaPagar c = new ContaPagar();
        ContaPagarMapper.updateEntityFromDto(dto, c);
        if (dto.fornecedorId != null) {
            com.jaasielsilva.portalceo.model.Fornecedor f = fornecedorService.findById(dto.fornecedorId);
            if (f != null)
                c.setFornecedor(f);
        }
        ContaPagar saved = contaPagarService.salvar(c, usuario);
        return ResponseEntity.ok(ContaPagarMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContaPagarDto> atualizar(@PathVariable Long id, @RequestBody ContaPagarDto dto,
                                                   @ModelAttribute("usuarioLogado") Usuario usuario) {
        return contaPagarService.buscarPorId(id).map(existing -> {
            ContaPagarMapper.updateEntityFromDto(dto, existing);
            if (dto.fornecedorId != null) {
                com.jaasielsilva.portalceo.model.Fornecedor f = fornecedorService.findById(dto.fornecedorId);
                if (f != null)
                    existing.setFornecedor(f);
            }
            ContaPagar saved = contaPagarService.salvar(existing, usuario);
            return ResponseEntity.ok(ContaPagarMapper.toDto(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @ModelAttribute("usuarioLogado") Usuario usuario) {
        contaPagarService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/aprovar")
    public ResponseEntity<ContaPagarDto> aprovar(@PathVariable Long id,
                                                 @ModelAttribute("usuarioLogado") Usuario usuario) {
        ContaPagar aprovado = contaPagarService.aprovar(id, usuario);
        return ResponseEntity.ok(ContaPagarMapper.toDto(aprovado));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<ContaPagarDto> pagar(@PathVariable Long id,
            @RequestParam("valorPago") java.math.BigDecimal valorPago,
            @RequestParam(value = "formaPagamento", required = false) String formaPagamento,
            @ModelAttribute("usuarioLogado") Usuario usuario) throws IOException {
        ContaPagar pago = contaPagarService.efetuarPagamento(id, valorPago, formaPagamento, usuario, null);
        return ResponseEntity.ok(ContaPagarMapper.toDto(pago));
    }

}
