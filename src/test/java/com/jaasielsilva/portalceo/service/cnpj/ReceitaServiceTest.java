package com.jaasielsilva.portalceo.service.cnpj;

import com.jaasielsilva.portalceo.dto.cnpj.CnaeDto;
import com.jaasielsilva.portalceo.dto.cnpj.CnpjConsultaDto;
import com.jaasielsilva.portalceo.dto.cnpj.EnderecoDto;
import com.jaasielsilva.portalceo.provider.cnpj.CnpjProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ReceitaServiceTest {

    @Autowired
    private ReceitaService receitaService;

    @MockBean
    private CnpjProvider cnpjProvider;

    @Test
    void cacheableConsult_shouldCacheByCnpj() {
        CnpjConsultaDto dto = new CnpjConsultaDto("Teste SA", "Teste", new EnderecoDto(), "ATIVA", new CnaeDto("1234", "Desc"), null);
        Mockito.when(cnpjProvider.consultar(anyString())).thenReturn(dto);

        String cnpj = "12345678000195";
        receitaService.consultarCnpj(cnpj);
        receitaService.consultarCnpj(cnpj);

        verify(cnpjProvider, times(1)).consultar("12345678000195");
    }
}

