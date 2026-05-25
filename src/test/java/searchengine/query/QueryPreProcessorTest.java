package searchengine.query;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class QueryPreProcessorTest {

    @Test
    void testBaseQueryBuilder() {
        QueryBuilder builder = new BaseQueryBuilder();
        assertEquals("test", builder.buildQuery("  test  "));
        assertEquals("", builder.buildQuery(null));
    }

    @Test
    void testSanitizationDecorator() {
        QueryBuilder builder = new SanitizationDecorator(new BaseQueryBuilder());
        assertEquals("test query", builder.buildQuery("test+ ~query*"));
        assertEquals("content:java color:red", builder.buildQuery("content:java @color:red"));
    }

    @Test
    void testSynonymDecorator() {
        QueryBuilder builder = new SynonymDecorator(new BaseQueryBuilder());
        assertEquals("img OR image OR photo", builder.buildQuery("img"));
        assertEquals("content:img OR content:image OR content:photo", builder.buildQuery("content:img"));
        assertEquals("test", builder.buildQuery("test"));
    }

    @Test
    void testLogicDecorator() {
        QueryBuilder builder = new LogicDecorator(new BaseQueryBuilder());
        assertEquals("search*", builder.buildQuery("search"));
        assertEquals("search*", builder.buildQuery("search*"));
        assertEquals("search* OR find*", builder.buildQuery("search OR find"));
    }

    @Test
    void testChainedDecorators() {
        QueryBuilder builder = new LogicDecorator(
                new SynonymDecorator(
                        new SanitizationDecorator(
                                new BaseQueryBuilder()
                        )
                )
        );
        String result = builder.buildQuery("img+");
        assertTrue(result.contains("img*"));
        assertTrue(result.contains("image*"));
        assertTrue(result.contains("photo*"));
    }

    @Test
    void testQueryParserGrouping() {
        QueryParser parser = new QueryParser();
        QueryParser.ParsedQuery query = parser.parse("img OR image OR photo");
        assertEquals(1, query.getGeneralTerms().size());
        assertEquals("img|image|photo", query.getGeneralTerms().get(0));

        String fts = query.toFullTextQuery();
        assertEquals("+(img* image* photo*)", fts);
    }
}
