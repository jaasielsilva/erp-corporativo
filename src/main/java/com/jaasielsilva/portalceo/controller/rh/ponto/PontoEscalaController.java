package com.jaasielsilva.portalceo.controller.rh.ponto;

import com.jaasielsilva.portalceo.model.Colaborador;
import com.jaasielsilva.portalceo.model.RegistroPonto;
import com.jaasielsilva.portalceo.model.Usuario;
import com.jaasielsilva.portalceo.repository.RegistroPontoRepository;
import com.jaasielsilva.portalceo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
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
    private com.jaasielsilva.portalceo.service.ColaboradorService colaboradorService;

    @GetMapping("/registros")
    public String registros(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/registros";
    }

    @GetMapping("/correcoes")
    public String correcoes(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/correcoes";
    }

    @GetMapping("/escalas")
    public String escalas(Model model) {
        model.addAttribute("colaboradores", colaboradorService.listarAtivos());
        return "rh/ponto-escalas/escalas";
    }

    // 🚀 Novo endpoint para validar matrícula + senha
    @PostMapping("/registros/validar-matricula")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarMatricula(
            @RequestParam String matricula) {

        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("Validando matrícula: " + matricula);

            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

            System.out.println("Usuário encontrado: " + usuario.getNome());

            response.put("success", true);
            response.put("valida", true);
            response.put("colaborador", usuario.getNome());
            response.put("proximoTipo", "Entrada"); // ajustar conforme lógica
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("valida", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registros/registrar-por-matricula")
    @ResponseBody
    public Map<String, Object> registrarPorMatricula(
            @RequestParam String matricula,
            @RequestParam String senha) {

        Map<String, Object> response = new HashMap<>();

        try {
            Usuario usuario = usuarioService.buscarPorMatricula(matricula)
                    .orElseThrow(() -> new IllegalArgumentException("Matrícula não encontrada"));

            if (!passwordEncoder.matches(senha, usuario.getSenha())) {
                throw new IllegalArgumentException("Senha inválida");
            }

            // ⚡ Busca o colaborador diretamente pela matrícula
            Colaborador colaborador = colaboradorService.buscarPorMatriculaUsuario(matricula)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Colaborador não encontrado para matrícula: " + matricula));

            RegistroPonto registro = new RegistroPonto();
            registro.setColaborador(colaborador);
            registro.setData(LocalDate.now());
            registro.setEntrada1(LocalTime.now());
            registro.setUsuarioCriacao(usuario);

            registroPontoRepository.save(registro);

            response.put("success", true);
            response.put("horario", registro.getEntrada1().toString());
            response.put("colaborador", colaborador.getNome());
            response.put("proximoTipo", "Entrada");
            response.put("message", "Ponto registrado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

}
