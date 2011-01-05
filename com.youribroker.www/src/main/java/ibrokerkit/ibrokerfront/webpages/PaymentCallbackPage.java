package ibrokerkit.ibrokerfront.webpages;

import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.index.grs.DoRegister;

import java.security.MessageDigest;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.WebRequest;


public abstract class PaymentCallbackPage extends BasePage {

	private static Log log = LogFactory.getLog(DoRegister.class.getName());

	public PaymentCallbackPage(PageParameters pageParameters, Class<? extends Page> errorPageClass, boolean verify, int minTotal) {

		if (! verify) return;

		Properties properties = ((IbrokerApplication) this.getApplication()).getProperties();

		// get passed-in parameters

		String demo = pageParameters.getString("demo");
		String total = pageParameters.getString("total");
		String key = pageParameters.getString("key");
		String orderNumber = pageParameters.getString("order_number");
		String merchantProductId = pageParameters.getString("merchant_product_id");

		if (demo == null || total == null || key == null || orderNumber == null) throw new IllegalArgumentException("Missing parameter(s).");

		log.info("Order " + orderNumber + " for product " + merchantProductId + " with total " + total + " (demo=" + demo + ")");
		log.debug("Parameters are: " + pageParameters.toString());

		// verify demo-mode

		boolean acceptDemo = Boolean.parseBoolean(properties.getProperty("payment-accept-demo"));

		if ("Y".equalsIgnoreCase(demo) && ! acceptDemo) {

			log.error("Invalid Demo: " + demo);
			throw new RuntimeException("Invalid access.");
		}

		// verify total

		if (Integer.parseInt(total.substring(0, total.indexOf('.'))) < minTotal) {

			log.error("Total " + total + " is lower than MinTotal " + minTotal);

			throw new RuntimeException("Invalid access.");
		}

		// verify referer

		String pattern = ((IbrokerApplication) this.getApplication()).getProperties().getProperty("payment-referer-pattern");
		String referer = ((WebRequest) this.getRequest()).getHttpServletRequest().getHeader("referer");
		if (referer == null) referer = "";

		if (referer == null || ! Pattern.matches(pattern, referer)) {

			log.error("Invalid Referer: " + referer);
			throw new RuntimeException("Invalid access.");
		}

		// verify hash

		MessageDigest digest;
		String hash;

		try {

			String secret = ((IbrokerApplication) this.getApplication()).getProperties().getProperty("payment-secret");
			String vendorNumber = ((IbrokerApplication) this.getApplication()).getProperties().getProperty("payment-vendor-number");

			digest = MessageDigest.getInstance("MD5");

			if ("Y".equalsIgnoreCase(demo)) {

				digest.update((secret + vendorNumber + "1" + total).getBytes("UTF-8"));
			} else {

				digest.update((secret + vendorNumber + orderNumber + total).getBytes("UTF-8"));
			}

			hash = new String(Hex.encodeHex(digest.digest()));
		} catch (Exception ex) {

			hash = null;
		}

		if (hash == null || ! hash.equalsIgnoreCase(key)) {

			log.error("Invalid Hash: " + hash + " (expected: " + key + ")");
			throw new RuntimeException("Invalid access.");
		}

		// verify credit card

		if (! "Y".equalsIgnoreCase(pageParameters.getString("credit_card_processed"))) {

			log.error("Credit card not processed for order " + orderNumber);

			Page page;

			try {

				page = errorPageClass.newInstance();
			} catch (Exception ex) {

				throw new RuntimeException(this.getString("creditcardfail"));
			}

			page.error(this.getString("creditcardfail"));
			this.setResponsePage(page);
			return;
		}
	}
}
