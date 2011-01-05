package ibrokerkit.oauthfront.components;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;

public abstract class MyVelocityPanel extends Panel implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {

	private static final long serialVersionUID = 146146184117434L;

	private transient String stackTraceAsString;
	private transient String evaluatedTemplate;

	public MyVelocityPanel(String id, IModel model) {

		super(id, model);

		// create and add components

		this.addComponents();
	}

	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

		if (! Strings.isEmpty(stackTraceAsString)) {

			replaceComponentTagBody(markupStream, openTag, Strings.toMultilineMarkup(stackTraceAsString));
		} else if (! parseGeneratedMarkup()) {

			if (size() > 0) throw new WicketRuntimeException("Components cannot be added if the generated markup should not be parsed.");

			if (evaluatedTemplate == null) getMarkupResourceStream(null, null);
			replaceComponentTagBody(markupStream, openTag, evaluatedTemplate);
		} else {

			super.onComponentTagBody(markupStream, openTag);
		}
	}

	private void onException(final Exception exception) {

		if (! throwVelocityExceptions())
			stackTraceAsString = Strings.toString(exception);
		else
			throw new WicketRuntimeException(exception);
	}

	protected boolean escapeHtml() {

		return(false);
	}

	private String evaluateVelocityTemplate(Reader templateReader) {

		if (evaluatedTemplate == null) {

			final Map<?, ?> map = (Map<?, ?>)getModelObject();

			final VelocityContext ctx = new VelocityContext(map);

			StringWriter writer = new StringWriter();

			final String logTag = getId();

			try {

				Velocity.evaluate(ctx, writer, logTag, templateReader);
				
				evaluatedTemplate = writer.toString();
				if (escapeHtml()) evaluatedTemplate = Strings.escapeMarkup(evaluatedTemplate).toString();

				return(evaluatedTemplate);
			} catch (Exception ex) {

				onException(ex);
			}

			return(null);
		}

		return(evaluatedTemplate);
	}

	@SuppressWarnings("unchecked")
	public final IResourceStream getMarkupResourceStream(MarkupContainer container, Class containerClass) {

		AbstractResourceStream templateStream = this.getTemplateStream();
		
		Reader reader = new StringReader(templateStream.asString());
		if (reader == null) throw new WicketRuntimeException("could not find velocity template for panel: " + this);

		StringBuffer buffer = new StringBuffer();
		buffer.append("<wicket:panel>");
		buffer.append(evaluateVelocityTemplate(reader));
		buffer.append("</wicket:panel>");

		StringResourceStream markupStream = new StringResourceStream(buffer.toString());
		markupStream.setCharset(Charset.forName((String) Velocity.getProperty("output.encoding")));
		return(markupStream);
	}

	@SuppressWarnings("unchecked")
	public final String getCacheKey(MarkupContainer container, Class containerClass) {

		return(null);
	}

	protected void onDetach() {

		super.onDetach();
		stackTraceAsString = null;
		evaluatedTemplate = null;
	}

	protected boolean parseGeneratedMarkup() {

		return(true);
	}

	protected boolean throwVelocityExceptions() {

		return(false);
	}

	protected AbstractResourceStream getTemplateStream() {

		String filename = this.getFilename();
		String path = ((WebApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath(filename);
		FileResourceStream templateStream = new FileResourceStream(new File(path));
		templateStream.setCharset(Charset.forName((String) Velocity.getProperty("input.encoding")));
		return(templateStream);
	}

	protected abstract void addComponents();

	protected abstract String getFilename();
}
