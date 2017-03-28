package no.artorp.profilio.utility;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static methods for file locations
 */
public class FileLocations {
	
	public final static String DIR_CONFIG_NAME = "Factorio Profile Manager";
	public final static String DIR_PROFILE_NAME = "fpm";
	private static String OS = System.getProperty("os.name").toUpperCase();
	
	public static boolean isWindows() {
		return (OS.indexOf("WIN") >= 0);
	}
	
	public static boolean isMac() {
		return (OS.indexOf("MAC") >= 0);
	}
	
	public static boolean isLinuxUnix() {
		return (OS.indexOf("NIX") >= 0 || OS.indexOf("NUX") >= 0 || OS.indexOf("AIX") >= 0 );
	}
	
	/**
	 * Determines OS of application and returns applicable config folder for this application.
	 * Does not attempt to create said directory or otherwise validate it. Returns subfolder
	 * of home folder if OS detection fails.
	 * @return Absolute path of the configuration directory as a string
	 */
	public static Path getConfigDirectory() {
		Path configPath;
		if (isWindows()) {
			// Windows system, use AppData roaming
			configPath = Paths.get(System.getenv("AppData")).resolve(DIR_CONFIG_NAME);
			// configDirectory = System.getenv("AppData") + sep + DIR_CONFIG_NAME;
		} else if (isMac()) {
			// Mac, use user home folder
			configPath = Paths.get(System.getProperty("user.home"))
					.resolve("Library").resolve("Application Support")
					.resolve(DIR_CONFIG_NAME);
		} else if (isLinuxUnix()) {
			// Linux or Unix
			String configEnv = System.getenv("XDG_CONFIG_HOME");
			if (configEnv != null) {
				configPath = Paths.get(configEnv).resolve(DIR_CONFIG_NAME);
			} else {
				configPath = Paths.get(System.getProperty("user.home"))
						.resolve(".config")
						.resolve(DIR_CONFIG_NAME);
			}
		} else {
			// Unsupported OS, default to home folder
			configPath = Paths.get(System.getProperty("user.home"))
					.resolve(DIR_CONFIG_NAME);
		}
		return configPath.toAbsolutePath();
	}
	
	/**
	 * Determines OS of app and returns a guess of the Factorio userdata location
	 * <p>
	 * Returns the current working directory if guess fails
	 * 
	 * @return Absolute path of guessed Factorio user data folder
	 */
	public static Path getFactorioUserDataDirectory() {
		Path userDataPath = null;
		if (isWindows()) {
			// %AppData%\Factorio
			userDataPath = Paths.get(System.getenv("AppData")).resolve("Factorio");
		} else if (isMac()) {
			// ~/Library/Application Support/factorio
			userDataPath = Paths.get(System.getProperty("user.home"))
					.resolve("Library")
					.resolve("Application Support")
					.resolve("factorio");
		} else if (isLinuxUnix()) {
			// ~/.factorio
			userDataPath = Paths.get(System.getProperty("user.home"))
					.resolve(".factorio");
		} else {
			// Unsupported OS, return current directory and let user configure it
			userDataPath = Paths.get("").toAbsolutePath();
		}
		return userDataPath;
	}

}
