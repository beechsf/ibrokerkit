package ibrokerkit.iservicefront.velocity;

import java.io.Serializable;

public class Encoder implements Serializable {

	private static final long serialVersionUID = -3328425805820066497L;

	private static Encoder instance = null;

	private Encoder() { }

	public static Encoder getInstance() {

		if (instance == null) instance = new Encoder();

		return(instance);
	}

	public String html(String string) {

		return(html(string, false));
	}

	public String html(String string, boolean encodeTags) {

		if (string == null) return(null);

		String newString = string;

		if (encodeTags) {

			newString = newString
			.replaceAll("<", "&lt;")
			.replaceAll(">", "&gt;");
		}

		newString = newString
		.replaceAll("\r\n", "<br>")
		.replaceAll("\n", "<br>")
		.replaceAll("\t", "&nbsp;&nbsp;");

		return(newString);
	}
}
