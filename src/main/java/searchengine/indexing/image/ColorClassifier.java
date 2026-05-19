package searchengine.indexing.image;

/**
 * Converts an average RGB value into a simple color name that can be queried.
 */
public class ColorClassifier {

    public String classify(int red, int green, int blue) {
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        int brightness = (red + green + blue) / 3;

        if (brightness < 40) {
            return "black";
        }
        if (brightness > 220 && max - min < 35) {
            return "white";
        }
        if (max - min < 30) {
            return "gray";
        }

        if (red >= green + 35 && red >= blue + 35) {
            if (green > 110 && blue < 100) {
                return "orange";
            }
            if (blue > 110 && green < 100) {
                return "purple";
            }
            return "red";
        }

        if (green >= red + 30 && green >= blue + 30) {
            return "green";
        }

        if (blue >= red + 30 && blue >= green + 30) {
            return "blue";
        }

        if (red > 170 && green > 150 && blue < 120) {
            return "yellow";
        }

        if (red > 90 && green > 55 && blue < 70) {
            return "brown";
        }

        return "mixed";
    }
}
