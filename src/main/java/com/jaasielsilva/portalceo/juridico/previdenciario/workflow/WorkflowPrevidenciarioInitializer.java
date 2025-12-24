package com.jaasielsilva.portalceo.juridico.previdenciario.workflow;

import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflow;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.EtapaWorkflowCodigo;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.entity.TransicaoWorkflow;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.repository.EtapaWorkflowRepository;
import com.jaasielsilva.portalceo.juridico.previdenciario.workflow.repository.TransicaoWorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowPrevidenciarioInitializer implements CommandLineRunner {

        private final EtapaWorkflowRepository etapaRepository;
        private final TransicaoWorkflowRepository transicaoRepository;

        @Override
        public void run(String... args) {
                EnumMap<EtapaWorkflowCodigo, EtapaWorkflow> etapas = new EnumMap<>(EtapaWorkflowCodigo.class);

                etapas.put(EtapaWorkflowCodigo.CADASTRO, obterOuCriarEtapa(EtapaWorkflowCodigo.CADASTRO, 1, false));
                etapas.put(EtapaWorkflowCodigo.DOCUMENTACAO,
                                obterOuCriarEtapa(EtapaWorkflowCodigo.DOCUMENTACAO, 2, true));
                etapas.put(EtapaWorkflowCodigo.ANALISE, obterOuCriarEtapa(EtapaWorkflowCodigo.ANALISE, 3, true));
                etapas.put(EtapaWorkflowCodigo.PROTOCOLO_INSS,
                                obterOuCriarEtapa(EtapaWorkflowCodigo.PROTOCOLO_INSS, 4, true));
                etapas.put(EtapaWorkflowCodigo.ACOMPANHAMENTO,
                                obterOuCriarEtapa(EtapaWorkflowCodigo.ACOMPANHAMENTO, 5, true));
                etapas.put(EtapaWorkflowCodigo.DECISAO, obterOuCriarEtapa(EtapaWorkflowCodigo.DECISAO, 6, true));
                etapas.put(EtapaWorkflowCodigo.RECURSO, obterOuCriarEtapa(EtapaWorkflowCodigo.RECURSO, 7, true));
                etapas.put(EtapaWorkflowCodigo.FINALIZADO, obterOuCriarEtapa(EtapaWorkflowCodigo.FINALIZADO, 8, false));

                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.CADASTRO),
                                etapas.get(EtapaWorkflowCodigo.DOCUMENTACAO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.DOCUMENTACAO),
                                etapas.get(EtapaWorkflowCodigo.ANALISE));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.ANALISE),
                                etapas.get(EtapaWorkflowCodigo.PROTOCOLO_INSS));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.PROTOCOLO_INSS),
                                etapas.get(EtapaWorkflowCodigo.ACOMPANHAMENTO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.ACOMPANHAMENTO),
                                etapas.get(EtapaWorkflowCodigo.DECISAO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.DECISAO), etapas.get(EtapaWorkflowCodigo.RECURSO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.DECISAO), etapas.get(EtapaWorkflowCodigo.FINALIZADO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.RECURSO), etapas.get(EtapaWorkflowCodigo.FINALIZADO));

                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.DOCUMENTACAO),
                                etapas.get(EtapaWorkflowCodigo.CADASTRO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.ANALISE),
                                etapas.get(EtapaWorkflowCodigo.DOCUMENTACAO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.PROTOCOLO_INSS),
                                etapas.get(EtapaWorkflowCodigo.ANALISE));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.ACOMPANHAMENTO),
                                etapas.get(EtapaWorkflowCodigo.PROTOCOLO_INSS));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.DECISAO),
                                etapas.get(EtapaWorkflowCodigo.ACOMPANHAMENTO));
                garantirTransicoes(etapas.get(EtapaWorkflowCodigo.RECURSO), etapas.get(EtapaWorkflowCodigo.DECISAO));
        }

        private EtapaWorkflow obterOuCriarEtapa(EtapaWorkflowCodigo codigo, int ordem, boolean permiteAnexo) {
                return etapaRepository.findByCodigo(codigo)
                                .orElseGet(() -> etapaRepository
                                                .save(new EtapaWorkflow(null, codigo, ordem, permiteAnexo)));
        }

        private void garantirTransicoes(EtapaWorkflow origem, EtapaWorkflow destino) {
                if (origem == null || destino == null) {
                        return;
                }
                List<String> roles = List.of("JURIDICO", "GERENCIAL", "ADMINISTRADOR", "ADMIN", "MASTER");
                List<TransicaoWorkflow> existentes = transicaoRepository
                                .findByEtapaOrigem_CodigoAndEtapaDestino_Codigo(origem.getCodigo(),
                                                destino.getCodigo());
                for (String role : roles) {
                        boolean jaExiste = existentes.stream()
                                        .anyMatch(t -> role.equalsIgnoreCase(t.getRolePermitida()));
                        if (!jaExiste) {
                                transicaoRepository.save(new TransicaoWorkflow(null, origem, destino, role));
                        }
                }
        }
}
