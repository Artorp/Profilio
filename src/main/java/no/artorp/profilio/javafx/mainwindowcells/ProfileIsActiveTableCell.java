package no.artorp.profilio.javafx.mainwindowcells;

import java.io.IOException;
import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import no.artorp.profilio.javafx.ExceptionDialog;
import no.artorp.profilio.javafx.Profile;
import no.artorp.profilio.javafx.Registry;
import no.artorp.profilio.utility.FileIO;
import no.artorp.profilio.utility.SettingsIO;

public class ProfileIsActiveTableCell extends TableCell<Profile, Boolean> {
	
	private Registry registry;
	
	private final ToggleButton toggleRadioButton = new ToggleButton("Active"){
		@Override public void fire() {
			if (registry.getMoveMethod() == null || registry.getMoveMethod().intValue() == 0) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setHeaderText(null);
				alert.setContentText("No move method set!");
				alert.showAndWait();
				return;
			}
			if (getToggleGroup() == null || !isSelected()) {
				super.fire();
			}
		}
	};

	public ProfileIsActiveTableCell(ToggleGroup tGroup, Registry registry, FileIO fileIO, SettingsIO settingsIO) {
		this.registry = registry;
		toggleRadioButton.setToggleGroup(tGroup);
		toggleRadioButton.setMaxWidth(Double.MAX_VALUE);
		
		toggleRadioButton.selectedProperty().addListener((observable, oldVal, newVal)->{
			Profile p = (Profile) getTableRow().getItem();
			if (p != null && newVal != null && newVal.booleanValue()) {
				System.out.println("Toggle radio button was changed");
				Profile previousActive = registry.getActiveProfile();
				Path userDataPath = registry.getFactorioDataPath();
				
				try {
					fileIO.revertMoveGeneral(registry.getMoveMethod(), userDataPath, previousActive.getDirectory().toPath());
				} catch (IOException e) {
					e.printStackTrace();
					Alert alert = new ExceptionDialog(e);
					alert.showAndWait();
				}
				
				try {
					fileIO.performMoveGeneral(registry.getMoveMethod(), userDataPath, p.getDirectory().toPath());
				} catch (IOException e) {
					e.printStackTrace();
					Alert alert = new ExceptionDialog(e);
					alert.showAndWait();
					return;
				}
				
				p.setIsActive(newVal);
				registry.setActiveProfile(p);
				settingsIO.saveRegistry(registry); // Save

			}
		});
	}

	@Override
	protected void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);
		
		if (item == null || empty) {
			setGraphic(null);
			setText(null);
		} else {
			/*
			Profile p = (Profile) getTableRow().getItem();
			System.out.println("profile is null: "+p);
			*/
			toggleRadioButton.setSelected(item);
			setGraphic(toggleRadioButton);
			setText(null);
		}
	}
	
}
