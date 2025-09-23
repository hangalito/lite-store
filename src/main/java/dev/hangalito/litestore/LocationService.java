package dev.hangalito.litestore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A singleton service responsible for determining and managing the file system locations
 * where LiteStore stores its data and index files. It abstracts away operating system-specific
 * details of application data directories.
 * @author Bartolomeu Hangalo
 * @since 1.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class LocationService {
    /**
     * Inner static class to hold the singleton instance, ensuring lazy initialization and thread safety.
     */
    private static final class LocationServiceHolder {
        private static volatile LocationService INSTANCE;
    }

    /**
     * Returns the singleton instance of {@code LocationService}.
     * If the instance does not exist, it determines the appropriate application data directory
     * based on the operating system and creates the directory if it doesn't exist.
     *
     * @return The singleton {@link LocationService} instance.
     * @throws RuntimeException if an {@link IOException} occurs during directory creation.
     */
    public static LocationService getInstance() {
        if (LocationServiceHolder.INSTANCE == null) {
            String osName = System.getProperty("os.name").toLowerCase();
            Path path;
            if (osName.contains("win")) {
                path = Paths.get(System.getenv("APPDATA"), "LiteStore");
            } else if (osName.contains("mac")) {
                path = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "LiteStore");
            } else if (osName.contains("nux") || osName.contains("nix")) {
                path = Paths.get(System.getProperty("user.home"), ".config", "LiteStore");
            } else {
                path = Paths.get(System.getProperty("user.home"), "LiteStore");
            }
            if (!path.toFile().exists()) {
                path.toFile().mkdir();
            }
            LocationServiceHolder.INSTANCE = new LocationService(path);
        }
        return LocationServiceHolder.INSTANCE;
    }

    /**
     * The base path for LiteStore data and index files.
     */
    private final Path path;

    /**
     * Private constructor to enforce singleton pattern.
     * @param path The base path for LiteStore files.
     */
    private LocationService(Path path) {
        this.path = path;
    }

    /**
     * Returns the base {@link Path} where LiteStore stores its files.
     * @return The base {@link Path}.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the base directory as a {@link File} object.
     * @return The base directory {@link File}.
     */
    public File getAsFile() {
        return path.toFile();
    }

    /**
     * Retrieves the {@link File} object for the data file (`.dat`) corresponding to a given entity.
     * If the file does not exist, it will be created.
     *
     * @param entity The name of the entity (typically the class name).
     * @return The {@link File} object for the entity's data file.
     * @throws RuntimeException if an {@link IOException} occurs during file creation.
     */
    public File getAsDatabase(String entity) {
        String filename = entity + ".dat";
        File file = new File(getAsFile(), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * Retrieves the {@link File} object for an index file (`.idx`) corresponding to a given field.
     * If this index file does not exist, it will be created.
     *
     * @param field The name of the index (e.g., entity class name for primary index, or "ClassName#fieldName" for custom index).
     * @return The {@link File} object for the index file. Will never be {@code null}.
     * @throws RuntimeException if an {@link IOException} occurs during file creation.
     */
    public File getAsIndex(String field) {
        String filename = field + ".idx";
        File file = new File(getAsFile(), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * Retrieves the {@link File} object for an index file (`.idx`) corresponding to a given field,
     * with an option to create it if it does not exist.
     *
     * @param field  The name of the field to get its index.
     * @param create Whether to create the index file if it does not exist. If {@code false} and the file does not exist, {@code null} will be returned.
     * @return The {@link File} object for the index, or {@code null} if {@code create} is {@code false} and the file does not exist.
     * @throws RuntimeException if an {@link IOException} occurs during file creation.
     */
    public File getAsIndex(String field, boolean create) {
        String filename = field + ".idx";
        File file = new File(getAsFile(), filename);
        if (!file.exists() && create) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!file.exists() && !create) file = null;

        return file;
    }

}
