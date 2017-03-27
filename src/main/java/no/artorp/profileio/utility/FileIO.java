package no.artorp.profileio.utility;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import no.artorp.profileio.exceptions.FactorioProfileManagerException;
import no.artorp.profileio.javafx.ExceptionDialog;
import no.artorp.profileio.javafx.Registry;

public class FileIO {
	
	public static final int METHOD_MOVE = 1;
	public static final int METHOD_JUNCTION = 2;
	public static final int METHOD_SYMLINK = 3;
	
	
	public void createProfilesDir(Registry registry) {
		File profileDir = registry.getFactorioProfilesPath().toFile();
		if (!profileDir.exists()) {
			profileDir.mkdirs();
		}
	}
	
	/**
	 * Move mods and saves folder into profile folder, generate link to previous location
	 * <p>
	 * Most error checking should be done on UI level
	 * @param registry
	 * @param performFileOperations
	 * @return
	 * @throws FactorioProfileManagerException
	 * @throws IOException 
	 */
	private String initialSetupOperations(Registry registry, boolean performFileOperations)
			throws FactorioProfileManagerException, IOException {
		StringBuilder operationsReadable = new StringBuilder("");
		Path factorioUserDataPath = registry.getFactorioDataPath();
		File factorioUserDataFile = factorioUserDataPath.toFile();
		File profileDirFile = registry.getFactorioProfilesPath().toFile();
		if ((!factorioUserDataFile.exists())) {
			throw new FactorioProfileManagerException("Factorio data path does not exist!\n"
					+ "MISSING: " + factorioUserDataFile.getAbsolutePath());
		}
		if (!profileDirFile.exists()) {
			throw new FactorioProfileManagerException("Profile directory not found!\n"
					+ "MISSING: " +profileDirFile.getAbsolutePath());
		}
		
		// Make sure "mods" and "saves" folders aren't symbolic
		Path modsPath = factorioUserDataFile.toPath().resolve("mods");
		if (Files.isSymbolicLink(modsPath)) {
			throw new FactorioProfileManagerException("Mods folder is already a symbolic link!\n"
					+ modsPath.toString() );
		}
		Path savesPath = factorioUserDataFile.toPath().resolve("saves");
		if (Files.isSymbolicLink(savesPath)) {
			throw new FactorioProfileManagerException("Saves folder is already a symbolic link!\n"
					+ modsPath.toString() );
		}
		
		String firstProfileName = "default_profile";
		
		Path newModsPath = profileDirFile.toPath().resolve(Paths.get(firstProfileName, "mods"));
		Path newSavesPath = profileDirFile.toPath().resolve(Paths.get(firstProfileName, "saves"));
		Path profilePath = profileDirFile.toPath().resolve(firstProfileName);
		
		if (!profilePath.toFile().exists()) {
			operationsReadable.append("Create folder: "+System.lineSeparator()+ profilePath.toString());
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(System.lineSeparator());
		}
		
		int moveMethod = registry.getMoveMethod();
		
		if (moveMethod != METHOD_MOVE) {
			// If moving, no need to move files back and forth
			operationsReadable.append("Move: ");
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(modsPath +" -> " + newModsPath);
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append("Move:");
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(savesPath +" -> " + newSavesPath);
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(System.lineSeparator());
			
			String opName = "";
			if (moveMethod == METHOD_JUNCTION) {
				opName = "Create junction: ";
			} else if (moveMethod == METHOD_SYMLINK) {
				opName = "Create symlink: ";
			}

			operationsReadable.append(opName+System.lineSeparator()+ newModsPath +" -> " + modsPath);
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(opName+System.lineSeparator()+ newSavesPath +" -> " + savesPath);
			operationsReadable.append(System.lineSeparator());
			operationsReadable.append(System.lineSeparator());
		}
		
		
		
		if (performFileOperations) {

			profilePath.toFile().mkdir();
			
			// No need to move files back and forth
			if (moveMethod != METHOD_MOVE) {
				Files.move(modsPath, newModsPath);
				Files.move(savesPath, newSavesPath);
			}
			
			if (moveMethod == METHOD_JUNCTION) {
				performProfileJunctionCreation(profilePath, factorioUserDataPath);
				/*
				WindowsJunctionUtility jUtil = new WindowsJunctionUtility();
				
				jUtil.createJunction(modsPath, newModsPath);
				jUtil.createJunction(savesPath, newSavesPath);
				*/
			} else if (moveMethod == METHOD_SYMLINK) {
				performProfileSymlinks(profilePath, factorioUserDataPath);
				/*
				Files.createSymbolicLink(modsPath, newModsPath);
				Files.createSymbolicLink(savesPath, newSavesPath);
				*/
			} else if (moveMethod == METHOD_MOVE) {
				// No need to move files back and forth
				
				// Deprecated
				/*
				Files.walkFileTree(newSavesPath, new CopyFileVisitor(savesPath));
				Files.walkFileTree(newModsPath, new CopyFileVisitor(modsPath));
				*/
			}
			
		}
		
		return operationsReadable.toString();
	}
	
