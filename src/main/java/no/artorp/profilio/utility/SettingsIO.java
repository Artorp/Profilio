package no.artorp.profilio.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import no.artorp.profilio.javafx.ExceptionDialog;
import no.artorp.profilio.javafx.FactorioInstallation;
import no.artorp.profilio.javafx.KeyValuePair;
import no.artorp.profilio.javafx.Profile;
import no.artorp.profilio.javafx.Registry;
import no.artorp.profilio.json_models.FactorioInstallationsJson;
import no.artorp.profilio.json_models.FactorioVersionMapJson;
import no.artorp.profilio.json_models.SettingsJson;

/**
 * Handles loading and saving of settings
 */
public class SettingsIO {
	
	public static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public static final String FOLDER_NAME_MODS = "mods";
	public static final String FOLDER_NAME_SAVES = "saves";
	
	private File settingsFile;

	public SettingsIO(File settingsFile) {
		this.settingsFile = settingsFile;
	}
	
	public void saveSettings(SettingsJson settingsObject) throws IOException {
		if (settingsObject == null) {
			throw new IllegalArgumentException("Settings object must be initialized.");
		}

		Gson gson = new GsonBuilder()
				.serializeNulls()
				.setPrettyPrinting()
				.create();
		String json_string = gson.toJson(settingsObject);
		try (Writer writer = new FileWriter(settingsFile, false)) {
			writer.write(json_string);
		}
	}
	
	public void saveRegistry(Registry r) {
		try {
			SettingsJson s = settingsFromRegistry(r);
			saveSettings(s);
		} catch (IOException e) {
			String errorMsg = "There was an error while saving settings.json";
			LOGGER.log(Level.SEVERE, errorMsg, e);
			Alert alert = new ExceptionDialog(e, errorMsg);
			alert.showAndWait();
		}
	}
	
	public SettingsJson loadSettings() throws IOException {
		Gson gson = new Gson();
		byte[] json_bytes = Files.readAllBytes(settingsFile.toPath());
		String json_string = new String(json_bytes, StandardCharsets.UTF_8);
		SettingsJson settings = gson.fromJson(json_string, SettingsJson.class);
		return settings;
	}

	public File getSettingsFile() {
		return settingsFile;
	}

	public void setSettingsFile(File settingsFile) {
		this.settingsFile = settingsFile;
	}
	
	public void putIntoRegistry(SettingsJson settings, Registry myRegistry) {
		// Get from JSON object
		Path configPath = Paths.get(settings.configPath);
		Path factorioDataPath = Paths.get(settings.factorioDataPath);
		Path factorioProfilesPath = Paths.get(settings.factorioProfilesPath);
		Integer moveMethod = settings.moveMethod;
		Boolean closeOnLaunch = settings.closeOnLaunch;
		Boolean hasInitialized = settings.hasInitialized;
		Path activeProfilePath = settings.activeProfilePath == null ? null : Paths.get(settings.activeProfilePath);
		
		ObservableList<FactorioInstallation> factorioInstallations = FXCollections.observableArrayList();
		List<FactorioInstallationsJson> installations = settings.factorioInstallations;
		for (FactorioInstallationsJson element : installations) {
			FactorioInstallation fi = new FactorioInstallation();
			fi.setName(element.customName);
			fi.setPath(Paths.get(element.path));
			fi.setUseCustomConfigPath(element.useCustomConfigPath);
			fi.setCustomConfigPath(element.customConfigPath == null
					? null
					: Paths.get(element.customConfigPath));
			factorioInstallations.add(fi);
		}
		
		ObservableList<KeyValuePair<String, String>> profileToFactorioName = FXCollections.observableArrayList();
		List<FactorioVersionMapJson> profileToInstallationMap = settings.profileToFactorioName;
		for (FactorioVersionMapJson e : profileToInstallationMap) {
			KeyValuePair<String, String> profileMap;
			profileMap = new KeyValuePair<String, String>(e.profileName, e.factorioName);
			profileToFactorioName.add(profileMap);
		}
		
		
		// Put in registry
		myRegistry.setConfigPath(configPath);
		myRegistry.setFactorioDataPath(factorioDataPath);
		myRegistry.setFactorioProfilesPath(factorioProfilesPath);
		myRegistry.setMoveMethod(moveMethod);
		myRegistry.setCloseOnLaunch(closeOnLaunch);
		myRegistry.setHasInitialized(hasInitialized);
		myRegistry.setActiveProfilePath(activeProfilePath);
		myRegistry.setFactorioInstallations(factorioInstallations);
		myRegistry.setProfileToFactorioName(profileToFactorioName);
	}
	
