package no.artorp.profileio.javafx.mainwindowcells;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import no.artorp.profileio.javafx.FactorioInstallations;
import no.artorp.profileio.javafx.Profile;

public class FacNameCell extends TableCell<FactorioInstallations, String> {
	
	private TextField textField = new TextField();
	private ObservableList<Profile> mainTableViewData;

	public FacNameCell(ObservableList<Profile> mainTableViewData) {
		this.mainTableViewData = mainTableViewData;
		
		// Let escape cancel editing
        textField.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        
        // Let Enter commit change
        textField.setOnAction(event->{
        	commitEdit(textField.getText());
        });
        
        // Show visual if name conflict
        textField.textProperty().addListener((observable, oldValue, newValue)->{
        	if (newValue != null) {
        		if (nameAlreadyInUse(newValue)) {
        			textField.setStyle("-fx-focus-color: rgba(255, 0, 0, 0.8); -fx-faint-focus-color:rgba(255, 100, 100, 0.2);");
        			//setStyle("-fx-background-color: rgba(255, 0, 0, 0.5);");
        		} else {
        			textField.setStyle("");
        		}
        		
        	}
        });
	}

	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else if (isEditing()) {
			textField.setText(item);
			setText(null);
			setGraphic(textField);
			textField.selectAll();
			textField.requestFocus();
		} else {
			setText(item);
			setGraphic(null);
		}
	}
	
	@Override
	public void startEdit() {
		super.startEdit();
		textField.setText(getItem());
		setText(null);
		setGraphic(textField);
		textField.requestFocus();
		textField.selectAll();
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getItem());
		setGraphic(null);
		setStyle("");
	}

	@Override
	public void commitEdit(String newValue) {
		if (nameAlreadyInUse(newValue)) {
			cancelEdit();
			return;
		}
		
		FactorioInstallations fi = (FactorioInstallations) getTableRow().getItem();
		
		
		String oldValue = fi.getName();
		
		
		super.commitEdit(newValue);
		fi.setName(newValue);
		setText(newValue);
		setGraphic(null);
		setStyle("");
		
		// Alert every profile about the change, and make changes where applicable
		for (Profile p : this.mainTableViewData) {
			if (p.getFactorioVersion() != null && p.getFactorioVersion().equals(oldValue)) {
				p.setFactorioVersion(newValue);
			}
		}
	}
	
	private boolean nameAlreadyInUse(String name) {
		FactorioInstallations fi = (FactorioInstallations) getTableRow().getItem();

		if (fi != null) {
			for (FactorioInstallations f : getTableView().getItems()) {
				if (!f.equals(fi)) {
					if (name.equalsIgnoreCase(f.getName())) {
						// Name conflict
						return true;
					}
				}
			}
		}
		return false;
	}

}
