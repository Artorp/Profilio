package no.artorp.profilio.utility;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DirectoryWatcher implements Runnable {
	
	public static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final boolean recursive;
	private boolean trace = false;
	
	private Path watchingDir;
	
	private final CopyOnWriteArrayList<WatcherListener> listeners = new CopyOnWriteArrayList<>();

	public DirectoryWatcher(Path dir, boolean recursive) throws IOException {
		this.watchingDir = dir;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.recursive = recursive;
		
		LOGGER.setLevel(Level.FINE);
		
		if (recursive) {
			LOGGER.info(String.format("Scanning %s ...", dir));
			registerAll(dir);
			LOGGER.info(String.format("Done scanning %s", dir));
		} else {
			register(dir);
		}
		
		// Enable trace after initial register
		this.trace = true;
	}
	
	public void addListener(WatcherListener listener) {
		this.listeners.add(listener);
	}
	
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				LOGGER.info(String.format("register %s", dir));
			} else {
				if (!dir.equals(prev)) {
					LOGGER.info(String.format("update %s -> %s", prev, dir));
				}
			}
		}
		keys.put(key, dir);
	}
	
	private void registerAll(final Path start) throws IOException {
		// Register directory and all subdirectories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>(){

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
			
		});
	}
	
	@Override
	public void run() {
		LOGGER.info("Watcher watching " + this.watchingDir);
		processEvents();
		LOGGER.info("Watcher done watching " + this.watchingDir);
	}
	
	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() {
		while (! Thread.currentThread().isInterrupted()) {
			// Wait for key to be signaled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				LOGGER.info("Watcher interrupted, exiting...");
				//e.printStackTrace();
				break;
			}
			
			Path dir = keys.get(key);
			if (dir == null) {
				LOGGER.warning("Watchkey not recognized!");
				continue;
			}
			
			for (WatchEvent<?> event : key.pollEvents()) {
				Kind<?> kind = event.kind();
				
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					// Maybe handle overflow?
					continue;
				}
				
				Path name = (Path) event.context(); // Overflow skipped above, will always be path
				Path child = dir.resolve(name);
				
				// Print out event
				LOGGER.fine(String.format("%s: %s", event.kind().name(), child));
				
				
				// If directory is created and watching recursively,
				// register it and it's subdirectories
				if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
							register(child);
						}
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error registering dir "+child, e);
					}
				}
				
				// Send events to listeners
				for (WatcherListener listener : listeners) {
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						listener.fileCreated(child);
					} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
						listener.fileDeleted(child);
					} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						listener.fileModified(child);
					}
				}
				
			}
			
			// Reset key and remove from watchlist if dir no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);
				
				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
			
		}
	}

}
