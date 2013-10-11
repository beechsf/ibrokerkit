package com.ibrokerkit.webpages;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public abstract class BasePage extends WebPage {

	private static final long serialVersionUID = -6288743341707441458L;

	private Label titleLabel;

	public BasePage() {

		// create and add components

		this.titleLabel = new Label("titleLabel", this.getClass().getName());

		this.add(new BookmarkablePageLink<String> ("homePageLink", Application.get().getHomePage()));
		this.add(new BasePageSidePanel("sidePanel"));
		this.add(this.titleLabel);
		this.add(new FeedbackPanel("feedbackPanel"));
	}

	protected void setTitle(String title) {

		this.titleLabel.setDefaultModelObject(title);
	}

	@Override
	public void renderHead(IHeaderResponse headerResponse) {

		super.renderHead(headerResponse);

		headerResponse.render(CssHeaderItem.forUrl("/style.css", "screen"));
		headerResponse.render(CssHeaderItem.forUrl("/style-print.css", "print"));
	}

	protected String getPageStats() {

		StringBuffer result = new StringBuffer();

		result.append("Size in Bytes: " + this.getPage().getSizeInBytes() + " / ");

		return(result.toString());
	}
}
