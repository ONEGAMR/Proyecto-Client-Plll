package data;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageUtils {
    private static final ConcurrentHashMap<String, WeakReference<Image>> IMAGE_CACHE = new ConcurrentHashMap<>();

    public static WritableImage cropToSquare(Image sourceImage) {
        if (isInvalidImage(sourceImage)) {
            return null;
        }

        double size = Math.min(sourceImage.getWidth(), sourceImage.getHeight());
        double x = (sourceImage.getWidth() - size) / 2;
        double y = (sourceImage.getHeight() - size) / 2;

        return createScaledImage(sourceImage, x, y, size, size, size, size);
    }

    public static Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        WeakReference<Image> imageRef = IMAGE_CACHE.get(imagePath);
        Image image = imageRef != null ? imageRef.get() : null;

        if (image == null) {
            image = tryLoadImage(imagePath);
            if (image != null && !image.isError()) {
                IMAGE_CACHE.put(imagePath, new WeakReference<>(image));
            }
        }

        return image;
    }

    private static Image tryLoadImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString(), false);
            }

            String classPathPath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
            if (ImageUtils.class.getResource(classPathPath) != null) {
                return new Image(ImageUtils.class.getResource(classPathPath).toExternalForm(), false);
            }

            return new Image(imagePath, false);
        } catch (Exception e) {
            System.err.println("Exception loading image: " + e.getMessage());
            return null;
        }
    }

    private static WritableImage createScaledImage(Image sourceImage, double srcX, double srcY,
                                                   double srcWidth, double srcHeight,
                                                   double targetWidth, double targetHeight) {
        Canvas canvas = new Canvas(targetWidth, targetHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(sourceImage, srcX, srcY, srcWidth, srcHeight, 0, 0, targetWidth, targetHeight);
        return canvas.snapshot(null, null);
    }

    private static boolean isInvalidImage(Image sourceImage) {
        return sourceImage == null || sourceImage.isError();
    }
}