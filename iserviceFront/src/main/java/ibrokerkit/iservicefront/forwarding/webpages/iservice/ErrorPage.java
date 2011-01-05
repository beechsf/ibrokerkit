package ibrokerkit.iservicefront.forwarding.webpages.iservice;

import ibrokerkit.iservicefront.components.MyVelocityPanel;
import ibrokerkit.iservicefront.forwarding.webapplication.ForwardingApplication;
import ibrokerkit.iservicefront.forwarding.webpages.BasePage;
import ibrokerkit.iservicestore.store.Forwarding;

import java.util.Properties;
import java.util.Vector;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.openxri.XRI;
import org.openxri.xml.AuthenticationService;
import org.openxri.xml.Service;
import org.openxri.xml.XRD;

public class ErrorPage extends BasePage implements IHeaderContributor {

	private static final long serialVersionUID = 9070328342622786981L;

	private Forwarding forwarding;
	private XRI qxri;
	private XRD xrd;
	private String path;

	public ErrorPage(Forwarding forwarding, XRI qxri, XRD xrd, String path) {

		this.forwarding = forwarding;
		this.qxri = qxri;
		this.xrd = xrd;
		this.path = path;

		// extend velocity map

		this.velocityMap.put("forwarding", this.forwarding);
		this.velocityMap.put("qxri", this.qxri.getAuthorityPath().toString());
		this.velocityMap.put("path", this.path);
		
		if (this.xrd != null && this.xrd.getCanonicalID() != null)
			this.velocityMap.put("inumber", xrd.getCanonicalID().getValue());

		this.addVelocity(new MyVelocityPanel("velocity", Model.valueOf(this.velocityMap)) {

			private static final long serialVersionUID = 2387469837463456L;

			@Override
			protected void addComponents() {

			}

			@Override
			protected String getFilename() {

				return("velocity/forwarding-forwardingerror.vm");
			}
		});
	}

	@Override
	protected void setHeaders(WebResponse response) {

		super.setHeaders(response);

		String xri;
		
		if (this.xrd != null && this.xrd.getCanonicalID() != null)
			xri = this.xrd.getCanonicalID().getValue();
		else
			xri = this.qxri.toString();

		response.setHeader("Link", "<http://xri2xrd.net/" + xri + ">; rel=\"lrdd\"; type=\"application/xrd+xml\"");
		response.setHeader("X-XRDS-Location", "http://xri.net/" + xri + "?_xrd_r=application/xrds+xml;sep=false");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void renderHead(IHeaderResponse response) {

		Properties properties = ((ForwardingApplication) Application.get()).getProperties();

		// check OpenID delegation

		String openidServer = null;
		String openidDelegate = null;

		if (this.xrd != null) {

			Vector<Service> services = this.xrd.getServices();

			for (Service service : services) {

				if (AuthenticationService.isInstance(service) && service.getNumLocalIDs() > 0 && service.getNumURIs() > 0) {

					openidServer = service.getURIAt(0).getUriString();
					openidDelegate = service.getLocalIDAt(0).getValue();
					break;
				}
			}
		}

		if (openidServer == null ) openidServer = properties.getProperty("authn-endpoint-url");
		if (openidDelegate == null) openidDelegate = (this.xrd != null && this.xrd.getCanonicalID() != null) ? this.xrd.getCanonicalID().getValue() : this.qxri.getAuthorityPath().toString();

		// insert OpenID delegation tags

		response.renderString("<title>" + this.qxri.getAuthorityPath().toString() + "</title>\n");
		response.renderString("<link rel=\"openid.server\" href=\"" + openidServer + "\" />\n");
		response.renderString("<link rel=\"openid2.provider\" href=\"" + openidServer + "\" />\n");
		response.renderString("<link rel=\"openid.delegate\" href=\"http://xri.net/" + openidDelegate + "\" />\n");
		response.renderString("<link rel=\"openid2.local_id\" href=\"http://xri.net/" + openidDelegate + "\" />\n");
	}
}
