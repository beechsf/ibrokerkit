package ibrokerkit.epptools4java;

import com.neulevel.epp.core.response.EppResult;

public class EppToolsUnsuccessfulException extends EppToolsException {

	private static final long serialVersionUID = 5263442184599779212L;

	private EppResult eppResult;
	
	public EppToolsUnsuccessfulException(EppResult eppResult) {

		super();
		
		this.eppResult = eppResult;
	}

	public EppToolsUnsuccessfulException(String message, Throwable ex, EppResult eppResult) {

		super(message, ex);
		
		this.eppResult = eppResult;
	}

	public EppToolsUnsuccessfulException(String message, EppResult eppResult) {

		super(message);
		
		this.eppResult = eppResult;
	}

	public EppToolsUnsuccessfulException(Throwable ex, EppResult eppResult) {

		super(ex);
		
		this.eppResult = eppResult;
	}
	
	public EppResult getResult() {
		
		return(this.eppResult);
	}
}