	public String getInitialSetupReadable(Registry registry) throws FactorioProfileManagerException, IOException {
		return initialSetupOperations(registry, false);
	}
	
	public void performInitialSetup(Registry registry) throws FactorioProfileManagerException, IOException {
		initialSetupOperations(registry, true);
	}
	
	/**
	 * Creates a symlink in user directory to check if app has symlink permissions
	 * 
	 * @return {@code true} if creation of symlink worked, {@code false} if it failed
	 */
	public boolean testSymbolicLink() {
		Path home = Paths.get(System.getProperty("user.home"));
		
		Path target = home.resolve("testTarget");
		Path link = home.resolve("testLink");
		
		target.toFile().mkdir();
		
		if (link.toFile().exists()) {
			try {
				Files.deleteIfExists(link);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		try {
			Files.createSymbolicLink(link, target);
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
		
		if (link.toFile().exists()) {
			try {
				Files.deleteIfExists(target);
				Files.deleteIfExists(link);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		try {
			Files.deleteIfExists(target);
			Files.deleteIfExists(link);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Checks if path is a link
	 * <p>
	 * Compares a Path against its real path. Will not distinguish
	 * between junction and symlink, both return {@code true}
	 * 
	 * @param p  the path to be tested
	 * @return   {@code true} if real path differ, {@code false} otherwise
	 */
	public static boolean isJunctionOrSymlink(Path p) throws NoSuchFileException {
		boolean isLink = false;
		try {
			isLink = (p.compareTo(p.toRealPath()) != 0);
		} catch (IOException e) {
			System.err.println("An error occurred while checking if file is junction.");
			e.printStackTrace();
		}
		return isLink;
	}
	
	/**
	 * Moves files from profile folder to user data folder
	 * 
	 * @param profileDirectory
	 * @param userDataFolder
	 * @throws IOException
	 */
	public void performProfileMove(Path profileDirectory, Path userDataFolder) throws IOException {
		// Move the files
		Files.move(profileDirectory.resolve(SettingsIO.FOLDER_NAME_MODS), userDataFolder.resolve(SettingsIO.FOLDER_NAME_MODS));
		Files.move(profileDirectory.resolve(SettingsIO.FOLDER_NAME_SAVES), userDataFolder.resolve(SettingsIO.FOLDER_NAME_SAVES));
	}
	
	/**
	 * Moves files from user data folder to profile folder
	 * 
	 * @param userDataFolder path to move "saves" and "mods" from
	 * @param profileDirectory path to move folders into
	 * @throws IOException 
	 */
	public void revertProfileMove(Path userDataFolder, Path profileDirectory) throws IOException {
		// Move the files
		Files.move(userDataFolder.resolve(SettingsIO.FOLDER_NAME_MODS), profileDirectory.resolve(SettingsIO.FOLDER_NAME_MODS));
		Files.move(userDataFolder.resolve(SettingsIO.FOLDER_NAME_SAVES), profileDirectory.resolve(SettingsIO.FOLDER_NAME_SAVES));
	}
	
	/**
	 * Create junctions in user data folder
	 * @param profileDirectory
	 * @param userDataFolder
	 * @throws IOException
	 */
	public void performProfileJunctionCreation(Path profileDirectory, Path userDataFolder) throws IOException {
		WindowsJunctionUtility jUtil = new WindowsJunctionUtility();
		
		// Profile folder
		Path profileMods  = profileDirectory.resolve(SettingsIO.FOLDER_NAME_MODS);
		Path profileSaves = profileDirectory.resolve(SettingsIO.FOLDER_NAME_SAVES);
		
		// User data folder
		Path dataMods  = userDataFolder.resolve(SettingsIO.FOLDER_NAME_MODS);
		Path dataSaves = userDataFolder.resolve(SettingsIO.FOLDER_NAME_SAVES);
		
		jUtil.createJunction(dataMods, profileMods);
		jUtil.createJunction(dataSaves, profileSaves);
	}
	
	/**
	 * Remove junctions in user data folder
	 * @param userDataFolder
	 * @throws IOException
	 */
	public void revertProfileJunctions(Path userDataFolder) throws IOException {
		revertLinks(userDataFolder);
	}
	
	/**
	 * Create symlinks in user data folder from profile folder
	 * 
	 * @param profileDirectory
	 * @param userDataFolder
	 * @throws IOException
	 */
	public void performProfileSymlinks(Path profileDirectory, Path userDataFolder) throws IOException {
		// Profile folder
		Path profileMods  = profileDirectory.resolve(SettingsIO.FOLDER_NAME_MODS);
		Path profileSaves = profileDirectory.resolve(SettingsIO.FOLDER_NAME_SAVES);
		
		// User data folder
		Path dataMods  = userDataFolder.resolve(SettingsIO.FOLDER_NAME_MODS);
		Path dataSaves = userDataFolder.resolve(SettingsIO.FOLDER_NAME_SAVES);
		
		if (! (profileMods.toFile().exists() && profileSaves.toFile().exists()) ) {
			String errorMessage = "Profile \"saves\" and \"mods\" folder not found\n"+profileMods+"\n"+profileSaves;
			throw new FileNotFoundException(errorMessage);
		}
		
		Files.createSymbolicLink(dataMods, profileMods);
		Files.createSymbolicLink(dataSaves, profileSaves);
	}
	
	/**
	 * Remove symlinks in user data folder
	 * 
	 * @param userDataFolder
	 * @throws IOException
	 */
	public void revertProfileSymlinks(Path userDataFolder) throws IOException {
		revertLinks(userDataFolder);
	}
	
	private void revertLinks(Path userDataFolder) throws IOException {
		// User data folder
		Path dataMods  = userDataFolder.resolve(SettingsIO.FOLDER_NAME_MODS);
		Path dataSaves = userDataFolder.resolve(SettingsIO.FOLDER_NAME_SAVES);
		
		// Verify they are junctions or links
		try {
			dataMods.toRealPath();
			dataSaves.toRealPath();
		} catch (IOException e) {
			// Can't resolve, might be pointing at nothing, try to delete
			Files.deleteIfExists(dataMods);
			Files.deleteIfExists(dataSaves);
			return;
		}
		if ( (! FileIO.isJunctionOrSymlink(dataMods)) || (! FileIO.isJunctionOrSymlink(dataSaves)) ) {
			throw new IOException("Links to be deleted must be links\n"+dataMods+"\n"+dataSaves);
		}

		Files.deleteIfExists(dataMods);
		Files.deleteIfExists(dataSaves);
	}
	
	public void revertMoveGeneral(Integer moveMethod, Path factorioUserData, Path toBeRemovedProfilePath)
			throws IOException {
		if (moveMethod == null || moveMethod.intValue() == 0) {
			throw new IllegalArgumentException("moveMethod Integer must be set or non-null.");
		}
		
		if (moveMethod.intValue() == METHOD_JUNCTION) {
			revertProfileJunctions(factorioUserData);
		} else if (moveMethod.intValue() == METHOD_SYMLINK) {
			revertProfileSymlinks(factorioUserData);
		} else if (moveMethod.intValue() == METHOD_MOVE) {
			revertProfileMove(factorioUserData, toBeRemovedProfilePath);
		}
	}
	
	public void performMoveGeneral(Integer moveMethod, Path factorioUserData, Path copyFrom) throws IOException {
		if (moveMethod == null || moveMethod.intValue() == 0) {
			throw new IllegalArgumentException("moveMethod Integer must be set or non-null.");
		}
		
		if (moveMethod.intValue() == METHOD_JUNCTION) {
			performProfileJunctionCreation(copyFrom, factorioUserData);
		} else if (moveMethod.intValue() == METHOD_SYMLINK) {
			performProfileSymlinks(copyFrom, factorioUserData);
		} else if (moveMethod.intValue() == METHOD_MOVE) {
			performProfileMove(copyFrom, factorioUserData);
		}
	}
	
	/**
	 * Browse uri if Desktop supported
	 * 
	 * @param uri
	 * @return {@code true} if succeeded, {@code false otherwise}
	 */
	public static void browse(URI uri) {
		if (! canBrowse()) {
			new Alert(AlertType.INFORMATION,
					"Browsing throught Desktop.getDesktop().browse(URI) not supported on this platform")
			.showAndWait();
			return;
		}
		new Thread(() -> { // New thread to fix freeze on Linux
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				e.printStackTrace();
				Platform.runLater(() ->
				new ExceptionDialog(e, "Error when attempting to browse").showAndWait()
				);
			}
		}).start();
	}
	
	public static boolean canBrowse() {
		if (Desktop.isDesktopSupported()) {
			if (Desktop.getDesktop().isSupported(Action.BROWSE)) {
				return true;
			}
		}
		return false;
	}

}
