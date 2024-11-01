package data;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class ImageUtils {
    private static final ConcurrentHashMap<String, WeakReference<Image>> IMAGE_CACHE = new ConcurrentHashMap<>();

    public static Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        WeakReference<Image> imageRef = IMAGE_CACHE.get(imagePath);
        Image image = imageRef != null ? imageRef.get() : null;

        if (image == null) {
            try {
                // Try loading from file system
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    image = new Image(imageFile.toURI().toString(), false);
                } else {
                    // Try loading from classpath
                    String classPathPath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
                    if (ImageUtils.class.getResource(classPathPath) != null) {
                        image = new Image(ImageUtils.class.getResource(classPathPath).toExternalForm(), false);
                    } else {
                        // Try direct URL
                        image = new Image(imagePath, false);
                    }
                }

                if (!image.isError()) {
                    IMAGE_CACHE.put(imagePath, new WeakReference<>(image));
                } else {
                    System.err.println("Error loading image from: " + imagePath);
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Exception loading image: " + e.getMessage());
                return null;
            }
        }

        return image;
    }

    public static WritableImage cropToSquare(Image sourceImage) {
        if (sourceImage == null || sourceImage.isError()) {
            return null;
        }

        double size = Math.min(sourceImage.getWidth(), sourceImage.getHeight());
        double x = (sourceImage.getWidth() - size) / 2;
        double y = (sourceImage.getHeight() - size) / 2;

        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(sourceImage, x, y, size, size, 0, 0, size, size);

        return canvas.snapshot(null, null);
    }

    public static WritableImage cropAndScale(Image sourceImage, double targetWidth, double targetHeight) {
        if (sourceImage == null || sourceImage.isError()) {
            return null;
        }

        double sourceRatio = sourceImage.getWidth() / sourceImage.getHeight();
        double targetRatio = targetWidth / targetHeight;

        double srcX = 0;
        double srcY = 0;
        double srcWidth = sourceImage.getWidth();
        double srcHeight = sourceImage.getHeight();

        if (sourceRatio > targetRatio) {
            srcWidth = sourceImage.getHeight() * targetRatio;
            srcX = (sourceImage.getWidth() - srcWidth) / 2;
        } else {
            srcHeight = sourceImage.getWidth() / targetRatio;
            srcY = (sourceImage.getHeight() - srcHeight) / 2;
        }

        Canvas canvas = new Canvas(targetWidth, targetHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(sourceImage,
                srcX, srcY, srcWidth, srcHeight,
                0, 0, targetWidth, targetHeight);

        return canvas.snapshot(null, null);
    }
}