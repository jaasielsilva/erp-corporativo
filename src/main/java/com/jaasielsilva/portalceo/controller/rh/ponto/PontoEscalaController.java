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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    // Registrar ponto por matrícula (máximo 4 batidas por dia com intervalo mínimo de 1 hora)
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
            
            // Validar máximo de 4 batidas por dia
            int totalBatidas = contarBatidasDoDia(registro);
            if (totalBatidas >= 4) {
                throw new IllegalArgumentException("Limite máximo de 4 batidas por dia já atingido");
            }
            
            // Validar intervalo mínimo de 1 hora entre batidas
            LocalTime ultimaBatida = obterUltimaBatida(registro);
            if (ultimaBatida != null) {
                long minutosDecorridos = java.time.Duration.between(ultimaBatida, agora).toMinutes();
                if (minutosDecorridos < 60) {
                    long minutosRestantes = 60 - minutosDecorridos;
                    throw new IllegalArgumentException(
                        String.format("Intervalo mínimo de 1 hora não respeitado. Aguarde mais %d minutos", minutosRestantes)
                    );
                }
            }

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
            } else {
                throw new IllegalArgumentException("Limite máximo de 4 batidas por dia já atingido");
            }

            registroPontoRepository.save(registro);

            response.put("success", true);
            response.put("horarioFormatado", agora.format(DateTimeFormatter.ofPattern("HH:mm")));
            response.put("colaboradorNome", colaborador.getNome());
            response.put("proximaBatida", proximaBatida);
            response.put("message", "Batida registrada com sucesso!");
            response.put("totalBatidas", totalBatidas + 1);
            response.put("batidasRestantes", 4 - (totalBatidas + 1));
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

    // Buscar últimos registros formatados para exibição
    @GetMapping("/registros/ultimos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ultimosRegistros() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Buscar os últimos 10 registros de todos os colaboradores
            List<RegistroPonto> registros = registroPontoRepository
                    .findTop10ByOrderByDataCriacaoDesc();
            
            List<Map<String, Object>> registrosFormatados = new ArrayList<>();
            
            for (RegistroPonto registro : registros) {
                // Processar cada batida do registro
                adicionarBatida(registrosFormatados, registro, registro.getEntrada1(), "Entrada", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida1(), "Saída", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getEntrada2(), "Retorno", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida2(), "Saída", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getEntrada3(), "Entrada", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida3(), "Saída", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getEntrada4(), "Entrada", registro.getData());
                adicionarBatida(registrosFormatados, registro, registro.getSaida4(), "Saída", registro.getData());
            }
            
            // Ordenar por data/hora mais recente primeiro
            registrosFormatados.sort((a, b) -> {
                LocalDateTime dataA = (LocalDateTime) a.get("dataHora");
                LocalDateTime dataB = (LocalDateTime) b.get("dataHora");
                return dataB.compareTo(dataA);
            });
            
            // Limitar aos 10 mais recentes
            if (registrosFormatados.size() > 10) {
                registrosFormatados = registrosFormatados.subList(0, 10);
            }
            
            response.put("success", true);
            response.put("registros", registrosFormatados);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    private void adicionarBatida(List<Map<String, Object>> lista, RegistroPonto registro, 
                                LocalTime horario, String tipo, LocalDate data) {
        if (horario != null) {
            Map<String, Object> batida = new HashMap<>();
            batida.put("horarioFormatado", horario.format(DateTimeFormatter.ofPattern("HH:mm")));
            batida.put("tipo", tipo);
            batida.put("colaboradorNome", registro.getColaborador().getNome());
            batida.put("matricula", registro.getColaborador().getUsuario().getMatricula());
            batida.put("data", data);
            batida.put("dataHora", LocalDateTime.of(data, horario));
            
            // Determinar se é hoje, ontem ou outra data
            LocalDate hoje = LocalDate.now();
            String dataTexto;
            if (data.equals(hoje)) {
                dataTexto = "Hoje";
            } else if (data.equals(hoje.minusDays(1))) {
                dataTexto = "Ontem";
            } else {
                dataTexto = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            batida.put("dataFormatada", dataTexto);
            
            lista.add(batida);
        }
    }

    // Método auxiliar para contar quantas batidas já foram registradas no dia
    private int contarBatidasDoDia(RegistroPonto registro) {
        int count = 0;
        if (registro.getEntrada1() != null) count++;
        if (registro.getSaida1() != null) count++;
        if (registro.getEntrada2() != null) count++;
        if (registro.getSaida2() != null) count++;
        return count;
    }

    // Método auxiliar para obter o horário da última batida registrada
    private LocalTime obterUltimaBatida(RegistroPonto registro) {
        LocalTime ultimaBatida = null;
        
        if (registro.getEntrada1() != null) {
            ultimaBatida = registro.getEntrada1();
        }
        if (registro.getSaida1() != null) {
            ultimaBatida = registro.getSaida1();
        }
        if (registro.getEntrada2() != null) {
            ultimaBatida = registro.getEntrada2();
        }
        if (registro.getSaida2() != null) {
            ultimaBatida = registro.getSaida2();
        }
        
        return ultimaBatida;
    }

}
