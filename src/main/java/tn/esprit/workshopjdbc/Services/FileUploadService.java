package tn.esprit.workshopjdbc.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileUploadService {
    private static final String UPLOAD_DIR = "forum_uploads";
    private static final String IMAGE_DIR = "images";
    private static final String VIDEO_DIR = "videos";

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList("mp4", "avi", "mov", "mkv", "webm"));
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50 MB

    static {
        ensureUploadDirectoriesExist();
    }

    private static void ensureUploadDirectoriesExist() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR, IMAGE_DIR));
            Files.createDirectories(Paths.get(UPLOAD_DIR, VIDEO_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String uploadImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            return null;
        }

        if (!isValidImage(imageFile)) {
            return null;
        }

        try {
            String fileName = generateFileName(imageFile.getName(), "image");
            String destinationPath = Paths.get(UPLOAD_DIR, IMAGE_DIR, fileName).toString();

            Files.copy(imageFile.toPath(), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            return destinationPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String uploadVideo(File videoFile) {
        if (videoFile == null || !videoFile.exists()) {
            return null;
        }

        if (!isValidVideo(videoFile)) {
            return null;
        }

        try {
            String fileName = generateFileName(videoFile.getName(), "video");
            String destinationPath = Paths.get(UPLOAD_DIR, VIDEO_DIR, fileName).toString();

            Files.copy(videoFile.toPath(), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
            return destinationPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isValidImage(File file) {
        if (file.length() > MAX_IMAGE_SIZE) {
            return false;
        }

        String extension = getFileExtension(file.getName()).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    private static boolean isValidVideo(File file) {
        if (file.length() > MAX_VIDEO_SIZE) {
            return false;
        }

        String extension = getFileExtension(file.getName()).toLowerCase();
        return ALLOWED_VIDEO_EXTENSIONS.contains(extension);
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    private static String generateFileName(String originalFileName, String type) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFileName);
        return type + "_" + timestamp + "_" + System.nanoTime() + "." + extension;
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    public static String getUploadDirectory() {
        return UPLOAD_DIR;
    }

    public static String getImageDirectory() {
        return Paths.get(UPLOAD_DIR, IMAGE_DIR).toString();
    }

    public static String getVideoDirectory() {
        return Paths.get(UPLOAD_DIR, VIDEO_DIR).toString();
    }
}
