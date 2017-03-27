package no.artorp.profileio;

public class Main {

	/**
	 * App is extending Application, using a main class that
	 * extends a class will result in an obscure error message
	 * "Could not find or load main class [package].[class]"
	 * if the superclass can't be resolved
	 * <p>
	 * Some installations of Linux doesn't include JavaFX by
	 * default with OpenJDK
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		App.main_proxy(args);
	}

}
