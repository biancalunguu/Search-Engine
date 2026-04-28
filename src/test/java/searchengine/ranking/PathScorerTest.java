package searchengine.ranking;

import org.junit.jupiter.api.Test;
import searchengine.model.FileRecord;

import static org.junit.jupiter.api.Assertions.*;

public class PathScorerTest {

    @Test
    void sourceFilesScoreHigherThanBuildFiles() {
        PathScorer scorer = new PathScorer();

        FileRecord sourceFile = new FileRecord();
        sourceFile.setFilePath("src/main/java/searchengine/Main.java");
        sourceFile.setFileName("Main.java");
        sourceFile.setExtension("java");
        sourceFile.setSizeBytes(5000);

        FileRecord buildFile = new FileRecord();
        buildFile.setFilePath("target/classes/searchengine/Main.class");
        buildFile.setFileName("Main.class");
        buildFile.setExtension("class");
        buildFile.setSizeBytes(5000);

        double sourceScore = scorer.score(sourceFile);
        double buildScore = scorer.score(buildFile);

        assertTrue(sourceScore > buildScore);
    }

    @Test
    void readmeFileGetsPositiveScore() {
        PathScorer scorer = new PathScorer();

        FileRecord readme = new FileRecord();
        readme.setFilePath("README.md");
        readme.setFileName("README.md");
        readme.setExtension("md");
        readme.setSizeBytes(3000);

        assertTrue(scorer.score(readme) > 0);
    }

    @Test
    void veryLargeFileGetsLowerScoreThanSmallTextFile() {
        PathScorer scorer = new PathScorer();

        FileRecord smallFile = new FileRecord();
        smallFile.setFilePath("docs/notes.txt");
        smallFile.setFileName("notes.txt");
        smallFile.setExtension("txt");
        smallFile.setSizeBytes(2000);

        FileRecord largeFile = new FileRecord();
        largeFile.setFilePath("docs/archive.txt");
        largeFile.setFileName("archive.txt");
        largeFile.setExtension("txt");
        largeFile.setSizeBytes(10_000_000);

        assertTrue(scorer.score(smallFile) > scorer.score(largeFile));
    }
}