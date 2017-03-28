package no.artorp.profilio.utility;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import no.artorp.profilio.javafx.Profile;
import no.artorp.profilio.javafx.Registry;

public class ProfileDirectoryHelper {
	
	
	/**
	 * @param profileDirectory directory to check for profile folders
	 * @param registry         the registry to pass into profile constructor
	 * @returnA list of {@link Profile profiles} generated from a profile directory path
	 */
	public static List<Profile> getProfiles(Path profileDirectory, Registry registry, SettingsIO settingsIO) {
		List<Profile> profiles = new ArrayList<Profile>();
		if (profileDirectory == null) {
			// Return empty list if no profile path has been set
			registry.getProfiles().clear();
			return profiles;
		}

		File[] files = profileDirectory.toFile().listFiles();
		
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) continue;
				
				// Determine if this is the active profile
				Path activeProfile = registry.getActiveProfilePath();
				boolean isActiveProfile = (f.toPath().equals(activeProfile));
				Profile p = new Profile(f, registry, isActiveProfile, settingsIO);
				if (isActiveProfile) {
					registry.setActiveProfile(p);
				}
				
				String factorioVersion = registry.findGameName(f.getName());
				if (factorioVersion == null) {
					if (! registry.getFactorioInstallations().isEmpty()) {
						// No defined factorio version for this profile? Assign the first one
						factorioVersion = registry.getFactorioInstallations().get(0).getName();
					}
				}
				p.setFactorioVersion(factorioVersion);
				
				profiles.add(p);
			}
		}
		
		registry.getProfiles().clear();
		registry.getProfiles().addAll(profiles);
		return profiles;
	}
	
	/**
	 * Generates list of profiles from a directory
	 * <p>
	 * Looks up target profile directory in registry, {@link Registry#getFactorioProfilesPath()}
	 * @param registry registry used to look up target folder and to pass into generated profiles
	 * @return list of profiles
	 */
	public static List<Profile> getProfiles(Registry registry, SettingsIO settingsIO) {
		return getProfiles(registry.getFactorioProfilesPath(), registry, settingsIO);
	}
}
