package com.engine;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Utils {
    public static Path getFile(String fileName, Type type, String folderName) {
        String os = System.getProperty("os.name").toLowerCase();
        Path configurationDirectory;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                configurationDirectory = Paths.get(appData, folderName);
            } else {
                configurationDirectory = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", folderName);
            }
        } else if (os.contains("mac")) {
            configurationDirectory = Paths.get(System.getProperty("user.home"), "Library", "Application Support", folderName);
        } else {
            configurationDirectory = Paths.get(System.getProperty("user.home"), ".config", folderName);
        }

        try {
            if (!Files.exists(configurationDirectory)) {
                Files.createDirectories(configurationDirectory);
            }

            Path filePath = configurationDirectory.resolve(fileName);
            if (!Files.exists(filePath)) {
                if (type == Type.FILE)
                    Files.createFile(filePath);
                else if (type == Type.DIRECTORY)
                    Files.createDirectory(filePath);
                else
                    throw new IllegalArgumentException("Unknown type " + type.name());
            }
            return filePath;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static boolean isFileEmpty(Path path) {
        try (InputStream inputStream=Files.newInputStream(path)) {
            int aByte= inputStream.read();
            return aByte == -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Color oppositeColor(Color color) {
        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
    }

    public enum Type {
        DIRECTORY,
        FILE
    }
}
