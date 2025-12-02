package com.jaasielsilva.portalceo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CnpjUtilsTest {

    @Test
    void sanitizeRemovesMask() {
        Assertions.assertEquals("12345678000195", CnpjUtils.sanitize("12.345.678/0001-95"));
    }

    @Test
    void isValidLengthChecks14Digits() {
        Assertions.assertTrue(CnpjUtils.isValidLength("12.345.678/0001-95"));
        Assertions.assertFalse(CnpjUtils.isValidLength("00000000000000"));
        Assertions.assertFalse(CnpjUtils.isValidLength("123"));
    }
}

