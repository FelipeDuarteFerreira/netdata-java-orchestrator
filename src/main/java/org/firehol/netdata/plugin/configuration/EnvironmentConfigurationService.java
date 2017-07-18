package org.firehol.netdata.plugin.configuration;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.firehol.netdata.Main;
import org.firehol.netdata.plugin.configuration.exception.EnvironmentConfigurationException;
import org.firehol.netdata.utils.LoggingUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Singleton for reading defined Environment Variables.
 * 
 * Quits the program if it could not initialize.
 * 
 * @author Simon Nagl
 * @since 1.0.0
 *
 */
@Getter
public class EnvironmentConfigurationService {
	@Getter(AccessLevel.NONE)
	private final Logger log = Logger.getLogger("org.firehol.netdata.daemon.configuration.environment");

	private Path configDir;

	private static final EnvironmentConfigurationService INSTANCE = new EnvironmentConfigurationService();

	public static EnvironmentConfigurationService getInstance() {
		return INSTANCE;
	}

	private EnvironmentConfigurationService() {
		try {
			readEnvironmentVariables();
		} catch (EnvironmentConfigurationException e) {
			// Fail fast if this important singleton could not initialize.
			Main.exit(e.getMessage());
		}
	}

	private void readEnvironmentVariables() throws EnvironmentConfigurationException {
		configDir = readNetdataConfigDir();
	}

	protected Path readNetdataConfigDir() throws EnvironmentConfigurationException {
		log.fine("Parse environment variable NETDATA_CONFIG_DIR");
		String configDirString = System.getenv("NETDATA_CONFIG_DIR");

		if (configDirString == null) {
			throw new EnvironmentConfigurationException(
					"Expected environment variable 'NETDATA_CONFIG_DIR' is missing");
		}

		Path configDir;
		 
		try {
			configDir = Paths.get(configDirString);
		} catch (InvalidPathException e) {
			throw new EnvironmentConfigurationException(LoggingUtils.buildMessage("NETDATA_CONFIG_DIR contains no valid path name.", e));
		}

		return configDir;

	}
}
