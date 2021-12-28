package network.security;

import tech.fastj.logging.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public record SecureServerConfig(InputStream certificateFile, String certificatePassword, SecureTypes secureType) {
    public SecureServerConfig {
        if (secureType.compareTo(SecureTypes.TLSv1_2) < 0) {
            Log.warn(SecureServerConfig.class, "Using deprecated protocol {}.", secureType.name());
        }
    }

    public SecureServerConfig(Path certificatePath, String certificatePassword, SecureTypes secureType) throws FileNotFoundException {
        this(new FileInputStream(certificatePath.toFile()), certificatePassword, secureType);
    }
}
