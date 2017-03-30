package no.artorp.profilio.javafx.mainwindowcells;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	public static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
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
			if (p == null || newVal == null) return;
			if (newVal.booleanValue()) {
				LOGGER.info("Toggle radio button was changed");
				Profile previousActive = registry.getActiveProfile();
				Path previousProfileDataPath;
				Path newProfileDataPath;
				
				if (previousActive != null
						&& previousActive.getFactorioInstallation() != null
						&& previousActive.getFactorioInstallation().isUseCustomConfigPath()
						&& previousActive.getFactorioInstallation().getCustomConfigPath() != null) {
					previousProfileDataPath = previousActive.getFactorioInstallation().getCustomConfigPath();
				} else {
					previousProfileDataPath = registry.getFactorioDataPath();
				}
				
				if (p.getFactorioInstallation() != null
						&& p.getFactorioInstallation().isUseCustomConfigPath()
						&& p.getFactorioInstallation().getCustomConfigPath() != null) {
					newProfileDataPath = p.getFactorioInstallation().getCustomConfigPath();
				} else {
					newProfileDataPath = registry.getFactorioDataPath();
				}
				
				if (previousActive != null) {
					try {
						fileIO.revertMoveGeneral(registry.getMoveMethod(),
								previousProfileDataPath,
								previousActive.getDirectory().toPath());
						previousActive.setIsActive(false);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Exception when reverting move", e);
						Alert alert = new ExceptionDialog(e);
						alert.showAndWait();
					}
				}
				
				try {
					fileIO.performMoveGeneral(registry.getMoveMethod(),
							newProfileDataPath,
							p.getDirectory().toPath());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Exception when moving", e);
					Alert alert = new ExceptionDialog(e);
					alert.showAndWait();
					return;
				}
				
				p.setIsActive(true);
				registry.setActiveProfile(p);
				settingsIO.saveRegistry(registry); // Save
			} else {
				p.setIsActive(false);
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
			toggleRadioButton.setSelected(item);
			setGraphic(toggleRadioButton);
			setText(null);
		}
	}
	
}
