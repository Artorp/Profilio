package no.artorp.profilio.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.artorp.profilio.utility.FileLocations;

public class MyLogger {
	
	public static final Logger LOGGER = Logger.getLogger(MyLogger.class.getName());

	/**
	 * Setup the logging environment
	 */
	public static void setup() {
		
		Logger rootLogger = Logger.getGlobal().getParent();
		
		for (Handler h : rootLogger.getHandlers()) {
			rootLogger.removeHandler(h);
		}
		
		rootLogger.setLevel(Level.INFO);
		
		Handler toConsole = new MyConsoleHandler();
		toConsole.setFormatter(new MyConsoleFormatter());
		rootLogger.addHandler(toConsole);
		
		Path configDir = FileLocations.getConfigDirectory();
		Path logCurrent = configDir.resolve(FileLocations.LOG_NAME_CURRENT);
		Path logPrevious = configDir.resolve(FileLocations.LOG_NAME_PREVIOUS);
		
		if (logCurrent.toFile().exists()) {
			try {
				Files.move(logCurrent, logPrevious, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Handler toLog = null;
		
		try {
			toLog = new FileHandler(logCurrent.toAbsolutePath().toString());
		} catch (SecurityException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		toLog.setFormatter(new MyLogFormatter());
		rootLogger.addHandler(toLog);
	}

}
