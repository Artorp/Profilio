package no.artorp.profilio;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import no.artorp.profilio.javafx.MainWindowController;
import no.artorp.profilio.javafx.Registry;
import no.artorp.profilio.logging.MyLogger;
import no.artorp.profilio.utility.FileIO;
import no.artorp.profilio.utility.FileLocations;
import no.artorp.profilio.utility.Globals;
import no.artorp.profilio.utility.SettingsIO;

public class App extends Application {
	
	public static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private SettingsIO settingsIO;
	private Registry myRegistry;

	/**
	 * Do not use this <em>directly</em> as program main entry point,
	 * <br>
	 * see {@link Main#main(String[])}
	 * 
	 * @param args
	 * @see Main#main(String[])
	 */
	public static void main_proxy(String[] args) {
		MyLogger.setup();
		
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Thread.currentThread().setName("javafx");
		
		LOGGER.log(Level.INFO, String.format("%s (%s, javafx: %s), OS: %s (%s)",
				System.getProperty("java.runtime.name"),
				System.getProperty("java.version"),
				System.getProperty("javafx.version"),
				System.getProperty("os.name"),
				System.getProperty("os.arch")));

		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_16.png")));
		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_32.png")));
		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_48.png")));
		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_256.png")));
		
		Registry myRegistry = new Registry();
		this.myRegistry = myRegistry;
		
		// Set up SettingsIO object
		String fileName = "settings.json";
		File configDir = FileLocations.getConfigDirectory().toFile();
		if (!configDir.exists()) {
			if (!configDir.mkdirs()) {
				String errorMsg = "Could not create config directory at location:\n"+configDir.getAbsolutePath();
				LOGGER.severe(errorMsg);
				throw new RuntimeException(errorMsg);
			}
		}
		
		File settingsFile = new File(configDir + File.separator + fileName);
		FileIO fileIO = new FileIO();
		SettingsIO settingsIO = new SettingsIO(settingsFile);
		this.settingsIO = settingsIO;
		
		// Setup scene, controller, stage
		primaryStage.setTitle("Profilio - Factorio Profile Manager");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
		MainWindowController controller = new MainWindowController(primaryStage, settingsIO, fileIO, myRegistry);
		loader.setController(controller);
		BorderPane root = (BorderPane) loader.load();
		
		Scene scene = new Scene(root);
		//scene.getStylesheets().add("/MainWindowStyle.css");
		primaryStage.setScene(scene);
		
		primaryStage.show();
	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Platform stopping, interrupting all threads");
		
		long start = System.currentTimeMillis();
		long patience = 500;
		
		for (Thread t : Globals.THREADS) {
			t.interrupt();
		}
		
		for (Thread t : Globals.THREADS) {
			t.join(Math.max(0, patience + start - System.currentTimeMillis()));
		}

		LOGGER.info("Saving settings file");
		
		settingsIO.saveRegistry(myRegistry); // Save
		
		super.stop();
	}
}
