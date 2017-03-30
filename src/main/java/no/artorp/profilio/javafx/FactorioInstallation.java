package no.artorp.profilio.javafx;

import java.nio.file.Path;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FactorioInstallation {
	
	private StringProperty name = new SimpleStringProperty();
	private ObjectProperty<Path> path = new SimpleObjectProperty<>();
	
	private BooleanProperty useCustomConfigPath = new SimpleBooleanProperty();
	private ObjectProperty<Path> customConfigPath = new SimpleObjectProperty<>();

	public StringProperty nameProperty() {
		return this.name;
	}
	
	public String getName() {
		return this.name.getValue();
	}
	
	public void setName(String name) {
		this.name.setValue(name);
	}
	
	public ObjectProperty<Path> pathProperty() {
		return this.path;
	}
	
	public Path getPath() {
		return this.path.getValue();
	}
	
	public void setPath(Path path) {
		this.path.setValue(path);
	}

	public final BooleanProperty useCustomConfigPathProperty() {
		return this.useCustomConfigPath;
	}
	

	public final boolean isUseCustomConfigPath() {
		return this.useCustomConfigPathProperty().get();
	}
	

	public final void setUseCustomConfigPath(final boolean useCustomConfigPath) {
		this.useCustomConfigPathProperty().set(useCustomConfigPath);
	}

	public final ObjectProperty<Path> customConfigPathProperty() {
		return this.customConfigPath;
	}
	

	public final Path getCustomConfigPath() {
		return this.customConfigPathProperty().get();
	}
	

	public final void setCustomConfigPath(final Path customConfigPath) {
		this.customConfigPathProperty().set(customConfigPath);
	}
	
	
}
