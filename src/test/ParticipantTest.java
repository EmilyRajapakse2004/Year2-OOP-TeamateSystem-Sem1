package test;

import org.junit.jupiter.api.Test;
import teamate.Participant;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @Test
    void testToCSVRow() {
        Participant p = new Participant("P1", "John", "john@mail.com",
                "Valorant", 8, "Strategist", 80, "Balanced");

        String row = p.toCSVRow("1");

        assertTrue(row.contains("1"));
        assertTrue(row.contains("P1"));
        assertTrue(row.contains("John"));
    }

    @Test
    void testToStringNotNull() {
        Participant p = new Participant("P1", "A", "a@mail.com",
                "FIFA", 6, "Supporter", 60, "Thinker");

        assertNotNull(p.toString());
    }
}
