package ibrokerkit.iserviceopenidconnect.webpages.iservice;

import org.openid4java.message.AuthRequest;

public class OpenIDUtil {

	private OpenIDUtil() { }

	public static boolean isDirectedIdentity(String identity) {

		return(identity != null && identity.equals(AuthRequest.SELECT_ID));
	}
}
