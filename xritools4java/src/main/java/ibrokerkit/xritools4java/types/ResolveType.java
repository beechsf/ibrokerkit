package ibrokerkit.xritools4java.types;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ResolveType implements Serializable {

	private static final long serialVersionUID = -4040884280117388590L;

	public static final ResolveType SEP = new ResolveType();
	public static final ResolveType AUTH = new ResolveType();

	public static ResolveType[] asArray() {

		return(new ResolveType[] { SEP, AUTH });
	}

	public static List<ResolveType> asList() {

		return(Arrays.asList(asArray()));
	}

	public static ResolveType parse(String str) {

		if (str.equalsIgnoreCase("Service Endpoint") ||
				str.equalsIgnoreCase("SEP") ||
				str.equalsIgnoreCase("S")) return(SEP);
		
		if (str.equalsIgnoreCase("Authority") ||
				str.equalsIgnoreCase("Auth") ||
				str.equalsIgnoreCase("A")) return(AUTH);
		
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

		if (this == SEP) return("Service Endpoint");
		if (this == AUTH) return("Authority");

		return(null);
	}
}
