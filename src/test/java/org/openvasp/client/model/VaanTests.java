package org.openvasp.client.model;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VaanTests {

    @Test
    public void checkVaanDisplay() {
        Vaan vaan = new Vaan("1000BB528777E33B0785209E");
        Pattern pattern = Pattern.compile("^[a-f0-9]{4} [a-f0-9]{4} [a-f0-9]{4} [a-f0-9]{4} [a-f0-9]{4} [a-f0-9]{4}$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(vaan.toString());
        assertTrue(matcher.matches());
    }

    @Test
    public void checkVaanStorage() {
        Vaan vaan = new Vaan("1000 Bb528777 E33B078520 9E");
        Pattern pattern = Pattern.compile("^[a-f0-9]{24}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(vaan.getData());
        assertTrue(matcher.matches());
    }

    @Test
    public void checkVaanReceptionFormat() {
        Vaan vaan = new Vaan("1000bb528777e33b0785209e");
        assertFormat(vaan);
        vaan = new Vaan("1000 bb52 8777 e33b 0785 209e");
        assertFormat(vaan);
        vaan = new Vaan("1 0 0 0 b b 5 2 8 7 7 7 e 3 3 b 0 7 8 5 2 0 9 e");
        assertFormat(vaan);
        vaan = new Vaan("1000 BB528777 E33B078520 9E");
        assertFormat(vaan);
        vaan = new Vaan("1000BB52 8777e33b0785209e");
        assertFormat(vaan);
    }

    private void assertFormat(final Vaan vaan) {
        assertThat(vaan.getVaspCodeType()).isEqualTo(new VaspCodeType("10"));
        assertThat(vaan.getVaspCode()).isEqualTo(new VaspCode("bb528777"));
        assertThat(vaan.getCustomerNr()).isEqualTo("e33b078520");
        assertThat(vaan.getCheckSum()).isEqualTo("9e");
    }
}
