package no.artorp.profileio.javafx;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

/**
 * Holds data commonly loaded and saved
 */
public class Registry {
	
	private ObjectProperty<Path> configPath = new SimpleObjectProperty<Path>();
	private ObjectProperty<Path> factorioDataPath = new SimpleObjectProperty<Path>();
	private ObjectProperty<Path> factorioProfilesPath = new SimpleObjectProperty<Path>();
	private IntegerProperty moveMethod = new SimpleIntegerProperty();
	
	private BooleanProperty closeOnLaunch = new SimpleBooleanProperty();
	private BooleanProperty hasInitialized = new SimpleBooleanProperty();
	
	private ObjectProperty<Path> activeProfilePath = new SimpleObjectProperty<Path>();
	private ObjectProperty<Profile> activeProfile = new SimpleObjectProperty<>();
	private StringProperty stringPathFileConverter = new SimpleStringProperty(); // This isn't exposed through getters
	
	private ObservableList<FactorioInstallation> factorioInstallations = FXCollections.observableArrayList();
	private ObservableList<KeyValuePair<String, String>> profileToFactorioName = FXCollections.observableArrayList();
	
	private List<Profile> profiles = new ArrayList<>();
	
	
	
	/**
	 * Sets up bindings between activeProfilePath and activeProfile
	 * <p>
	 * Since these are two different object types, two {@link StringConverter} are used to
	 * keep them in sync. activeProfile will only ever point to an object that is currently
	 * in the tableview.
	 * 
	 * @param profiles observablelist used in the tableview
	 */
	public void setupPathProfileBindings(ObservableList<Profile> profiles) {
		
		// TODO: REMOVE DEBUG PRINTERS:
		this.activeProfile.addListener((observable, o, n)->{
			System.out.println("active profile was changed: "+n.getName());
		});
		this.activeProfilePath.addListener((observable, o, n)->{
			System.out.println("active path was changed: "+n.getFileName());
		});
		
		
		// Use two bidirectional bindings and two stringconverters
		// with a string as an intermediary
		
		StringConverter<Path> pathConverter = new StringConverter<Path>() {
			@Override
			public String toString(Path object) {
				if (object == null) return null;
				return object.toString();
			}

			@Override
			public Path fromString(String string) {
				if (string == null) return null;
				return Paths.get(string);
			}
		};
		
		StringConverter<Profile> profileConverter = new StringConverter<Profile>() {
			@Override
			public String toString(Profile object) {
				if (object == null) return null;
				return object.getDirectory().toPath().toString();
			}

			@Override
			public Profile fromString(String string) {
				if (string == null) return null;
				for (Profile p : profiles) {
					if (p.getDirectory().toPath().equals(Paths.get(string))) {
						return p;
					}
				}
				return activeProfile.getValue(); // Avoid setting path to null by returning last value
			}
		};
		
		// Calling this puts string to converted value of profile
		stringPathFileConverter.bindBidirectional(this.activeProfile, profileConverter);
		
		// Calling this puts string to converted value of path,
		// and then profile to converted value of string
		// If path was initialized at this point, this call order would preserve the value
		stringPathFileConverter.bindBidirectional(this.activeProfilePath, pathConverter);
		
	}
	
	public FactorioInstallation findInstallation(String name) {
		for (FactorioInstallation fi : factorioInstallations) {
			if (fi.getName().equals(name)) {
				return fi;
			}
		}
		return null;
	}
	
	public String findGameName(String name) {
		for (KeyValuePair<String, String> pair : profileToFactorioName) {
			if (pair.getKey().equals(name)) {
				return pair.getValue();
			}
		}
		return null;
	}

	public ObjectProperty<Path> configPathProperty() {
		return configPath;
	}

	public void setConfigPath(Path configPath) {
		this.configPath.setValue(configPath);
	}
	
	public Path getConfigPath() {
		return this.configPath.getValue();
	}
	
	public ObjectProperty<Path> factorioDataPathProperty() {
		return factorioDataPath;
	}

	public void setFactorioDataPath(Path factorioDataPath) {
		this.factorioDataPath.setValue(factorioDataPath);
	}
	
	public Path getFactorioDataPath() {
		return this.factorioDataPath.getValue();
	}
	
	public ObjectProperty<Path> factorioProfilesPathProperty() {
		return factorioProfilesPath;
	}

	public void setFactorioProfilesPath(Path factorioProfilesPath) {
		this.factorioProfilesPath.setValue(factorioProfilesPath);
	}
	
	public Path getFactorioProfilesPath() {
		return this.factorioProfilesPath.getValue();
	}
	
	public IntegerProperty moveMethodProperty() {
		return this.moveMethod;
	}
	
	public Integer getMoveMethod() {
		return this.moveMethod.getValue();
	}
	
	public void setMoveMethod(Integer method) {
		this.moveMethod.setValue(method);
	}
	
	public BooleanProperty closeOnLaunchProperty() {
		return this.closeOnLaunch;
	}
	
	public Boolean getCloseOnLaunch() {
		return this.closeOnLaunch.getValue();
	}
	
	public void setCloseOnLaunch(Boolean closeOnLaunch) {
		this.closeOnLaunch.setValue(closeOnLaunch);
	}
	
	public BooleanProperty hasInitializedProperty() {
		return this.hasInitialized;
	}
	
	public Boolean getHasInitialized() {
		return this.hasInitialized.getValue();
	}
	
	public void setHasInitialized(Boolean hasInitialized) {
		this.hasInitialized.setValue(hasInitialized);
	}
	
	public ObjectProperty<Path> activeProfilePathProperty() {
		return activeProfilePath;
	}

	public void setActiveProfilePath(Path activeProfile) {
		this.activeProfilePath.setValue(activeProfile);
	}
	
	public Path getActiveProfilePath() {
		return this.activeProfilePath.getValue();
	}
	
	public ObjectProperty<Profile> activeProfileProperty() {
		return activeProfile;
	}
	
	public void setActiveProfile(Profile profile) {
		this.activeProfile.setValue(profile);
	}
	
	public Profile getActiveProfile() {
		return this.activeProfile.getValue();
	}

	public ObservableList<FactorioInstallation> getFactorioInstallations() {
		return factorioInstallations;
	}

	public void setFactorioInstallations(ObservableList<FactorioInstallation> factorioInstallations) {
		this.factorioInstallations = factorioInstallations;
	}

	public ObservableList<KeyValuePair<String, String>> getProfileToFactorioName() {
		return profileToFactorioName;
	}

	public void setProfileToFactorioName(ObservableList<KeyValuePair<String, String>> profileToFactorioName) {
		this.profileToFactorioName = profileToFactorioName;
	}
	
	public List<Profile> getProfiles() {
		return this.profiles;
	}
	
}
