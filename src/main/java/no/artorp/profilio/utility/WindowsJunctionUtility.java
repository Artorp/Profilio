package no.artorp.profilio.utility;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WindowsJunctionUtility {
	
	public static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
    public static final String D_LINK = "/D";
    public static final String H_LINK = "/H";
    public static final String J_LINK = "/J";
    public static final String REM_LINK = "rmdir"; // Don't use DEL to delete junction, it will delete target folder too
    
    private String command, flag;
    private Path link, target;
    private List<String> commands = Arrays.asList(D_LINK, H_LINK, J_LINK, REM_LINK);

	public WindowsJunctionUtility() {
		this.command = this.flag = "";
		this.link = this.target = null;
	}
	
	
	/**
	 * Creates a directory junction at the link
	 * @param link the new junction link
	 * @param target the target of the junction link
	 * @return {@code true} if successful, {@code false} if unsuccessful
	 * @throws IOException 
	 * @see http://ss64.com/nt/mklink.html
	 */
	public boolean createJunction(Path link, Path target) throws IOException {
		return createLink(J_LINK, link, target);
	}
	
	/**
	 * Creates a {@code mklink} link
	 * <p>
	 * {@link #createJunction(Path, Path) createJunction} passes in the junction flag
	 * @param flag the flag for mklink
	 * @param link the new junction link
	 * @param target the target of the junction link
	 * @return {@code true} if successful, {@code false} if unsuccessful
	 * @see http://ss64.com/nt/mklink.html
	 */
	public boolean createLink(String flag, Path link, Path target) throws IOException {
		if (! this.commands.contains(flag)) {
			LOGGER.warning(String.format("%s is not a valid command", flag));
			return false;
		}
		
		this.command = "mklink";
		this.flag = flag;
		this.link = link.toAbsolutePath();
		this.target = target.toAbsolutePath();
		
		boolean targetable = false;
		
		try {
			targetable = (target.toFile().isDirectory() || FileIO.isJunctionOrSymlink(target));
		} catch (NoSuchFileException e) {
			// If the target can't be resolved, no link should be made
			if (target.toFile().exists() || link.toFile().exists()) {
				targetable = false;
			}
		}
		
		if (! targetable) {
			throw new NotDirectoryException(target.toString());
		}
		
		if (link.toFile().exists()) {
			throw new FileAlreadyExistsException("Cannot create a file if it already exists:\n"+link);
		}
		
		if (runProcess() == 0) {
			return true;
		}
		
		return false;
	}
	
	private int runProcess() {
		Process process = null;
		int exitVal = -1;
		
		try {
			ProcessBuilder pb = new ProcessBuilder("cmd", "/C", this.command, this.flag,
					this.link.toString(), this.target.toString());
			//pb.redirectErrorStream(true);
			process = pb.start();
			exitVal = process.waitFor();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "There was an exception when starting CMD", e);
		}
		
		return exitVal;
	}
	

	
	/**
	 * Tests junction creation on home folder
	 * <p>
	 * Home folder denoted by {@code System.getProperty("user.home")}
	 * @return {@code true} if junction creation is permitted,
	 * {@code false} if test failed
	 */
	public boolean testJunctionPermissions() {
		Path homeFolder = Paths.get(System.getProperty("user.home")).toAbsolutePath();
		
		Path fromDir = homeFolder.resolve("tempFrom");
		Path toDir = homeFolder.resolve("tempTo");
		
		if (!fromDir.toFile().exists()) {
			fromDir.toFile().mkdir();
		}
		Integer exitValue;
		String[] commands = {"CMD", "/C", "mklink", "/J", toDir.toString(), fromDir.toString()};
		try {
			Process createProcess = Runtime.getRuntime().exec(commands);
			if (createProcess.waitFor(5, TimeUnit.SECONDS)) {
				exitValue = createProcess.exitValue();
			} else {
				return false;
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.log(Level.SEVERE, "There was an exception when creating junction link", e);
			return false;
		}
		
		if (exitValue != null && exitValue.intValue() == 0) {
			if (toDir.toFile().exists()) {
				try {
					String[] cmd_1 = {"CMD", "/C", "rmdir", toDir.toString()};
					Runtime.getRuntime().exec(cmd_1);
					String[] cmd_2 = {"CMD", "/C", "rmdir", fromDir.toString()};
					Runtime.getRuntime().exec(cmd_2);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "There was an exception when removing junction link", e);
				}
				return true;
			}
		}
		
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		
		// Test utility
		
		WindowsJunctionUtility win = new WindowsJunctionUtility();
		System.out.println(win.testJunctionPermissions());
		
		Path home = Paths.get(System.getProperty("user.home"));
		Path fromDir = home.resolve("test");
		Path toDir = home.resolve("test 2");
		
		System.out.println("Created: "+win.createJunction(toDir, fromDir));
		
		System.out.println("Is considered symbolic: "+Files.isSymbolicLink(toDir));
		
		try {
			Files.deleteIfExists(toDir); // Deletes link, but not target
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
