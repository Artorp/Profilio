package no.artorp.profilio.javafx.mainwindowcells;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import no.artorp.profilio.exceptions.FactorioProfileManagerException;
import no.artorp.profilio.javafx.ExceptionDialog;
import no.artorp.profilio.javafx.MainWindowController;
import no.artorp.profilio.javafx.Profile;

public class ProfileNameCell extends TableCell<Profile, Profile> {
	
	private TextField textField = new TextField();
	private MainWindowController mainController;

	public ProfileNameCell(MainWindowController controller) {
		this.mainController = controller;
		
		// Let escape cancel editing
        textField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        
        // Let Enter commit change
        textField.setOnAction(event->{
        	commitEdit((Profile) getTableRow().getItem());
        });
        
        // Verify name on the fly
        textField.textProperty().addListener((observable, oldval, newval) -> {
        	if (newval != null) {
        		if (nameAlreadyInUse(newval)) {
        			textField.setStyle("-fx-focus-color: rgba(255, 0, 0, 0.8); -fx-faint-focus-color:rgba(255, 100, 100, 0.2);");
        		} else {
        			textField.setStyle("");
        		}
        	}
        });
	}
	
	@Override
	protected void updateItem(Profile item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else if (isEditing()) {
			textField.setText(item.getName());
			setText(null);
			setGraphic(textField);
			textField.selectAll();
			textField.requestFocus();
		} else {
			setText(item.getName());
			setGraphic(null);
		}
	}
	
	@Override
	public void startEdit() {
		super.startEdit();
		textField.setText(getItem().getName());
		setText(null);
		setGraphic(textField);
		textField.requestFocus();
		textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getItem().getName());
		setGraphic(null);
	}

	@Override
	public void commitEdit(Profile newValue) {
		Profile profile = (Profile) getTableRow().getItem();
		
		String newName = textField.getText();
		
		Path oldPath = profile.getDirectory().toPath();
		Path newPath = oldPath.getParent().resolve(newName);
		
		// Make sure file events are suppressed in main controller
		this.mainController.ignoreTheseEvents(oldPath, newPath);
		
		try {
			profile.renameFile(newName);
		} catch (InvalidPathException | FactorioProfileManagerException e_1) {
			e_1.printStackTrace();
			Alert exceptionDialog = new ExceptionDialog(e_1, "Invalid filename.");
			exceptionDialog.showAndWait();
			cancelEdit();
			this.mainController.stopIgnoreTheseEvents(oldPath, newPath);
			return;
		} catch (IOException e_2) {
			e_2.printStackTrace();
			Alert exceptionDialog = new ExceptionDialog(e_2, "An error occurred while renaming.");
			exceptionDialog.showAndWait();
			cancelEdit();
			this.mainController.stopIgnoreTheseEvents(oldPath, newPath);
			return;
		}
		
		setText(profile.getDirectory().getName());
		setGraphic(null);
		super.commitEdit(newValue);
		getTableView().sort();
	}

	
	private boolean nameAlreadyInUse(String name) {
		Profile p = (Profile) getTableRow().getItem();
		if (p != null) {
			for (Profile pFromTable : getTableView().getItems()) {
				if (pFromTable.equals(p)) continue;
				if (pFromTable.getName().equalsIgnoreCase(name)) return true;
			}
		}
		return false;
	}

}
