package ibrokerkit.iservicefront.components;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;

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
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.string.Strings;

public abstract class MyVelocityPanel extends Panel implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {

	private static final long serialVersionUID = 146146184117434L;

	private transient String stackTraceAsString;
	private transient String evaluatedTemplate;

	public MyVelocityPanel(String id, IModel<HashMap<String, Object>> model) {

		super(id, model);

		// create and add components

		this.addComponents();
	}

	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

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

	@SuppressWarnings("unchecked")
	private String evaluateVelocityTemplate(Reader templateReader) {

		if (evaluatedTemplate == null) {

			final HashMap<String, Object> map = (HashMap<String, Object>) this.getDefaultModelObject();

			final VelocityContext ctx = new VelocityContext(map);

			StringWriter writer = new StringWriter();

			final String logTag = getId();

			try {

				Velocity.setProperty("input.encoding", "UTF-8");
				Velocity.setProperty("output.encoding", "UTF-8");
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

	public final IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {

		Reader templateReader;
		StringBuffer buffer;

		try {

			templateReader = this.getTemplateReader();

			buffer = new StringBuffer();
			buffer.append("<wicket:panel>");
			buffer.append(evaluateVelocityTemplate(templateReader));
			buffer.append("</wicket:panel>");
		} catch (Exception ex) {

			throw new RuntimeException(ex);
		}

		StringResourceStream markupStream = new StringResourceStream(buffer.toString());
		markupStream.setCharset(Charset.forName("UTF-8"));

		return(markupStream);
	}

	@Override
	public final String getCacheKey(MarkupContainer container, Class<?> containerClass) {

		return(null);
	}

	@Override
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

	protected Reader getTemplateReader() throws IOException {

		String file = this.getFilename();
		if (file == null) throw new NullPointerException();

		String path = ((WebApplication) this.getApplication()).getWicketFilter().getFilterConfig().getServletContext().getRealPath(file);
		if (path == null) throw new NullPointerException();

		return(new FileReader(new File(path)));
	}

	protected abstract void addComponents();

	protected abstract String getFilename();
}
