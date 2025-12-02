package com.jaasielsilva.portalceo.controller.cadastros;

import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;
import com.jaasielsilva.portalceo.service.cnpj.ReceitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/cadastros")
public class ConsultaCnpjController {

    @Autowired
    private ReceitaService receitaService;

    @GetMapping("/consultar-cnpj")
    public String consultarCnpjPage(Model model) {
        return "cadastros/consultar-cnpj";
    }

    @GetMapping("/consultar")
    public ResponseEntity<CnpjConsultaDto> consultar(@RequestParam("cnpj") String cnpj) {
        CnpjConsultaDto dto = receitaService.consultarCnpj(cnpj);
        return ResponseEntity.ok(dto);
    }
}

