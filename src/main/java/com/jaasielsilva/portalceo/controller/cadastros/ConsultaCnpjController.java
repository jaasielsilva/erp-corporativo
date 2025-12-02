package com.jaasielsilva.portalceo.controller.cadastros;

import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;
import com.jaasielsilva.portalceo.service.cnpj.ReceitaService;
import com.jaasielsilva.portalceo.repository.cnpj.CnpjConsultaRepository;
import com.jaasielsilva.portalceo.model.cnpj.CnpjConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/cadastros")
public class ConsultaCnpjController {

    @Autowired
    private ReceitaService receitaService;

    @Autowired
    private CnpjConsultaRepository cnpjConsultaRepository;

    @GetMapping("/consultar-cnpj")
    public String consultarCnpjPage(Model model) {
        return "cadastros/consultar-cnpj";
    }

    @GetMapping("/consultar")
    public ResponseEntity<CnpjConsultaDto> consultar(@RequestParam("cnpj") String cnpj) {
        CnpjConsultaDto dto = receitaService.consultarCnpj(cnpj);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/historico-cnpj")
    public String historicoCnpjPage(@RequestParam(name = "cnpj", required = false) String cnpj, Model model) {
        model.addAttribute("cnpj", cnpj != null ? cnpj : "");
        return "cadastros/historico-cnpj";
    }

    @GetMapping("/historico")
    public ResponseEntity<List<CnpjConsulta>> historico(@RequestParam("cnpj") String cnpj,
                                                        @RequestParam(name = "from", required = false) String from,
                                                        @RequestParam(name = "to", required = false) String to) {
        String s = cnpj.replaceAll("[^0-9]", "");
        List<CnpjConsulta> list;
        if (from != null && to != null && !from.isBlank() && !to.isBlank()) {
            LocalDate d1 = LocalDate.parse(from);
            LocalDate d2 = LocalDate.parse(to);
            LocalDateTime start = d1.atStartOfDay();
            LocalDateTime end = d2.atTime(LocalTime.MAX);
            list = cnpjConsultaRepository.findByCnpjAndConsultedAtBetweenOrderByConsultedAtDesc(s, start, end);
        } else {
            list = cnpjConsultaRepository.findByCnpjOrderByConsultedAtDesc(s);
        }
        return ResponseEntity.ok(list);
    }
}
