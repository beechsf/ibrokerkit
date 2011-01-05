package ibrokerkit.iserviceadmin.webpages;

import org.apache.wicket.Application;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public abstract class BasePage extends WebPage {

	private Label titleLabel;

	public BasePage() {

		// add css
		
		this.add(HeaderContributor.forCss("style.css", "screen"));

		// create and add components

		this.titleLabel = new Label("titleLabel", this.getClass().getName());

		this.add(new BookmarkablePageLink("homePageLink", Application.get().getHomePage()));
		this.add(this.titleLabel);
		this.add(new BasePageMenuPanel("menuPanel"));
		this.add(new FeedbackPanel("feedbackPanel"));
	}

	protected void setTitle(String title) {

		this.titleLabel.setModelObject(title);
	}
}
