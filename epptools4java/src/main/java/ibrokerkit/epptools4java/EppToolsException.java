package ibrokerkit.epptools4java;

public class EppToolsException extends Exception {

	private static final long serialVersionUID = 2753798775094326912L;

	public EppToolsException() {

		super();
	}

	public EppToolsException(String message) {

		super(message);
	}

	public EppToolsException(Throwable ex) {

		super(ex);
	}

	public EppToolsException(String message, Throwable ex) {

		super(message, ex);
	}
}
