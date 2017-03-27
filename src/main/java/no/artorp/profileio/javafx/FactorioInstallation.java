package no.artorp.profileio.javafx;

import java.nio.file.Path;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FactorioInstallation {
	
	private StringProperty name = new SimpleStringProperty();
	private ObjectProperty<Path> path = new SimpleObjectProperty<>();

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

}
