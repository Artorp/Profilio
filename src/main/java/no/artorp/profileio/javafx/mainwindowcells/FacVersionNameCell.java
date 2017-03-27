package no.artorp.profileio.javafx.mainwindowcells;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import no.artorp.profileio.javafx.FactorioInstallation;
import no.artorp.profileio.javafx.Profile;
import no.artorp.profileio.javafx.Registry;

public class FacVersionNameCell extends TableCell<Profile, String> {
	
	private final ComboBox<FactorioInstallation> comboBox = new ComboBox<>();

	public FacVersionNameCell(Registry registry) {
		
		comboBox.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
		
		
		// Styling
		comboBox.setMaxWidth(Double.MAX_VALUE);
		
		// Cell factory
		Callback<ListView<FactorioInstallation>, ListCell<FactorioInstallation>> cbCellFactory
		= (ListView<FactorioInstallation> param) -> {
				return new ListCell<FactorioInstallation>(){
					@Override
		            protected void updateItem(FactorioInstallation item, boolean empty) {
		                super.updateItem(item, empty);
		                if (item == null || empty) {
		                    setGraphic(null);
		                } else {
		                    setText(item.getName());
		                }
		            }
				};
		};
		
		comboBox.setButtonCell((ListCell<FactorioInstallation>) cbCellFactory.call(null));
		comboBox.setCellFactory(cbCellFactory);
		
		// Populate combobox
		ObservableList<FactorioInstallation> options = registry.getFactorioInstallations();
		comboBox.setItems(options);
		
		// Event handling
		comboBox.setOnAction(event->{
			if (comboBox.getValue() != null) {
				commitEdit(comboBox.getValue().getName());
			}
		});
		
		// Check if our version still is in list, if not, change text to red
		options.addListener((javafx.collections.ListChangeListener.Change<? extends FactorioInstallation> c)->{
			updateTextColor();
		});
	}
	
	private boolean updateTextColor() {
		boolean flag = versionNameInList();
		if (flag) {
			setStyle("");
		} else {
			setStyle("-fx-text-fill: rgba(255,0,0,1);");
		}
		return flag;
	}
	
	private boolean versionNameInList() {
		ObservableList<FactorioInstallation> installs = comboBox.getItems();
		
		Profile profile = (Profile) getTableRow().getItem();
		
		if (profile == null) {
			return false;
		}
		if (profile.getFactorioVersion() == null) {
			return false;
		}
		
		for (FactorioInstallation fi : installs) {
			if (profile.getFactorioVersion().equals(fi.getName())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		
		updateTextColor();
		
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else if (isEditing()) {
			setText(null);
			setGraphic(comboBox);
		} else {
			setText(item);
			setGraphic(null);
		}
	}
	
	@Override
	public void startEdit() {
		super.startEdit();
		Profile p = (Profile) getTableRow().getItem();
		String facVersion = p.getFactorioVersion();
		if (facVersion != null) {
			for (FactorioInstallation f : comboBox.getItems()) {
				if (facVersion.equals(f.getName())) {
					comboBox.setValue(f);
					break;
				}
			}
		}
		
		setText(null);
		setGraphic(comboBox);
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getItem());
		setGraphic(null);
	}
	
	@Override
	public void commitEdit(String newValue) {
		Profile profile = (Profile) getTableRow().getItem();
		
		profile.setFactorioVersion(newValue);
		
		setText(profile.getFactorioVersion());
		setGraphic(null);
		super.commitEdit(newValue);
		
		
		updateTextColor();
	}

}
