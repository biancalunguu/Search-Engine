package searchengine.ranking;

import searchengine.model.FileRecord;

public class PathScorer {

    public double score(FileRecord record) {
        double score = 0.0;

        String path = record.getFilePath().toLowerCase();
        String extension = record.getExtension() == null
                ? ""
                : record.getExtension().toLowerCase();

        score += scorePathLength(path);
        score += scoreImportantDirectories(path);
        score += scoreExtension(extension);
        score += scoreFileSize(record.getSizeBytes());

        return score;
    }

    private double scorePathLength(String path) {
        int depth = path.split("[/\\\\]").length;

        if (depth <= 3)
            return 3.0;
        if (depth <= 6)
            return 2.0;
        if (depth <= 10)
            return 1.0;

        return 0.0;
    }

    private double scoreImportantDirectories(String path) {
        double score = 0.0;

        if (path.contains("src"))
            score += 2.0;
        if (path.contains("main"))
            score += 1.5;
        if (path.contains("docs"))
            score += 1.5;
        if (path.contains("readme"))
            score += 2.0;

        if (path.contains("target"))
            score -= 2.0;
        if (path.contains("build"))
            score -= 1.5;
        if (path.contains("node_modules"))
            score -= 3.0;

        return score;
    }

    private double scoreExtension(String extension) {
        return switch (extension) {
            case "java" -> 2.0;
            case "md" -> 1.8;
            case "txt" -> 1.5;
            case "xml", "json", "properties" -> 1.2;
            case "class", "jar", "exe" -> -2.0;
            default -> 0.0;
        };
    }

    private double scoreFileSize(long size) {
        if (size <= 0)
            return -1.0;
        if (size < 10_000)
            return 1.0;
        if (size < 500_000)
            return 0.5;
        if (size > 5_000_000)
            return -1.0;

        return 0.0;
    }
}