package dev.hangalito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Bartolomeu Hangalo
 */
public class LocationService {
    private static final class LocationServiceHolder {
        private static LocationService INSTANCE;
    }

    /**
     * Gets an instance of {@link LocationService}.
     *
     * @return {@link LocationService}
     */
    public static LocationService getInstance() {
        if (LocationServiceHolder.INSTANCE == null) {
            String os = System.getProperty("os.name").toLowerCase();
            Path path;
            if (os.contains("win")) {
                path = Paths.get(System.getenv("APPDATA"), "LiteStore");
            } else if (os.contains("mac")) {
                path = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "LiteStore");
            } else if (os.contains("nux") || os.contains("nix")) {
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

    public String getAsString() {
        return path.toString();
    }

}
