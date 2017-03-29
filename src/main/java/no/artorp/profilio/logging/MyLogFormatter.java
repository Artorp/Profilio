package no.artorp.profilio.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyLogFormatter extends Formatter {
	
	@Override
	public String format(LogRecord record) {
		String time = (new SimpleDateFormat("HH:mm")
				.format(new java.util.Date(record.getMillis())) );
		
		String line = String.format("[%s] %12s %9s %s\n",
				time,
				"[" + Thread.currentThread().getName() + "]",
				"[" + record.getLevel() + "]",
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

	@Override
	public String getHead(Handler h) {
		
		String time = this.calcDate(System.currentTimeMillis());
		
		return time + " log start\n";
	}

	@Override
	public String getTail(Handler h) {
		return this.calcDate(System.currentTimeMillis()) + " Farewell\n";
	}
	
	private String calcDate(long time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startTime = new Date(time);
		return dateFormat.format(startTime);
	}
	
}
