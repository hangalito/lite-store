package dev.hangalito.storage;

import java.io.File;
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

}