	public SettingsJson settingsFromRegistry(Registry myRegistry) {
		// Get from registry
		Path configPath = myRegistry.getConfigPath();
		Path factorioDataPath = myRegistry.getFactorioDataPath();
		Path factorioProfilesPath = myRegistry.getFactorioProfilesPath();
		Integer moveMethod = myRegistry.getMoveMethod();
		Boolean closeOnLaunch = myRegistry.getCloseOnLaunch();
		Boolean hasInitialized = myRegistry.getHasInitialized();
		Path activeProfilePath = myRegistry.getActiveProfilePath();
		List<FactorioInstallation> factorioInstallations = myRegistry.getFactorioInstallations();
		
		// Generate new list of profile-to-game-name-pairs
		List<KeyValuePair<String, String>> profileToFactorioName =
				this.getProfileToInstallationMap(myRegistry.getProfiles());
		
		
		// Generate Json object
		SettingsJson settings = new SettingsJson();
		settings.configPath = configPath.toString();
		settings.factorioDataPath = factorioDataPath.toString();
		settings.factorioProfilesPath = factorioProfilesPath.toString();
		settings.moveMethod = moveMethod.intValue();
		settings.closeOnLaunch = closeOnLaunch.booleanValue();
		settings.hasInitialized = hasInitialized.booleanValue();
		settings.activeProfilePath = activeProfilePath == null ? null : activeProfilePath.toString();
		
		List<FactorioInstallationsJson> installations = new ArrayList<>();
		for (FactorioInstallation fi : factorioInstallations) {
			FactorioInstallationsJson jsonObj = new FactorioInstallationsJson();
			jsonObj.customName = fi.getName();
			jsonObj.path = fi.getPath().toAbsolutePath().toString();
			jsonObj.useCustomConfigPath = fi.isUseCustomConfigPath();
			jsonObj.customConfigPath = fi.getCustomConfigPath() == null
					? null
					: fi.getCustomConfigPath().toAbsolutePath().toString();
			installations.add(jsonObj);
		}
		settings.factorioInstallations = installations;
		
		List<FactorioVersionMapJson> profileToInstallationMap = new ArrayList<>();
		for (KeyValuePair<String, String> pair : profileToFactorioName) {
			FactorioVersionMapJson jsonObj = new FactorioVersionMapJson();
			jsonObj.profileName = pair.getKey();
			jsonObj.factorioName = pair.getValue();
			profileToInstallationMap.add(jsonObj);
		}
		settings.profileToFactorioName = profileToInstallationMap;
		
		return settings;
	}
	
