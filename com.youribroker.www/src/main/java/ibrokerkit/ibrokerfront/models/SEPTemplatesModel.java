package ibrokerkit.ibrokerfront.models;

import ibrokerkit.iservicestore.sep.LocatorService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.openxri.resolve.TrustType;
import org.openxri.xml.AuthenticationService;
import org.openxri.xml.AuthorityResolutionService;
import org.openxri.xml.ContactService;
import org.openxri.xml.DefaultService;
import org.openxri.xml.ForwardingService;
import org.openxri.xml.ProxyResolutionService;
import org.openxri.xml.SEPUri;
import org.openxri.xml.Service;
import org.openxri.xml.XDIService;

public class SEPTemplatesModel extends AbstractReadOnlyModel implements IChoiceRenderer {

	private static final long serialVersionUID = -6512545789304813529L;

	public static List<Service> list;
	public static String[] names;

	static {

		try {

			list = Arrays.asList(new Service[] {
					new Service(),
					new DefaultService(new URI("http://__mydefaultendpoint__")),
					new AuthorityResolutionService(new URI("http://__myauthorityresolver__"), "__myproviderid__", new TrustType(TrustType.TRUST_NONE), SEPUri.APPEND_NONE),
					new ProxyResolutionService(new URI[] { new URI("http://__myproxyserver__"), new URI("https://__mysecureproxyserver__") }, "__myproviderid__", new TrustType(TrustType.TRUST_NONE), Boolean.TRUE, Boolean.TRUE),
					new AuthenticationService(new URI[] { new URI("http://__myauthenticationprovider__"), new URI("https://__mysecureauthenticationprovider__") }, "__myproviderid__", null, true),
					new ContactService(new URI("http://__mycontactprovider__"), "__myproviderid__", false),
					new ForwardingService(new URI("http://__myforwardingprovider__"), "__myproviderid__", false, true),
					new LocatorService(new URI("http://__mylocatorprovider__"), "__myproviderid__", false),
					new XDIService(new URI("http://__myxdiendpoint__"), "__myproviderid__")
			});

			names = new String[] {
					"Blank SEP",
					"Default SEP",
					"Authority Resolution SEP",
					"Proxy Resolution SEP",
					"Authentication SEP",
					"Contact SEP",
					"Forwarding SEP",
					"Locator SEP",
					"XDI SEP"
			};
		} catch (Exception ex) {

			list = null;
			names = null;
		}
	}

	public SEPTemplatesModel() {

	}

	@Override
	public Object getObject() {

		return(list);
	}

	public Object getDisplayValue(Object object) {

		return(names[list.indexOf(object)]);
	}

	public String getIdValue(Object object, int index) {

		return(Integer.toString(index));
	}
}
