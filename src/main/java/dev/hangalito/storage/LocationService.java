package dev.hangalito.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class LocationService {
    private static final class LocationServiceHolder {
        private static volatile LocationService INSTANCE;
    }

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

    private final Path path;

    private LocationService(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public File getAsFile() {
        return path.toFile();
    }

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
     * Retrieves the index of a specified field.
     * If this index does not exist yet, it will create
     * one and return an empty index.
     *
     * @param field The name of the index in the entity.
     * @return The index file. Will never be null.
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
     * Retrieves the index of a specified field.
     * If the index has already been indexed, returns it as a file,
     * if not, {@code null} will be returned.
     *
     * @param field  The name of the field to get its index.
     * @param create Whether to create the index if it does not exist.
     * @return The index as a file or {@code null} if the index does not exist.
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
