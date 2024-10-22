package cloud.loify.packages.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class ImageUtils {

    private String loifyPlaylistImage(String imageUrl) throws IOException {
        // Download image from the provided URL
        URL url = new URL(imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        // Resize the image if it exceeds the maximum dimension
        BufferedImage resizedImage = resizeImage(originalImage);

        // Create a new BufferedImage to hold the final image (with emoji overlay)
        BufferedImage finalImage = new BufferedImage(resizedImage.getWidth(), resizedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // Create a graphics object for the final image
        Graphics2D g2d = finalImage.createGraphics();

        // Set the composite to draw the original image at 60% opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2d.drawImage(resizedImage, 0, 0, null);

        // Reset the composite to draw the emoji with full opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Set font and get the metrics for the emoji size
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 100));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        String emoji = "🍃";

        // Calculate the position to center the emoji
        int stringWidth = fontMetrics.stringWidth(emoji);
        int stringHeight = fontMetrics.getAscent();

        int centerX = (finalImage.getWidth() - stringWidth) / 2;
        int centerY = ((finalImage.getHeight() - stringHeight) / 2) + stringHeight - 10;

        // Draw the emoji in the center of the image
        g2d.drawString(emoji, centerX, centerY);

        g2d.dispose(); // Clean up graphics context

        // Convert the final image with the emoji to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(finalImage, "png", baos);
        byte[] finalImageBytes = baos.toByteArray();

        // Encode to Base64
        String base64Image = Base64.getEncoder().encodeToString(finalImageBytes);

        // Return the Base64 string to the client
        return base64Image;
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate new dimensions while maintaining aspect ratio
        int newWidth;
        int newHeight;

        if (originalWidth > originalHeight) {
            newWidth = Math.min(originalWidth, 200);
            newHeight = (int) ((double) originalHeight * newWidth / originalWidth);
        } else {
            newHeight = Math.min(originalHeight, 200);
            newWidth = (int) ((double) originalWidth * newHeight / originalHeight);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

}
