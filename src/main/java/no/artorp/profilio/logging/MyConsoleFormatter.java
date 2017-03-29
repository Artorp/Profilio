package no.artorp.profilio.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MyConsoleFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		
		String line = String.format("%12s %9s %s\n",
				"["+Thread.currentThread().getName()+"]",
				"["+record.getLevel()+"]",
				formatMessage(record)
				);
		
		Throwable e = record.getThrown();
		
		if (e != null) {
			StringWriter sw = new StringWriter();
			try (PrintWriter pw = new PrintWriter(sw)) {
				e.printStackTrace(pw);
			}
			line = line + sw.toString();
		}
		
		return line;
	}

}
