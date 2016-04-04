package org.hildan.fxlog.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.hildan.fxlog.errors.ErrorDialog;

/**
 * The ConfigLoader is responsible for reading and writing configuration files.
 */
class ConfigLoader {

    /**
     * The default location of the user's config.
     */
    private static final String USER_CONFIG_PATH =
            Paths.get(System.getProperty("user.home") + "/.fxlog/config.json").toAbsolutePath().toString();

    /**
     * The resource path of the default config.
     */
    private static final String BUILTIN_RESOURCE = "default_config.json";

    /**
     * Retrieves the user config, or the built-in config if the user config does not exist.
     *
     * @return the Config object corresponding to the current user config (or the default config)
     * @throws JsonSyntaxException
     *         if there is a JSON syntax error in the user config file
     */
    static Config getUserConfig() throws JsonSyntaxException {
        try {
            Config config = readConfigFrom(new FileReader(USER_CONFIG_PATH));
            if (config.getVersion() == Config.FORMAT_VERSION) {
                return config;
            } else {
                System.err.println("User config outdated");
                ErrorDialog.configOutdated();
            }
        } catch (FileNotFoundException e) {
            System.out.println("User config not found, falling back to built-in config");
        } catch (Exception e) {
            ErrorDialog.configReadException(USER_CONFIG_PATH, e);
        }
        return getBuiltinConfig();
    }

    private static Config getBuiltinConfig() {
        InputStream jsonConfigStream = ConfigLoader.class.getResourceAsStream(BUILTIN_RESOURCE);
        if (jsonConfigStream == null) {
            System.err.println("Something's wrong: built-in config not found!");
            return DefaultConfig.generate();
        }
        try {
            Config config = readConfigFrom(new InputStreamReader(jsonConfigStream));
            if (config.getVersion() == Config.FORMAT_VERSION) {
                return config;
            }
            System.err.println("Built-in config version does not match the current config version");
        } catch (JsonSyntaxException e) {
            System.err.println("Syntax error in built-in config. SHAME.");
        } catch (JsonIOException e) {
            System.err.println("IO error while reading built-in config");
        }
        System.err.println("Falling back to default config");
        return DefaultConfig.generate();
    }

    static void writeUserConfig(Config config) throws IOException {
        writeConfigTo(USER_CONFIG_PATH, config);
    }

    private static Config readConfigFrom(Reader source) throws JsonIOException, JsonSyntaxException {
        return FxGson.builder().create().fromJson(source, Config.class);
    }

    private static void writeConfigTo(String filename, Config config) throws IOException {
        Path filePath = Paths.get(filename);
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        // html escaping disabled to see <> in regexps in the JSON
        Gson gson = FxGson.builder().disableHtmlEscaping().setPrettyPrinting().create();
        List<String> lines = Collections.singletonList(gson.toJson(config));
        Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
