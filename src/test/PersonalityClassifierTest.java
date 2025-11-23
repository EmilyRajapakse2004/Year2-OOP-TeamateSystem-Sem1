package test;

import org.junit.jupiter.api.Test;
import teamate.PersonalityClassifier;

import static org.junit.jupiter.api.Assertions.*;

class PersonalityClassifierTest {

    @Test
    void testCalculateScore_Valid() {
        int score = PersonalityClassifier.calculateScore(5, 4, 4, 5, 5);
        assertEquals(92, score); // (5+4+4+5+5)=23*4=92
    }

    @Test
    void testCalculateScore_InvalidAnswer() {
        int score = PersonalityClassifier.calculateScore(5, 0, 4, 5, 5);
        assertEquals(-1, score);
    }

    @Test
    void testClassifyLeader() {
        assertEquals("Leader", PersonalityClassifier.classify(95));
    }

    @Test
    void testClassifyBalanced() {
        assertEquals("Balanced", PersonalityClassifier.classify(75));
    }

    @Test
    void testClassifyThinker() {
        assertEquals("Thinker", PersonalityClassifier.classify(60));
    }

    @Test
    void testClassifyUndefined() {
        assertEquals("Undefined", PersonalityClassifier.classify(10));
    }
}