	/**
	 * Set values of registry to the best match
	 * <p>
	 * Will attempt to find the best settings for the registry. Will
	 * overwrite previously set field values where applicable. Assumes
	 * registry has been constructed, but not loaded from settings file.
	 * @param registry instance of {@link Registry} object to be initialized
	 */
	public void initializeRegistry(Registry registry) {
		registry.setConfigPath(
				FileLocations.getConfigDirectory()
				);
		
		registry.setFactorioDataPath(
				FileLocations.getFactorioUserDataDirectory()
				);
		
		registry.setFactorioProfilesPath(
				FileLocations.getFactorioUserDataDirectory().resolve(FileLocations.DIR_PROFILE_NAME)
				);
		
		registry.setCloseOnLaunch(true);
		registry.setHasInitialized(false);
		
		
		// Try to find Factorio installation path
		List<Path> guesses = new ArrayList<>();
		if (FileLocations.isWindows()) {
			guesses.add(Paths.get("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Factorio"));
			guesses.add(Paths.get("C:\\Program Files\\Factorio"));
			guesses.add(Paths.get("D:\\Program Files (x86)\\Steam\\steamapps\\common\\Factorio"));
			guesses.add(Paths.get("D:\\Program Files\\Factorio"));
			
			for (int i = 0; i < guesses.size(); i++) {
				Path g = guesses.get(i);
				g = g.resolve(Paths.get("bin", "x64", "factorio.exe"));
				g = g.toAbsolutePath();
				guesses.set(i, g);
			}
		} else if (FileLocations.isMac()) {
			guesses.add(Paths.get(System.getProperty("user.home"),
					"Library/Application Support/Steam/steamapps/common/Factorio/factorio.app/Contents"));
			guesses.add(Paths.get("/Applications/factorio.app/Contents"));
			
			for (int i = 0; i < guesses.size(); i++) {
				Path g = guesses.get(i);
				g = g.resolve(Paths.get("MacOS", "factorio"));
				g = g.toAbsolutePath();
				guesses.set(i, g);
			}
		} else if (FileLocations.isLinuxUnix()) {
			guesses.add(Paths.get(System.getProperty("user.home"),
					".local/share/Steam/steamapps/common/Factorio"));
			guesses.add(Paths.get(System.getProperty("user.home"),
					".steam/steam/steamapps/common/Factorio"));
			
			guesses.add(Paths.get(System.getProperty("user.home"), ".factorio"));
			
			for (int i = 0; i < guesses.size(); i++) {
				Path g = guesses.get(i);
				g = g.resolve(Paths.get("bin", "x64", "factorio"));
				g = g.toAbsolutePath();
				guesses.set(i, g);
			}
		}
		// Check if any of the path guesses exists
		for (Path g : guesses) {
			if (g.toFile().exists()) {
				FactorioInstallation fi = new FactorioInstallation();
				fi.setName("Factorio");
				fi.setPath(g);
				fi.setUseCustomConfigPath(false);
				fi.setCustomConfigPath(null);
				registry.getFactorioInstallations().add(fi);
				LOGGER.info("Installation found!\n" + g);
				// If installation seems to be from steam, notify user about steam cloud storage
				if (g.toString().toLowerCase().contains("steam")) {
					Alert info = new Alert(AlertType.INFORMATION);
					info.setHeaderText(null);
					info.setContentText("Reminder: If you're using Steam, make sure Steam Cloud "
							+ "is deactivated to prevent saves conflict!"
							+ "\n\nTip: Store the profile directory in a cloud service "
							+ "(Dropbox, Google Drive, OneDrive, etc) to keep saves and mods secure");
					info.setResizable(true);
					info.showAndWait();
				}
				break;
			}
		}
		
		// Is there a profile folder? If so, populate profileToFactorioName
		File profileDir = registry.getFactorioProfilesPath().toFile();
		if (registry.getFactorioInstallations() != null &&
				registry.getFactorioInstallations().size() > 0 &&
				profileDir.exists()) {
			File[] children = profileDir.listFiles();
			if (children != null) {
				String factorioInstallation = registry.getFactorioInstallations().get(0).getName();
				for (File child : children) {
					String profileName = child.getName();
					KeyValuePair<String, String> pair = new KeyValuePair<String, String>(profileName, factorioInstallation);
					registry.getProfileToFactorioName().add(pair);
				}
			}
		}
	}
	
	private List<KeyValuePair<String, String>> getProfileToInstallationMap(List<Profile> profiles) {
		List<KeyValuePair<String, String>> list = new ArrayList<>();
		for (Profile p : profiles) {
			if (p.getFactorioVersion() != null) {
				KeyValuePair<String, String> pair = new KeyValuePair<>(p.getName(), p.getFactorioVersion());
				list.add(pair);
			}
		}
		return list;
	}

}
