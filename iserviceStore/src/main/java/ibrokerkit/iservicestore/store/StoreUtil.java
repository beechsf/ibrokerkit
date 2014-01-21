package ibrokerkit.iservicestore.store;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility methods for the Store.
 */
public class StoreUtil {

	public static String makeXriPass(String claimedPass) {

		String xriSalt = UUID.randomUUID().toString();
		String xriPass = xriSaltedHash(xriSalt + xriSaltedHash(claimedPass));

		return("xri--" + xriSalt + "--" + xriPass);
	}

	public static boolean checkPass(String pass, String claimedPass) {

		if (pass.startsWith("xri--")) {

			return(checkXriPass(pass, claimedPass));
		} else if (pass.startsWith("linksafe--")) {

			return(checkLinksafePass(pass, claimedPass));
		} else {

			return(checkOldHashPass(pass, claimedPass));
		}
	}

	private static boolean checkXriPass(String pass, String claimedPass) {

		Matcher matcher = Pattern.compile("^xri--(.+?)--(.+?)$").matcher(pass);
		if (! matcher.matches()) return(false);
		String xriSalt = matcher.group(1);
		String xriPass = matcher.group(2);
		String xriClaimedPass = xriSaltedHash(xriSalt + xriSaltedHash(claimedPass));

		return(xriPass.equals(xriClaimedPass));
	}

	private static String xriSaltedHash(String string) {

		return(DigestUtils.shaHex("@#XX$XRI.XDI" + "--" + string + "--"));
	}

	private static boolean checkLinksafePass(String pass, String claimedPass) {

		Matcher matcher = Pattern.compile("^linksafe--(.+?)--(.+?)$").matcher(pass);
		if (! matcher.matches()) return(false);
		String linksafeSalt = matcher.group(1);
		String linksafePass = matcher.group(2);
		String linksafeClaimedPass = linksafeSaltedHash(linksafeSalt + linksafeSaltedHash(claimedPass));

		return(linksafePass.equals(linksafeClaimedPass));
	}

	private static String linksafeSaltedHash(String string) {

		return(DigestUtils.shaHex("@#AF$RQETQEE" + "--" + string + "--"));
	}

	private static boolean checkOldHashPass(String pass, String claimedPass) {

		return(pass.equals(Base64.encodeBase64String(DigestUtils.sha(claimedPass))));
	}
}
