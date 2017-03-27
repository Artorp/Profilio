package no.artorp.profileio;
import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import no.artorp.profileio.javafx.MainWindowController;
import no.artorp.profileio.javafx.Registry;
import no.artorp.profileio.utility.FileIO;
import no.artorp.profileio.utility.FileLocations;
import no.artorp.profileio.utility.Globals;
import no.artorp.profileio.utility.SettingsIO;

public class App extends Application {
	
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
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_32.png")));
		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_64.png")));
		primaryStage.getIcons().add(new Image( getClass().getResourceAsStream("/icon_128.png")));
		
		Registry myRegistry = new Registry();
		this.myRegistry = myRegistry;
		
		// Set up SettingsIO object
		String fileName = "settings.json";
		File configDir = FileLocations.getConfigDirectory().toFile();
		if (!configDir.exists()) {
			if (!configDir.mkdirs()) {
				String errorMsg = "Could not create config directory at location:\n"+configDir.getAbsolutePath();
				throw new RuntimeException(errorMsg);
			}
		}
		
		File settingsFile = new File(configDir + File.separator + fileName);
		FileIO fileIO = new FileIO();
		SettingsIO settingsIO = new SettingsIO(settingsFile);
		this.settingsIO = settingsIO;
		
		// Setup scene, controller, stage
		primaryStage.setTitle("Profileio - Factorio Profile Manager");
		
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
		System.out.println("Platform stopping, interrupting all threads");
		
		long start = System.currentTimeMillis();
		long patience = 500;
		
		for (Thread t : Globals.THREADS) {
			t.interrupt();
		}
		
		for (Thread t : Globals.THREADS) {
			t.join(Math.max(0, patience + start - System.currentTimeMillis()));
		}
		
		System.out.println("Saving settings file");
		
		settingsIO.saveRegistry(myRegistry); // Save
		
		
		super.stop();
	}
}
