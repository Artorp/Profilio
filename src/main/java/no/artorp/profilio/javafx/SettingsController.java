package no.artorp.profilio.javafx;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import no.artorp.profilio.exceptions.FactorioProfileManagerException;
import no.artorp.profilio.javafx.mainwindowcells.FacNameCell;
import no.artorp.profilio.utility.FileIO;
import no.artorp.profilio.utility.FileLocations;
import no.artorp.profilio.utility.SettingsIO;
import no.artorp.profilio.utility.WindowsJunctionUtility;

public class SettingsController {
	
	public static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private Stage settingsStage;
	private MainWindowController mainController;
	private SettingsIO settingsIO;
	private FileIO fileIO;
	private Registry myRegistry;
	private final DirectoryChooser directoryChooser = new DirectoryChooser();
	private final FileChooser fileChooser = new FileChooser();
	
	
	@FXML private TextField textFieldUserData;
	@FXML private Button buttonBrowseUserData;
	@FXML private TextField textFieldProfilesDir;
	@FXML private Button buttonBrowseProfilesDir;
	
	@FXML private Button buttonFirstTimeInit;
	
	@FXML private RadioButton radioRename;
	@FXML private RadioButton radioJunction;
	@FXML private RadioButton radioSymlink;
	@FXML private Button buttonMoreInfo;
	
	@FXML private CheckBox checkBoxClose;
	
	@FXML private TableView<FactorioInstallation> tableViewInstallations;
	@FXML private TableColumn<FactorioInstallation, String> columnName;
	@FXML private TableColumn<FactorioInstallation, String> columnDir;
	@FXML private TableColumn<FactorioInstallation, FactorioInstallation> columnCustomPath;
	@FXML private CheckBox checkBoxCustomPath;
	
	private ObservableList<Profile> mainTableViewData;
	
	@FXML private Button buttonNewEntry;
	@FXML private Button buttonRemoveEntry;
	@FXML private Button buttonBrowse;
	@FXML private Button buttonCustomPath;
	@FXML private Button buttonClose;

	public SettingsController(Stage primaryStage,
			Stage settingsStage,
			MainWindowController mainController,
			SettingsIO settingsIO,
			FileIO fileIO,
			Registry myRegistry,
			ObservableList<Profile> mainTableViewData) {
		this.settingsStage = settingsStage;
		this.mainController = mainController;
		this.settingsIO = settingsIO;
		this.fileIO = fileIO;
		this.myRegistry = myRegistry;
		this.mainTableViewData = mainTableViewData;
	}
	
	public void initialize() {
		
		textFieldUserData.textProperty().bind(
				myRegistry.factorioDataPathProperty().asString()
				);
		buttonBrowseUserData.setOnAction(event->{
			File initDir = myRegistry.factorioDataPathProperty().getValue().toFile();
			if (initDir.exists()) {
				directoryChooser.setInitialDirectory(initDir);
			}
			File newDataPath = directoryChooser.showDialog(settingsStage);
			if (newDataPath != null) {
				myRegistry.setFactorioDataPath(newDataPath.toPath());
				settingsIO.saveRegistry(myRegistry); // Save
			}
		});
		
		textFieldProfilesDir.textProperty().bind(
				myRegistry.factorioProfilesPathProperty().asString()
				);
		
		buttonBrowseProfilesDir.setOnAction(event->{
			File initDir = myRegistry.factorioProfilesPathProperty().getValue().toFile();
			if (initDir.exists()) {
				directoryChooser.setInitialDirectory(initDir);
			}
			File newDataPath = directoryChooser.showDialog(settingsStage);
			if (newDataPath != null) {
				this.mainController.stopWatcher();
				
				boolean hasInit = myRegistry.getHasInitialized().booleanValue();
				if (hasInit) {
					if (myRegistry.getActiveProfile() != null) {
						try {
							fileIO.revertMoveGeneral(myRegistry.getMoveMethod(),
									myRegistry.getFactorioDataPath(),
									myRegistry.getActiveProfilePath());
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE, "Error revert move before changing profile directory", e);
						}
					}
				}
				
				myRegistry.setFactorioProfilesPath(newDataPath.toPath());
				
				if (hasInit) {
					try {
						fileIO.performMoveGeneral(myRegistry.getMoveMethod(),
								myRegistry.getFactorioDataPath(),
								myRegistry.getActiveProfilePath());
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error moving active profile to new location", e);
					}
					this.mainController.refreshProfiles();
				}
				this.mainController.setupDirectoryWatcher(newDataPath.toPath());
				settingsIO.saveRegistry(myRegistry); // Save
			}
		});
		
