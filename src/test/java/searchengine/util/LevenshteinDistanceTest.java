package searchengine.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LevenshteinDistanceTest {

    @Test
    void sameStringsHaveZeroDistance() {
        assertEquals(0, LevenshteinDistance.calculate("path", "path"));
    }

    @Test
    void missingCharacterHasDistanceOne() {
        assertEquals(1, LevenshteinDistance.calculate("pth", "path"));
    }

    @Test
    void substitutionHasDistanceOne() {
        assertEquals(1, LevenshteinDistance.calculate("java", "jova"));
    }

    @Test
    void handlesDifferentQueryWords() {
        assertEquals(1, LevenshteinDistance.calculate("content", "cntent"));
    }
}