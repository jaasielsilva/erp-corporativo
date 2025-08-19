package com.jaasielsilva.portalceo.formatter;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class BigDecimalFormatter implements Formatter<BigDecimal> {

    @Override
    public BigDecimal parse(String text, Locale locale) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        // Substitui vírgula por ponto
        return new BigDecimal(text.replace(",", "."));
    }

    @Override
    public String print(BigDecimal object, Locale locale) {
        return object.toString();
    }
}
