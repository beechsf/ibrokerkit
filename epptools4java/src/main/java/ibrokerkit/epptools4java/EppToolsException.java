package ibrokerkit.epptools4java;

import java.util.List;

import com.neulevel.epp.core.response.EppResult;

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

	@SuppressWarnings("unchecked")
	public static EppToolsUnsuccessfulException makeEppToolsUnsuccessfulException(EppResult eppResult) {

		if (eppResult == null) return(new EppToolsUnsuccessfulException("Unsuccessful.", eppResult));

		StringBuffer buffer = new StringBuffer();
		buffer.append("Unsuccessful: ");
		if (eppResult.getMessage() != null) buffer.append(eppResult.getMessage().getMessage());
		for (String value : (List<String>) eppResult.getValue()) buffer.append(" / " + value);

		return(new EppToolsUnsuccessfulException(buffer.toString(), eppResult));
	}
}
