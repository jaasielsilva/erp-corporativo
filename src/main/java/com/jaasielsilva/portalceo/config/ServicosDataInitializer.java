package com.jaasielsilva.portalceo.config;

import com.jaasielsilva.portalceo.model.servicos.*;
import com.jaasielsilva.portalceo.repository.servicos.AprovacaoSolicitacaoRepository;
import com.jaasielsilva.portalceo.repository.servicos.ServicoRepository;
import com.jaasielsilva.portalceo.repository.servicos.SolicitacaoServicoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Profile({"dev","default"})
public class ServicosDataInitializer implements CommandLineRunner {

    private final ServicoRepository servicoRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final AprovacaoSolicitacaoRepository aprovacaoRepository;

    public ServicosDataInitializer(ServicoRepository servicoRepository,
                                   SolicitacaoServicoRepository solicitacaoRepository,
                                   AprovacaoSolicitacaoRepository aprovacaoRepository) {
        this.servicoRepository = servicoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.aprovacaoRepository = aprovacaoRepository;
    }

    @Override
    public void run(String... args) {
        if (servicoRepository.count() == 0) {
            Servico a = new Servico();
            a.setNome("Serviço A");
            a.setDescricaoBreve("Descrição breve do serviço A.");
            a.setCategoria("Infra");
            a.setSlaRespostaHoras(4);
            a.setSlaSolucaoHoras(24);
            a.setCustoBase(new BigDecimal("150.00"));
            a.setExigeAprovacao(true);

            Servico b = new Servico();
            b.setNome("Serviço B");
            b.setDescricaoBreve("Descrição breve do serviço B.");
            b.setCategoria("Aplicações");
            b.setSlaRespostaHoras(8);
            b.setSlaSolucaoHoras(48);
            b.setCustoBase(new BigDecimal("250.00"));
            b.setExigeAprovacao(false);

            servicoRepository.save(a);
            servicoRepository.save(b);

            SolicitacaoServico s1 = new SolicitacaoServico();
            s1.setServico(a);
            s1.setTitulo("Acesso VPN corporativa");
            s1.setDescricao("Necessário acesso VPN para trabalho remoto.");
            s1.setPrioridade(Prioridade.MEDIA);
            s1.setStatus(StatusSolicitacao.CRIADA);
            s1.setCriadoEm(LocalDateTime.now().minusDays(2));
            s1.setAtualizadoEm(LocalDateTime.now().minusDays(1));
            s1.setSolicitanteNome("Colaborador Demo");
            s1.setExecutorNome(null);
            solicitacaoRepository.save(s1);

            SolicitacaoServico s2 = new SolicitacaoServico();
            s2.setServico(b);
            s2.setTitulo("Atualização de sistema");
            s2.setDescricao("Atualizar sistema de vendas para versão mais recente.");
            s2.setPrioridade(Prioridade.ALTA);
            s2.setStatus(StatusSolicitacao.EM_APROVACAO);
            s2.setCriadoEm(LocalDateTime.now().minusDays(1));
            s2.setAtualizadoEm(LocalDateTime.now());
            s2.setSolicitanteNome("Colaborador Demo");
            s2.setExecutorNome(null);
            s2 = solicitacaoRepository.save(s2);

            AprovacaoSolicitacao ap = new AprovacaoSolicitacao();
            ap.setSolicitacao(s2);
            ap.setStatus(AprovacaoSolicitacao.StatusAprovacao.EM_APROVACAO);
            ap.setJustificativa("Impacto em produção, revisar janela.");
            ap.setGestorNome("Gestor Demo");
            ap.setCriadoEm(LocalDateTime.now());
            ap.setAtualizadoEm(LocalDateTime.now());
            aprovacaoRepository.save(ap);

            SolicitacaoServico s3 = new SolicitacaoServico();
            s3.setServico(a);
            s3.setTitulo("Troca de notebook");
            s3.setDescricao("Equipamento com falhas constantes.");
            s3.setPrioridade(Prioridade.MEDIA);
            s3.setStatus(StatusSolicitacao.CONCLUIDA);
            s3.setCriadoEm(LocalDateTime.now().minusDays(5));
            s3.setAtualizadoEm(LocalDateTime.now().minusDays(3));
            s3.setSolicitanteNome("Colaborador Demo");
            s3.setExecutorNome("Atendente Demo");
            solicitacaoRepository.save(s3);
        }
    }
}