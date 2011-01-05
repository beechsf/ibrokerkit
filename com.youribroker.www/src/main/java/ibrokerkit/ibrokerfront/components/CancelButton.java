package ibrokerkit.ibrokerfront.components;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;

public class CancelButton extends Button {

	private static final long serialVersionUID = -4748912785527926083L;

	private Class<? extends Page> cls;
	private Page page;
	private PageParameters parameters;
	
	public CancelButton(String id, Class<? extends Page> cls) {
		
		super(id);

		this.setDefaultFormProcessing(false);
		
		this.cls = cls;
	}
	
	public CancelButton(String id, Class<? extends Page> cls, PageParameters parameters) {
		
		super(id);

		this.setDefaultFormProcessing(false);
		
		this.cls = cls;
		this.parameters = parameters;
	}
	
	public CancelButton(String id, Page page) {
		
		super(id);

		this.setDefaultFormProcessing(false);
		
		this.page = page;
	}
	
	@Override
	public void onSubmit() {
	
		if (this.cls != null) {

			if (this.parameters != null) {

				this.setResponsePage(this.cls, this.parameters);
			} else {
				
				this.setResponsePage(this.cls);
			}
		} else {
			
			this.setResponsePage(this.page);
		}
	}
}
