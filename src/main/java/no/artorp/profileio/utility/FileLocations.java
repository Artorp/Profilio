package no.artorp.profileio.utility;

import java.io.File;
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
		String sep = File.separator;
		String configDirectory = "";
		if (isWindows()) {
			// Windows system, use AppData roaming
			configDirectory = System.getenv("AppData") + sep + DIR_CONFIG_NAME;
		} else if (isMac()) {
			// Mac, use user home folder
			configDirectory = System.getProperty("user.home");
			configDirectory += sep + "Library"+sep+"Application Support"+sep+DIR_CONFIG_NAME;
		} else if (isLinuxUnix()) {
			// Linux or Unix
			configDirectory = System.getenv("XDG_CONFIG_HOME");
			if (configDirectory == null) {
				configDirectory = System.getProperty("user.home");
				configDirectory += sep + ".config" + sep + DIR_CONFIG_NAME;
			} else {
				configDirectory += sep + DIR_CONFIG_NAME;
			}
		} else {
			// Unsupported OS, default to home folder
			configDirectory = System.getProperty("user.home");
			configDirectory += sep + DIR_CONFIG_NAME;
		}
		return Paths.get(configDirectory).toAbsolutePath();
	}
	
	/**
	 * Determines OS of app and returns a guess of the Factorio userdata location
	 * <p>
	 * Returns an empty string if OS detection fails
	 * @return Absolute path of guessed Factorio user data folder
	 */
	public static Path getFactorioUserDataDirectory() {
		String userDir = "";
		if (isWindows()) {
			// %AppData%\Factorio
			userDir = System.getenv("AppData") + File.separator + "Factorio";
		} else if (isMac()) {
			// ~/Library/Application Support/factorio
			userDir = System.getProperty("user.home");
			userDir += File.separator + "Library"+File.separator+"Application Support"+File.separator+"factorio";
		} else if (isLinuxUnix()) {
			// ~/.factorio
			userDir = System.getProperty("user.home");
			userDir += File.separator + ".factorio";
		}
		return Paths.get(userDir).toAbsolutePath();
	}

}
