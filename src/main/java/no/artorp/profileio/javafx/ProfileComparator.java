package no.artorp.profileio.javafx;

import java.util.Comparator;

public class ProfileComparator implements Comparator<Profile> {

	@Override
	public int compare(Profile o1, Profile o2) {
		if (o1.isDirectory() == o2.isDirectory()) {
			// Both are filer or both are directories? Compare using filename
			return o1.getName().compareToIgnoreCase(o2.getName());
		} else {
			// Sort directories before files
			return Boolean.compare(o2.isDirectory(), o1.isDirectory());
		}
	}


}
