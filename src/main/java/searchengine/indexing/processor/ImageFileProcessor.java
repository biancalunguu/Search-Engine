package searchengine.indexing.processor;

import searchengine.indexing.image.ColorClassifier;
import searchengine.model.FileRecord;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy used for image files. It extracts a queryable dominant color.
 */
public class ImageFileProcessor implements FileProcessor {

    private final List<String> imageExtensions;
    private final ColorClassifier colorClassifier;

    public ImageFileProcessor(List<String> imageExtensions, ColorClassifier colorClassifier) {
        this.imageExtensions = imageExtensions;
        this.colorClassifier = colorClassifier;
    }

    @Override
    public boolean supports(FileRecord record) {
        return record.getExtension() != null
                && imageExtensions.contains(record.getExtension().toLowerCase());
    }

    @Override
    public void process(Path filePath, FileRecord record) throws IOException {
        record.setImageFile(true);

        BufferedImage image = ImageIO.read(filePath.toFile());
        if (image == null) {
            record.setImageFile(false);
            record.setDominantColor(null);
            record.setPreview("Image file could not be decoded.");
            return;
        }

        String dominantColor = extractDominantColor(image);
        record.setDominantColor(dominantColor);
        record.setPreview("Image file - dominant color: " + dominantColor);
    }

    private String extractDominantColor(BufferedImage image) {
        long redSum = 0;
        long greenSum = 0;
        long blueSum = 0;
        long pixelCount = 0;

        int stepX = Math.max(1, image.getWidth() / 100);
        int stepY = Math.max(1, image.getHeight() / 100);

        for (int y = 0; y < image.getHeight(); y += stepY) {
            for (int x = 0; x < image.getWidth(); x += stepX) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;

                if (alpha < 128) {
                    continue;
                }

                redSum += (argb >> 16) & 0xff;
                greenSum += (argb >> 8) & 0xff;
                blueSum += argb & 0xff;
                pixelCount++;
            }
        }

        if (pixelCount == 0) {
            return "unknown";
        }

        int averageRed = (int) (redSum / pixelCount);
        int averageGreen = (int) (greenSum / pixelCount);
        int averageBlue = (int) (blueSum / pixelCount);

        return colorClassifier.classify(averageRed, averageGreen, averageBlue);
    }
}
