package com.jaasielsilva.portalceo.controller.rh.ponto;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.RegistroPontoRepository;
import com.jaasielsilva.portalceo.service.ColaboradorService;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rh/ponto-escalas")
public class PontoEscalaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ColaboradorService colaboradorService;

    @GetMapping("/registros")
    public String registros(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/registros";
    }

    // Validar matrícula
    @PostMapping("/registros/validar-matricula")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarMatricula(@RequestParam String matricula) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

            Colaborador colaborador = usuario.getColaborador();
            if (colaborador == null || colaborador.getStatus() != Colaborador.StatusColaborador.ATIVO) {
                throw new IllegalArgumentException("Colaborador não encontrado ou inativo");
            }

            response.put("success", true);
            response.put("valida", true);
            response.put("colaborador", colaborador.getNome());
        } catch (Exception e) {
            response.put("success", false);
            response.put("valida", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // Registrar ponto por matrícula (com 4 batidas automáticas)
    @PostMapping("/registros/registrar-por-matricula")
    @ResponseBody
    public Map<String, Object> registrarPorMatricula(@RequestParam String matricula, @RequestParam String senha) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

            if (!passwordEncoder.matches(senha, usuario.getSenha())) {
                throw new IllegalArgumentException("Senha inválida");
            }

            Colaborador colaborador = usuario.getColaborador();
            if (colaborador == null) {
                throw new IllegalArgumentException("Colaborador não encontrado");
            }

            RegistroPonto registro = registroPontoRepository
                    .findByColaboradorAndData(colaborador, LocalDate.now())
                    .orElse(new RegistroPonto());

            registro.setColaborador(colaborador);
            registro.setData(LocalDate.now());
            registro.setUsuarioCriacao(usuario);
            registro.setTipoRegistro(RegistroPonto.TipoRegistro.AUTOMATICO);

            LocalTime agora = LocalTime.now();
            String proximaBatida;

            if (registro.getEntrada1() == null) {
                registro.setEntrada1(agora);
                proximaBatida = "Entrada 1";
            } else if (registro.getSaida1() == null) {
                registro.setSaida1(agora);
                proximaBatida = "Saída 1";
            } else if (registro.getEntrada2() == null) {
                registro.setEntrada2(agora);
                proximaBatida = "Entrada 2";
            } else if (registro.getSaida2() == null) {
                registro.setSaida2(agora);
                proximaBatida = "Saída 2";
            } else if (registro.getEntrada3() == null) {
                registro.setEntrada3(agora);
                proximaBatida = "Entrada 3";
            } else if (registro.getSaida3() == null) {
                registro.setSaida3(agora);
                proximaBatida = "Saída 3";
            } else if (registro.getEntrada4() == null) {
                registro.setEntrada4(agora);
                proximaBatida = "Entrada 4";
            } else if (registro.getSaida4() == null) {
                registro.setSaida4(agora);
                proximaBatida = "Saída 4";
            } else {
                throw new IllegalArgumentException("Todas as batidas já registradas para hoje");
            }

            registroPontoRepository.save(registro);

            response.put("success", true);
            response.put("horario", agora.toString());
            response.put("colaborador", colaborador.getNome());
            response.put("proximaBatida", proximaBatida);
            response.put("message", "Batida registrada com sucesso!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Buscar histórico de registros do dia
    @GetMapping("/registros/hoje")
    @ResponseBody
    public ResponseEntity<List<RegistroPonto>> registrosHoje(@RequestParam String matricula) {
        Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));
        Colaborador colaborador = usuario.getColaborador();
        if (colaborador == null) {
            throw new IllegalArgumentException("Colaborador não encontrado");
        }
        List<RegistroPonto> registros = registroPontoRepository
                .findByColaboradorAndDataBetweenOrderByDataDesc(colaborador, LocalDate.now(), LocalDate.now());
        return ResponseEntity.ok(registros);
    }

}
