package ibrokerkit.xritools4java.types;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ResultType implements Serializable {

	private static final long serialVersionUID = -4582890549465233640L;

	public static final ResultType URILIST = new ResultType();
	public static final ResultType XRD = new ResultType();
	public static final ResultType XRDS = new ResultType();

	public static ResultType[] asArray() {
		
		return(new ResultType[] { URILIST, XRD, XRDS});
	}

	public static List<ResultType> asList() {
		
		return(Arrays.asList(asArray()));
	}

	public static ResultType parse(String str) {
		
		if (str.equalsIgnoreCase("URI List") ||
				str.equalsIgnoreCase("URILIST") ||
				str.equalsIgnoreCase("U")) return(URILIST);
		
		if (str.equalsIgnoreCase("XRD")) return(XRD);
		
		if (str.equalsIgnoreCase("XRDS")) return(XRDS);
		
		return(null);
	}
	
	@Override
	public boolean equals(Object object) {
		
		if (object == this) return(true);
		if (object == null) return(false);

		return(false);
	}
	
	@Override
	public int hashCode() {
		
		return(super.hashCode());
	}

	@Override
	public String toString() {
		
		if (this == URILIST) return("URI List");
		if (this == XRD) return("XRD");
		if (this == XRDS) return("XRDS");
		
		return(null);
	}
}