		buttonFirstTimeInit.setOnAction(event->{
			if ( firstTimeInit() ) {
				mainController.refreshProfiles();
				Profile defaultActiveProfile = mainTableViewData.get(0);
				defaultActiveProfile.setIsActive(true);
				myRegistry.setActiveProfile(defaultActiveProfile);
				mainController.setupDirectoryWatcher(myRegistry.getFactorioProfilesPath());
				myRegistry.setHasInitialized(new Boolean(true));
			}
		});
		
		// Directory move method
		ToggleGroup moveMethod = new ToggleGroup();

		radioJunction.setToggleGroup(moveMethod);
		radioSymlink.setToggleGroup(moveMethod);
		radioRename.setToggleGroup(moveMethod);
		
		if (!FileLocations.isWindows()) {
			LOGGER.info("Junctions only available on Windows, disabling junction feature");
			radioJunction.setDisable(true);
		} else {
			if (!(new WindowsJunctionUtility()).testJunctionPermissions()) {
				LOGGER.warning("Runtime do not have permission to create junctions, disabling junctions");
				radioJunction.setDisable(true);
			}
		}
		if (! fileIO.testSymbolicLink()) {
			LOGGER.warning("Symbolic links not supported, disabling feature");
			radioSymlink.setDisable(true);
		}
		
		moveMethod.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			Path userDataPath = myRegistry.getFactorioDataPath();
			Profile activeProfile = myRegistry.getActiveProfile();
			Path activeProfilePath = null;
			if (activeProfile != null) {
				activeProfilePath = activeProfile.getDirectory().toPath();
			}
			if (oldValue != null && (activeProfile != null)) {
				if (oldValue.equals(radioJunction)) {
					try {
						fileIO.revertProfileJunctions(userDataPath);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error deleting junction links", e);
						Alert alert = new ExceptionDialog(e);
						alert.showAndWait();
						return;
					}
				} else if (oldValue.equals(radioSymlink)) {
					try {
						fileIO.revertProfileSymlinks(userDataPath);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error deleting symlinks", e);
						Alert alert = new ExceptionDialog(e);
						alert.showAndWait();
						return;
					}
				} else if (oldValue.equals(radioRename)) {
					try {
						fileIO.revertProfileMove(userDataPath, activeProfilePath);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error moving active profile folders", e);
						Alert alert = new ExceptionDialog(e);
						alert.showAndWait();
						return;
					}
				}
			}
			
