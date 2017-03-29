package no.artorp.profilio.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyConsoleHandler extends Handler {

	@Override
	public void publish(LogRecord record) {
		if (getFormatter() == null) {
			setFormatter(new MyConsoleFormatter());
		}
		
		String message = getFormatter().format(record);
		
		if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
			System.err.print(message);
		} else {
			System.out.print(message);
		}
	}
	
	@Override
	public void flush() {
		// not doing anything
	}

	@Override
	public void close() throws SecurityException {
		// Not doing anything
	}

}
