package searchengine.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryParserTest {

    @Test
    void parsesPathAndContentTerms() {
        QueryParser parser = new QueryParser();

        QueryParser.ParsedQuery query = parser.parse("path:src content:java");

        assertTrue(query.hasQualifiedTerms());
        assertEquals(1, query.getPathTerms().size());
        assertEquals("src", query.getPathTerms().get(0));
        assertEquals(1, query.getContentTerms().size());
        assertEquals("java", query.getContentTerms().get(0));
    }

    @Test
    void supportsAnyQualifierOrder() {
        QueryParser parser = new QueryParser();

        QueryParser.ParsedQuery query = parser.parse("content:parser path:main");

        assertEquals("parser", query.getContentTerms().get(0));
        assertEquals("main", query.getPathTerms().get(0));
    }

    @Test
    void keepsDuplicateQualifiersAsSeparateAndTerms() {
        QueryParser parser = new QueryParser();

        QueryParser.ParsedQuery query = parser.parse("path:src path:main");

        assertEquals(2, query.getPathTerms().size());
        assertTrue(query.getPathTerms().contains("src"));
        assertTrue(query.getPathTerms().contains("main"));
    }

    @Test
    void parsesGeneralTerms() {
        QueryParser parser = new QueryParser();

        QueryParser.ParsedQuery query = parser.parse("database engine");

        assertFalse(query.hasQualifiedTerms());
        assertEquals(2, query.getGeneralTerms().size());
        assertTrue(query.getGeneralTerms().contains("database"));
        assertTrue(query.getGeneralTerms().contains("engine"));
    }
}