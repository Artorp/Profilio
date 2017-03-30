package no.artorp.profilio.javafx.mainwindowcells;

import javafx.scene.control.TableCell;
import no.artorp.profilio.javafx.FactorioInstallation;

public class FacUseCustomPathCell extends TableCell<FactorioInstallation, Boolean> {

	@Override
	protected void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
		} else {
			setText(item.booleanValue() ? "Y" : "N" );
			if (item.booleanValue()) {
				// Show custom background
				if (((FactorioInstallation) getTableRow().getItem()).getCustomConfigPath() == null) {
					// Red if no path defined
					setStyle("-fx-alignment: CENTER;-fx-background-color: rgba(255, 0, 0, 0.5);");
				} else {
					// Purple if all OK
					setStyle("-fx-alignment: CENTER;-fx-background-color: rgba(143, 71, 217, 0.5);");
				}
			} else {
				setStyle("-fx-alignment: CENTER;");
			}
			setGraphic(null);
		}
	}


}
