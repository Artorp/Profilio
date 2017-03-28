package no.artorp.profilio.utility;

import java.nio.file.Path;

public interface WatcherListener {

	public void fileDeleted(Path fileDeleted);
	public void fileCreated(Path fileCreated);
	public void fileModified(Path fileModified);
}
