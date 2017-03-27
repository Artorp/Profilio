package no.artorp.profileio.javafx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import no.artorp.profileio.exceptions.FactorioProfileManagerException;
import no.artorp.profileio.utility.FileIO;
import no.artorp.profileio.utility.SettingsIO;

/**
 * Instances of this class represents each subfolder of the profiles directory.
 * <p>
 * This directory contains two folders, "saves" and "mods"
 */
public class Profile {

	private StringProperty customName = new SimpleStringProperty();
	private StringProperty factorioVersion = new SimpleStringProperty();
	private ObjectProperty<File> directory = new SimpleObjectProperty<>();
	private BooleanProperty isActive = new SimpleBooleanProperty();
	private final boolean isDirectory;
	
	private Registry register;
	private SettingsIO settingsIO;
	
	private FactorioInstallation myVersion;
	
	public Profile(File directory, Registry myRegistry, boolean isActive, SettingsIO settingsIO) {
		this.register = myRegistry;
		this.directory.set(directory);
		this.customName.set(directory.getName());
		this.isDirectory = directory.isDirectory();
		this.settingsIO = settingsIO;
		
		// Bind myVersion to path and whether it exists
		this.factorioVersion.addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				this.myVersion = myRegistry.findInstallation(newValue);
			} else {
				this.myVersion = null;
			}
		});
		
		
		// To determine factorioVersion: Look up registry against our name
		String facVersion = myRegistry.findGameName(
				this.directory.getValue().getName()
				);

		this.factorioVersion.set(facVersion);
		
		this.isActive.setValue(isActive);
		
		
		// Bind customName to actual directory name
		this.directory.addListener((observable, oldValue, newValue)->{
			customName.set(newValue.getName());
		});
		
	}
	
	/**
	 * Rename actual file directory into new filename.
	 * 
	 * @param newFileName
	 * @throws InvalidPathException
	 * @throws FactorioProfileManagerException
	 * @throws IOException
	 */
	public void renameFile(String newFileName) throws InvalidPathException, FactorioProfileManagerException, IOException {
		File ourFile = this.getDirectory();
		Path newFilePath = Paths.get(newFileName);
		newFilePath = ourFile.toPath().getParent().resolve(newFilePath);
		System.out.println("Rename to: "+newFilePath);
		if (newFilePath.toFile().exists()) {
			throw new FactorioProfileManagerException("Invalid name. New file already exists!\n"+newFilePath);
		}
		
		FileIO fileIO = new FileIO();
		
		if (isActive()) {
			fileIO.revertMoveGeneral(register.getMoveMethod(), register.getFactorioDataPath(),
					getDirectory().toPath());
		}
		
		Files.move(ourFile.toPath(), newFilePath);
		
		
		if (newFilePath.toFile().exists()) {
			this.directory.setValue(newFilePath.toFile());
			if (isActive()) {
				fileIO.performMoveGeneral(register.getMoveMethod(), register.getFactorioDataPath(), newFilePath);
				register.setActiveProfile(this);
				settingsIO.saveRegistry(register); // Save
			}
		}
	}
	
	public boolean isDirectory() { return this.isDirectory; }
	
	// ## Getters and setters below
	
	public FactorioInstallation getFactorioInstallation() {
		return this.myVersion;
	}
	
	public void setFactorioInstallation(FactorioInstallation fi) {
		this.myVersion = fi;
	}
	
	public StringProperty customNameProperty() {
		return customName;
	}
	
	public String getName() {
		return customName.getValue();
	}
	
	/**
	 * This property is bound to the file object property, no setter needed
	 */
	/*
	public void setName(String name) {
		this.customName.setValue(name);
	}
	*/
	
	public StringProperty factorioVersionProperty() {
		return factorioVersion;
	}
	
	public String getFactorioVersion() {
		return this.factorioVersion.getValue();
	}
	
	public void setFactorioVersion(String factorioVersion) {
		this.factorioVersion.setValue(factorioVersion);
	}
	
	public ObjectProperty<File> directoryProperty() {
		return directory;
	}
	
	public File getDirectory() {
		return this.directory.getValue();
	}
	
	public void setDirectory(File directory) {
		this.directory.setValue(directory);
	}
	
	public BooleanProperty isActiveProperty() {
		return isActive;
	}
	
	public boolean isActive() {
		return isActive.getValue();
	}
	
	public void setIsActive(boolean active) {
		this.isActive.setValue(active);
	}

}
