package com.jaasielsilva.portalceo.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class HoleriteCalculoService {

    private static final BigDecimal INSS_TETO_2024 = BigDecimal.valueOf(1090.04);
    private static final BigDecimal DEPENDENTE_DEDUCAO_IRRF = BigDecimal.valueOf(189.59);

    public BigDecimal calcularInssProgressivo(BigDecimal base) {
        BigDecimal resultado = BigDecimal.ZERO;
        BigDecimal restante = base;

        BigDecimal faixa1Lim = BigDecimal.valueOf(1412.00);
        BigDecimal faixa2Lim = BigDecimal.valueOf(2666.68);
        BigDecimal faixa3Lim = BigDecimal.valueOf(4000.03);
        BigDecimal faixa4Lim = BigDecimal.valueOf(7786.02);

        BigDecimal faixa1Aliq = BigDecimal.valueOf(0.075);
        BigDecimal faixa2Aliq = BigDecimal.valueOf(0.09);
        BigDecimal faixa3Aliq = BigDecimal.valueOf(0.12);
        BigDecimal faixa4Aliq = BigDecimal.valueOf(0.14);

        BigDecimal acumulado = BigDecimal.ZERO;

        BigDecimal faixa1Base = restante.min(faixa1Lim);
        resultado = resultado.add(faixa1Base.multiply(faixa1Aliq));
        restante = restante.subtract(faixa1Base);
        acumulado = acumulado.add(faixa1Base);

        if (restante.signum() > 0) {
            BigDecimal faixa2Base = restante.min(faixa2Lim.subtract(faixa1Lim));
            resultado = resultado.add(faixa2Base.multiply(faixa2Aliq));
            restante = restante.subtract(faixa2Base);
            acumulado = acumulado.add(faixa2Base);
        }
        if (restante.signum() > 0) {
            BigDecimal faixa3Base = restante.min(faixa3Lim.subtract(faixa2Lim));
            resultado = resultado.add(faixa3Base.multiply(faixa3Aliq));
            restante = restante.subtract(faixa3Base);
            acumulado = acumulado.add(faixa3Base);
        }
        if (restante.signum() > 0) {
            BigDecimal faixa4Base = restante.min(faixa4Lim.subtract(faixa3Lim));
            resultado = resultado.add(faixa4Base.multiply(faixa4Aliq));
        }

        resultado = resultado.setScale(2, RoundingMode.HALF_UP);
        if (resultado.compareTo(INSS_TETO_2024) > 0) {
            return INSS_TETO_2024;
        }
        return resultado;
    }

    public BigDecimal calcularIrrf(BigDecimal base, int dependentes) {
        BigDecimal deducaoDependentes = DEPENDENTE_DEDUCAO_IRRF.multiply(BigDecimal.valueOf(Math.max(dependentes, 0)));
        BigDecimal baseCalc = base.subtract(deducaoDependentes);
        if (baseCalc.compareTo(BigDecimal.ZERO) < 0) baseCalc = BigDecimal.ZERO;

        if (baseCalc.compareTo(BigDecimal.valueOf(2112.00)) <= 0) {
            return BigDecimal.ZERO;
        } else if (baseCalc.compareTo(BigDecimal.valueOf(2826.65)) <= 0) {
            return baseCalc.multiply(BigDecimal.valueOf(0.075)).subtract(BigDecimal.valueOf(158.40))
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (baseCalc.compareTo(BigDecimal.valueOf(3751.05)) <= 0) {
            return baseCalc.multiply(BigDecimal.valueOf(0.15)).subtract(BigDecimal.valueOf(370.40))
                    .setScale(2, RoundingMode.HALF_UP);
        } else if (baseCalc.compareTo(BigDecimal.valueOf(4664.68)) <= 0) {
            return baseCalc.multiply(BigDecimal.valueOf(0.225)).subtract(BigDecimal.valueOf(651.73))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return baseCalc.multiply(BigDecimal.valueOf(0.275)).subtract(BigDecimal.valueOf(884.96))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal calcularFgtsPatronal(BigDecimal base) {
        return base.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal limitarDescontoValeTransporte(BigDecimal salarioBase, BigDecimal descontoAtual) {
        BigDecimal limite = salarioBase.multiply(BigDecimal.valueOf(0.06)).setScale(2, RoundingMode.HALF_UP);
        if (descontoAtual == null) return BigDecimal.ZERO;
        return descontoAtual.min(limite);
    }
}