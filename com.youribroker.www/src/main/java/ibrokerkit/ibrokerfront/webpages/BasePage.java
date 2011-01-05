package ibrokerkit.ibrokerfront.webpages;

import ibrokerkit.ibrokerfront.webapplication.IbrokerSession;
import ibrokerkit.ibrokerfront.webpages.information.Policies;
import ibrokerkit.ibrokerfront.webpages.information.TermsOfUse;
import ibrokerkit.ibrokerstore.store.User;

import org.apache.wicket.Application;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public abstract class BasePage extends WebPage {

	private Label titleLabel;

	public BasePage() {

		User user = ((IbrokerSession) this.getSession()).getUser();

		// add css
		
		this.add(HeaderContributor.forCss("style.css", "screen"));
		this.add(HeaderContributor.forCss("style-print.css", "print"));
		
		// create and add components

		this.titleLabel = new Label("titleLabel", this.getClass().getName());

		this.add(new BookmarkablePageLink("homePageLink", Application.get().getHomePage()));
		if (user == null) {

			this.add(new BasePageLoginPanel("userPanel"));
		} else {

			this.add(new BasePageLogoutPanel("userPanel"));
		}
		this.add(new BasePageSidePanel("sidePanel"));
		this.add(this.titleLabel);
		this.add(new FeedbackPanel("feedbackPanel"));
		this.add(new BookmarkablePageLink("TermsOfUseLink", TermsOfUse.class));
		this.add(new BookmarkablePageLink("PoliciesLink", Policies.class));
	}

	protected void setTitle(String title) {

		this.titleLabel.setModelObject(title);
	}

	protected String getPageStats() {

		StringBuffer result = new StringBuffer();

		result.append("Size in Bytes: " + this.getPage().getSizeInBytes() + " / ");
		result.append("Page Map ID:" + + this.getPageMapEntry().getNumericId());

		return(result.toString());
	}
}
