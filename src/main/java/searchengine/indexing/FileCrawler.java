package searchengine.indexing;

import searchengine.config.ConfigurationManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recursively walks a directory tree and returns every file it discovers.
 */
public class FileCrawler {

    private final List<String> ignoreDirs;
    private final Set<Path> visitedRealPaths = new HashSet<>();
    private final List<Path> discoveredFiles = new ArrayList<>();
    private int permissionErrors = 0;

    public FileCrawler() {
        this.ignoreDirs = ConfigurationManager.getInstance().getIgnoreDirs();
    }

    /**
     * Crawls the given root path and returns all discovered file paths.
     */
    public List<Path> crawl(Path rootPath) {
        discoveredFiles.clear();
        visitedRealPaths.clear();
        permissionErrors = 0;

        if (!rootPath.toFile().exists()) {
            System.err.println("[ERROR] Root directory does not exist: " + rootPath);
            return discoveredFiles;
        }

        System.out.println("[INFO] Crawling: " + rootPath);

        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";

                    if (ignoreDirs.contains(dirName)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    try {
                        Path realPath = dir.toRealPath();
                        if (!visitedRealPaths.add(realPath)) {
                            System.out.println("[WARN] Circular reference detected, skipping: " + dir);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } catch (IOException e) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    discoveredFiles.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    if (exc instanceof AccessDeniedException) {
                        permissionErrors++;
                        if (permissionErrors <= 5) {
                            System.out.println("[WARN] Access denied: " + file);
                        } else if (permissionErrors == 6) {
                            System.out.println("[WARN] Further permission errors will be suppressed...");
                        }
                    } else {
                        System.err.println("[WARN] Could not visit: " + file + " (" + exc.getMessage() + ")");
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("[ERROR] Fatal error during file traversal: " + e.getMessage());
        }

        return discoveredFiles;
    }

    public int getPermissionErrors() {
        return permissionErrors;
    }
}
