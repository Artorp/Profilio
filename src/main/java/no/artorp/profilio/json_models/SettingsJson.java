package no.artorp.profilio.json_models;

import java.util.ArrayList;
import java.util.List;

/**
 * Json modal to be used by Gson
 * <p>
 * Contains fields for settings.json
 */
public class SettingsJson {
	
	public String configPath;
	public String factorioDataPath;
	public String factorioProfilesPath;
	public int moveMethod;
	public boolean closeOnLaunch;
	public boolean hasInitialized;
	public String activeProfilePath;
	
	public List<FactorioInstallationsJson> factorioInstallations = new ArrayList<>();
	public List<FactorioVersionMapJson> profileToFactorioName = new ArrayList<>();
	
}
