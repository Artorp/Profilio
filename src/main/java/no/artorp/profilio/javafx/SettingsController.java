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
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.effect.DropShadow;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import no.artorp.profilio.exceptions.FactorioProfileManagerException;
import no.artorp.profilio.javafx.mainwindowcells.FacNameCell;
import no.artorp.profilio.javafx.mainwindowcells.FacUseCustomPathCell;
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
	@FXML private TableColumn<FactorioInstallation, Boolean> columnCustomPath;
	@FXML private CheckBox checkBoxCustomPath;
	@FXML private TextField textFieldCustomPath;
	@FXML private Label labelCustomPath;
	
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
			} else {
				directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
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
			File userDataDir = myRegistry.factorioDataPathProperty().getValue().toFile();
			if (initDir.exists()) {
				directoryChooser.setInitialDirectory(initDir);
			} else if (userDataDir.exists()) {
				directoryChooser.setInitialDirectory(userDataDir);
			} else {
				directoryChooser.setInitialDirectory(null);
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
				myRegistry.setHasInitialized(true);
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
		
		// Set effects, only when not initialized
		DropShadow borderYellow = new DropShadow(3,
				new Color((246f/255f), (249f/255f), (14f/255f), 0.8));
		DropShadow borderGreen = new DropShadow(3,
				new Color((29f/255f), (229f/255f), (14f/225f), 0.8));
		
		ChangeListener<Toggle> effectToggleListener = (observable, oldValue, newValue) -> {
			if (newValue == null) {
				// Nothing selected, show yellow border glow
				for (Toggle t : moveMethod.getToggles()) {
					RadioButton rb = (RadioButton) t;
					if (rb.isDisabled()) {
						rb.setEffect(null);
					} else {
						rb.setEffect(borderYellow);
					}
				}
			} else {
				for (Toggle t : moveMethod.getToggles()) {
					RadioButton rb = (RadioButton) t;
					if (rb.equals(newValue)) {
						rb.setEffect(borderGreen);
					} else {
						rb.setEffect(null);
					}
				}
				// Add effect to "First time initialization" button
				buttonFirstTimeInit.setEffect(borderYellow);
			}
		};
		
		if (! myRegistry.getHasInitialized().booleanValue()) {
			// Add effect to radio buttons
			moveMethod.selectedToggleProperty().addListener(effectToggleListener);
			effectToggleListener.changed(null, null, moveMethod.getSelectedToggle());
		}
		
		myRegistry.hasInitializedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null && newValue.booleanValue()) {
				radioJunction.setEffect(null);
				radioSymlink.setEffect(null);
				radioRename.setEffect(null);
				buttonFirstTimeInit.setEffect(null);
				moveMethod.selectedToggleProperty().removeListener(effectToggleListener);
			} else {
				moveMethod.selectedToggleProperty().addListener(effectToggleListener);
			}
		});
		
		moveMethod.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			Path userDataPath = myRegistry.getFactorioDataPath();
			Profile activeProfile = myRegistry.getActiveProfile();
			Path activeProfilePath = null;
			if (activeProfile != null) {
				activeProfilePath = activeProfile.getDirectory().toPath();
			}
			if (oldValue != null && (activeProfile != null)
					&& activeProfile.getFactorioInstallation() != null) {
				
				int prevMoveMethod;
				if (oldValue.equals(radioJunction)) {
					prevMoveMethod = FileIO.METHOD_JUNCTION;
				} else if (oldValue.equals(radioSymlink)) {
					prevMoveMethod = FileIO.METHOD_SYMLINK;
				} else if (oldValue.equals(radioRename)) {
					prevMoveMethod = FileIO.METHOD_RENAME;
				} else {
					return;
				}
				
				FactorioInstallation fi = activeProfile.getFactorioInstallation();
				Path oldUserDataPath;
				if (fi.isUseCustomConfigPath()
						&& fi.getCustomConfigPath() != null) {
					oldUserDataPath = fi.getCustomConfigPath();
				} else {
					oldUserDataPath = myRegistry.getFactorioDataPath();
				}
				try {
					fileIO.revertMoveGeneral(prevMoveMethod, oldUserDataPath, activeProfilePath);
				} catch (IOException e1) {
					LOGGER.log(Level.SEVERE, "Error when changing move method", e1);
					Alert alert = new ExceptionDialog(e1);
					alert.showAndWait();
					return;
				}
			}
			
			if (newValue != null) {
				
				if (newValue.equals(radioJunction)) {
					myRegistry.setMoveMethod(FileIO.METHOD_JUNCTION);
				} else if (newValue.equals(radioSymlink)) {
					myRegistry.setMoveMethod(FileIO.METHOD_SYMLINK);
				} else if (newValue.equals(radioRename)) {
					myRegistry.setMoveMethod(FileIO.METHOD_RENAME);
				} else {
					return;
				}
				settingsIO.saveRegistry(myRegistry); // Save
				
				if (activeProfile != null && oldValue != null) {
					
					Path newUserDataPath;
					FactorioInstallation fi = activeProfile.getFactorioInstallation();
					if (fi.isUseCustomConfigPath()
							&& fi.getCustomConfigPath() != null) {
						newUserDataPath = fi.getCustomConfigPath();
					} else {
						newUserDataPath = userDataPath;
					}
					
					try {
						fileIO.performMoveGeneral(myRegistry.getMoveMethod(),
								newUserDataPath,
								activeProfilePath);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error creating new profile folders", e);
						Alert alert = new ExceptionDialog(e);
						alert.showAndWait();
					}
					
				}
			}
		});
		
		buttonMoreInfo.setOnAction(event->{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText(null);
			String content = 
			"NTFS Junction\n"
			+ "    Link to a directory. Preferable on "
			+ "Windows systems as Windows requires elevated permission for "
			+ "symbolic link creation."
			+ "\n\n"
			+ "Symbolic links\n"
			+ "    Similar to NTFS junctions. Requires special permissions "
			+ "on Windows systems."
			+ "\n\n"
			+ "Rename directory\n"
			+ "    Move folder within a disk drive using a rename. Symlinks "
			+ "or junctions are preferable as those are links to folders "
			+ "instead of moving the folder."
			+ "\n\n"
			+ "Disabled / grayed out options means that feature is not "
			+ "available on your system, or you don't have the required "
			+ "permissions.";
			alert.setContentText(content);
			alert.getDialogPane().setPrefWidth(640);
			alert.showAndWait();
		});
		
		if (myRegistry.getMoveMethod() != null) {
			Integer intMoveMethod = myRegistry.getMoveMethod();
			if (intMoveMethod.intValue() == FileIO.METHOD_RENAME) {
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
		
		columnCustomPath.setCellValueFactory(cellFeatures -> cellFeatures.getValue().useCustomConfigPathProperty());
		columnCustomPath.setCellFactory(tableCol -> new FacUseCustomPathCell());
		columnCustomPath.setStyle("-fx-alignment: CENTER;");
		
		
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
		
		ChangeListener<FactorioInstallation> cl = (observable, oldValue, newValue) -> {
			if (newValue == null) {
				checkBoxCustomPath.setDisable(true);
				textFieldCustomPath.setText("");
			} else {
				checkBoxCustomPath.setDisable(false);
				textFieldCustomPath.setText(""+newValue.getCustomConfigPath());
				checkBoxCustomPath.setSelected(newValue.isUseCustomConfigPath());
			}
		};
		
		checkBoxCustomPath.setOnAction(event -> {
			System.out.println("Checked: "+checkBoxCustomPath.isSelected());
			FactorioInstallation fi = tableViewInstallations.getSelectionModel().getSelectedItem();
			if (fi == null) return;
			fi.setUseCustomConfigPath(
					checkBoxCustomPath.isSelected()
					);
			
			Profile p = myRegistry.getActiveProfile();
			if (p == null) return;
			
			if (p.getFactorioInstallation().equals(fi)
					&& fi.getCustomConfigPath() != null) {
				// Profile has this installation as active profile, move between global or fi
				
				Path global = myRegistry.getFactorioDataPath();
				Path custom = fi.getCustomConfigPath();
				
				Path from;
				Path to;
				
				if (checkBoxCustomPath.isSelected()) {
					from = global;
					to = custom;
				} else {
					from = custom;
					to = global;
				}
				
				try {
					fileIO.revertMoveGeneral(myRegistry.getMoveMethod(),
							from,
							p.getDirectory().toPath());
					fileIO.performMoveGeneral(myRegistry.getMoveMethod(),
							to,
							p.getDirectory().toPath());
				} catch (IOException e) {
					String errorMsg = "Error when moving profile directory to new user data directory";
					LOGGER.log(Level.SEVERE, errorMsg, e);
					Alert alert = new ExceptionDialog(e, errorMsg);
					alert.showAndWait();
				}
				
			}
		});
		
		buttonCustomPath.setOnAction(event -> {
			FactorioInstallation fi = tableViewInstallations.getSelectionModel().getSelectedItem();
			if (fi == null) return;
			
			Path previousUserDataPath;
			File initialDirectory;
			if (fi.getCustomConfigPath() != null) {
				previousUserDataPath = fi.getCustomConfigPath();
				initialDirectory = fi.getCustomConfigPath().toFile();
			} else {
				previousUserDataPath = myRegistry.getFactorioDataPath();
				initialDirectory = fi.getPath().getParent().toFile();
			}
			
			
			// First, open the dialog and get new path
			directoryChooser.setInitialDirectory(initialDirectory);
			directoryChooser.setTitle("Select new user data path");
			File newUserDataPath = directoryChooser.showDialog(settingsStage);
			if (newUserDataPath == null) return; // User cancelled
			
			Profile p = myRegistry.getActiveProfile();
			if (fi.isUseCustomConfigPath()
					&& p != null && p.getFactorioInstallation().equals(fi)) {
				try {
					// If active profile has this installation, revert move of active profile
					fileIO.revertMoveGeneral(myRegistry.getMoveMethod(),
							previousUserDataPath,
							p.getDirectory().toPath());
					
					// And move to new location
					fileIO.performMoveGeneral(myRegistry.getMoveMethod(),
							newUserDataPath.toPath(),
							p.getDirectory().toPath());
				} catch (IOException e) {
					String errorMsg = "Error when moving profile folder";
					LOGGER.log(Level.SEVERE, errorMsg, e);
					Alert alert = new ExceptionDialog(e, errorMsg);
					alert.showAndWait();
					return;
				}
			}
			
			// Finally, set installation values
			fi.setCustomConfigPath(newUserDataPath.toPath());
		});
		
		cl.changed(null, null, null); // Force a re-evaluation
		
		// Bind checkbox to if item is selected, and its value
		tableViewInstallations.getSelectionModel().selectedItemProperty().addListener(cl);
		
		// Bind custom user data path ui items to if checkbox is checked and item is selected
		buttonCustomPath.disableProperty().bind(
				tableViewInstallations.getSelectionModel().selectedIndexProperty().isEqualTo(-1)
				);
		
		textFieldCustomPath.disableProperty().bind(checkBoxCustomPath.selectedProperty().not().or(
				tableViewInstallations.getSelectionModel().selectedIndexProperty().isEqualTo(-1)
				));
		
		labelCustomPath.disableProperty().bind(checkBoxCustomPath.selectedProperty().not().or(
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
			// Let user confirm directory creation
			ButtonType btCancel = new ButtonType("Wait, let me change it",
					ButtonData.CANCEL_CLOSE);
			Alert alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.OK, btCancel);
			alert.setHeaderText(null);
			alert.setContentText(String.format("Creating profile directory\n\nThe new folder will be created at\n%s", profileDir));
			Optional<ButtonType> result = alert.showAndWait();
			
			if (result.isPresent()) {
				if (result.get() == btCancel) {
					return false;
				} else {
					fileIO.createProfilesDir(myRegistry);
				}
			} else {
				return false;
			}
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
			return false;
		}
		
		// Move files
		try {
			fileIO.performInitialSetup(myRegistry);
		} catch (FactorioProfileManagerException | IOException e) {
			String errorMsg = "Exception during initial profile transfer";
			LOGGER.log(Level.SEVERE, errorMsg, e);
			Alert alert = new ExceptionDialog(e, errorMsg
					+ "\n\nPlease close the application and verify location of mods and saves folder");
			alert.showAndWait();
			return false;
		}
		
		// This will only show if file transfer above didn't throw exception
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