			if (newValue != null) {
				if (newValue.equals(radioJunction)
						&& myRegistry.getMoveMethod().intValue() != FileIO.METHOD_JUNCTION ) {
					myRegistry.setMoveMethod(FileIO.METHOD_JUNCTION);
					if (activeProfile != null) {
						try {
							fileIO.performProfileJunctionCreation(activeProfilePath, userDataPath);
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE, "Error creating junctions", e);
							Alert alert = new ExceptionDialog(e);
							alert.showAndWait();
							return;
						}
					}
				} else if (newValue.equals(radioSymlink)
						&& myRegistry.getMoveMethod().intValue() != FileIO.METHOD_SYMLINK) {
					myRegistry.setMoveMethod(FileIO.METHOD_SYMLINK);
					settingsIO.saveRegistry(myRegistry); // Save
					if (activeProfile != null) {
						try {
							fileIO.performProfileSymlinks(activeProfilePath, userDataPath);
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE, "Error creating symlinks", e);
							Alert alert = new ExceptionDialog(e);
							alert.showAndWait();
							return;
						}
					}
				} else if (newValue.equals(radioRename)
						&& myRegistry.getMoveMethod().intValue() != FileIO.METHOD_MOVE) {
					myRegistry.setMoveMethod(FileIO.METHOD_MOVE);
					settingsIO.saveRegistry(myRegistry); // Save
					if (activeProfile != null) {
						try {
							fileIO.performProfileMove(activeProfilePath, userDataPath);
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE, "Error moving profile folders", e);
							Alert alert = new ExceptionDialog(e);
							alert.showAndWait();
							return;
						}
					}
				}
			}
		});
		
		buttonMoreInfo.setOnAction(event->{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText(null);
			String content = 
			"NTFS Junction\n"
			+ "    Similar to symbolic links in practice, but preferable on "
			+ "Windows systems as Windows requires elevated permission for "
			+ "symbolic link creation."
			+ "\n\n"
			+ "Symbolic links\n"
			+ "    Use symbolic links, similar to NTFS junctions. Requires "
			+ "special permissions on Windows systems."
			+ "\n\n"
			+ "Rename directory\n"
			+ "    While technically a rename, it will move the root folder "
			+ "when a new profile is selected. "
			+ "Example: Profile \"foo\" is active and profile \"bar\" is "
			+ "activated. Files belonging to \"foo\" is moved back to the "
			+ "profile folder, and files from \"bar\" are moved to the user "
			+ "data folder. Junctions or symlinks are preferable to this "
			+ "alternative as folders stay in the same directory while "
			+ "links to them are modified."
			+ "\n\n"
			+ "Disabled / grayed out option means that feature is not "
			+ "available on your system.";
			alert.setContentText(content);
			alert.showAndWait();
		});
		
		if (myRegistry.getMoveMethod() != null) {
			Integer intMoveMethod = myRegistry.getMoveMethod();
			if (intMoveMethod.intValue() == FileIO.METHOD_MOVE) {
				radioRename.setSelected(true);
			} else if (intMoveMethod.intValue() == FileIO.METHOD_JUNCTION) {
				radioJunction.setSelected(true);
			} else if (intMoveMethod.intValue() == FileIO.METHOD_SYMLINK) {
				radioSymlink.setSelected(true);
			}
		}
		
		checkBoxClose.selectedProperty().bindBidirectional(myRegistry.closeOnLaunchProperty());
		
		// Factorio installation tableview
		ObservableList<FactorioInstallation> tableData = myRegistry.getFactorioInstallations();
		
		columnName.setCellValueFactory(cellFeatures->cellFeatures.getValue().nameProperty());
		columnName.setCellFactory(tableCol->new FacNameCell(this.mainTableViewData));
		
		columnDir.setCellValueFactory(cellFeatures->cellFeatures.getValue().pathProperty().asString());
		
		
		tableViewInstallations.setItems(tableData);
		
		// Evaluate profiles, then evaluate launch button
		tableData.addListener((Observable observable) -> {
			for (Profile p : mainTableViewData) {
				if (p.getFactorioVersion() != null) {
					FactorioInstallation validVersion = myRegistry.findInstallation(p.getFactorioVersion());
					p.setFactorioInstallation(validVersion);
				}
			}
			
			mainController.evaluateLaunchButtonState();
		});
		
		
		tableViewInstallations.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			boolean noSelection = (newValue.intValue() == -1);
			buttonRemoveEntry.setDisable(noSelection);
			buttonBrowse.setDisable(noSelection);
			//buttonCustomPath.setDisable(noSelection);
		});
		
		buttonNewEntry.setOnAction(event->{
			fileChooser.setInitialDirectory(Paths.get(File.separator).toFile());
			File newFactorioExecutable = fileChooser.showOpenDialog(settingsStage);
			if (newFactorioExecutable != null) {
				String name = "Factorio Executable";
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Enter name for the new Factorio installation");
				dialog.setHeaderText(null);
				dialog.setContentText("Please enter name of new Factorio executable:");
				dialog.getEditor().textProperty().addListener((observable, oldVal, newVal) -> {
					if (newVal != null) {
						Node confirm = dialog.getDialogPane().lookupButton(ButtonType.OK);
						if (installNameAlreadyInUse(newVal)) {
							confirm.setDisable(true);
							dialog.getEditor().setStyle(
									"-fx-focus-color: rgba(255, 0, 0, 0.8); -fx-faint-focus-color:rgba(255, 100, 100, 0.2);"
									);
		        		} else {
							confirm.setDisable(false);
		        			dialog.getEditor().setStyle("");
						}
					}
				});
				dialog.getEditor().setText("Factorio executable");
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					name = result.get();
					if (installNameAlreadyInUse(name)) {
						return;
					}
				} else {
					return; // User cancelled
				}
				FactorioInstallation fi = new FactorioInstallation();
				fi.setName(name);
				fi.setPath(newFactorioExecutable.toPath());
				myRegistry.getFactorioInstallations().add(fi);
				settingsIO.saveRegistry(myRegistry); // Save
			}
		});
		
		buttonRemoveEntry.setOnAction(event->{
			FactorioInstallation fi = tableViewInstallations.getSelectionModel().getSelectedItem();
			if (fi != null) {
				myRegistry.getFactorioInstallations().remove(fi);
			}
		});
		
		buttonBrowse.setOnAction(event->{
			FactorioInstallation fi = tableViewInstallations.getSelectionModel().getSelectedItem();
			if (fi != null) {
				URI toBrowse = fi.getPath().getParent().toUri();
				LOGGER.info("Attempting to browse "+toBrowse);
				FileIO.browse(toBrowse);
			}
		});
		
		buttonCustomPath.disableProperty().bind(checkBoxCustomPath.selectedProperty().not().or(
				tableViewInstallations.getSelectionModel().selectedIndexProperty().isEqualTo(-1)
				));
		
		// Close button
		buttonClose.setOnAction(event->{
			settingsStage.hide();
		});
		
	}
	
	private boolean firstTimeInit() {
		// Detect saves and mods folders, move into profiles dir, setup symbolic link
		
		if (myRegistry.getMoveMethod() == null || myRegistry.getMoveMethod() == 0) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText(null);
			alert.setContentText("No move method chosen, select either rename, junction, or symlink.");
			alert.showAndWait();
			return false;
		}
		
		Path savesDir = myRegistry.getFactorioDataPath().resolve("saves");
		Path modsDir  = myRegistry.getFactorioDataPath().resolve("mods");
		if (checkAlreadySetUp()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Info: Already set up");
			alert.setHeaderText(null);
			
			String content = "Folder structure already set up\n\n"
			+ "Active profile: " + myRegistry.getActiveProfile().getName() + "\n\n"
			+ "Folders\n"
			+ modsDir.toString() + "\n" + savesDir.toString();
			alert.setContentText(content);
			
			ButtonType browseDir = new ButtonType("Open data folder...", ButtonData.OTHER);
			ButtonType btnOk = new ButtonType("OK", ButtonData.OK_DONE);
			
			alert.getButtonTypes().setAll(browseDir,btnOk);
			
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == browseDir) {
				URI toBrowse = myRegistry.getFactorioDataPath().toUri();
				LOGGER.info("Attempting to browse "+toBrowse);
				FileIO.browse(toBrowse);
			}
			
			return false;
		}
		
		// Verify existence of profile folder
		Path profileDir = myRegistry.getFactorioProfilesPath();
		if (!profileDir.toFile().exists()) {
			Alert alert = new Alert(AlertType.WARNING);
			//alert.setTitle("Profile folder not found!");
			alert.setHeaderText(null);
			alert.setContentText("Could not find profile folder" + System.lineSeparator()
			+ System.lineSeparator() + "Missing: "+profileDir.toString());
			alert.showAndWait();
			return false;
		}
		
		// Analyze actions required
		String operations;
		try {
			operations = fileIO.getInitialSetupReadable(myRegistry);
		} catch (FactorioProfileManagerException e) {
			LOGGER.log(Level.SEVERE, "Caught exception when peeking", e);
			return false;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Caught exception when peeking", e);
			return false;
		}
		
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setHeaderText(null);
		confirm.setContentText(
				"Moving saves and mods to profile folder."
				+ System.lineSeparator() + System.lineSeparator()
				+ "Please verify the following file operations:");
		
		TextArea textArea = new TextArea(operations);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		
		VBox expandable = new VBox(textArea);
		
		confirm.getDialogPane().setExpandableContent(expandable);
		confirm.getDialogPane().setExpanded(true);
		
		Optional<ButtonType> result = confirm.showAndWait();
		
		if (result.get().getButtonData() == ButtonData.CANCEL_CLOSE) {
			/*
			Alert success = new Alert(AlertType.INFORMATION);
			success.setHeaderText(null);
			success.setContentText("Operation cancelled, no file operations performed.");
			success.showAndWait();
			*/
			return false;
		}
		
		// Move files
		try {
			fileIO.performInitialSetup(myRegistry);
		} catch (FactorioProfileManagerException e) {
			LOGGER.log(Level.SEVERE, "Caught exception during initial setup", e);
			return false;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Caught exception during initial setup", e);
			return false;
		}
		
		// TODO: Check if stuff was successfully transferred
		Alert success = new Alert(AlertType.INFORMATION);
		success.setHeaderText(null);
		success.setContentText("Saves and mods folders successfully transferred.");
		success.showAndWait();
		
		return true;
	}
	
	private boolean installNameAlreadyInUse(String name) {
		for (FactorioInstallation f : this.tableViewInstallations.getItems()) {
			if (name.equalsIgnoreCase(f.getName())) {
				// Name conflict
				return true;
			}
		}
		return false;
	}
	
	private boolean checkAlreadySetUp() {
		if (! mainTableViewData.isEmpty()) {
			Path userDataPath = myRegistry.getFactorioDataPath();
			File mods = userDataPath.resolve(SettingsIO.FOLDER_NAME_MODS).toFile();
			File saves = userDataPath.resolve(SettingsIO.FOLDER_NAME_SAVES).toFile();
			if (mods.exists() && saves.exists()) {
				return true;
			}
		}
		return false;
	}

}
