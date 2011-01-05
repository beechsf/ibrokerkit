package ibrokerkit.ibrokerfront.webpages.xri.transfer;

import ibrokerkit.epptools4java.EppTools;
import ibrokerkit.ibrokerfront.components.openid.OpenIDPanel;
import ibrokerkit.ibrokerfront.webapplication.IbrokerApplication;
import ibrokerkit.ibrokerfront.webpages.BasePage;
import ibrokerkit.iname4java.store.Xri;
import ibrokerkit.iname4java.store.XriStore;
import ibrokerkit.iname4java.store.impl.grs.GrsXri;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.neulevel.epp.xri.EppXriAuthority;

public class TransferXRIOut extends BasePage {

	private static final long serialVersionUID = -3451522127828390121L;

	private static Log log = LogFactory.getLog(TransferXRIOut.class.getName());

	// form data

	private String openid;
	private String token;

	public TransferXRIOut() {

		this.setTitle(this.getString("title"));

		// create and add components

		this.add(new MyForm("form", new CompoundPropertyModel(this)));
	}

	private class MyForm extends Form {

		// form components

		private static final long serialVersionUID = -1038093122855907816L;

		private OpenIDPanel openidPanel;
		private TextField tokenTextField;
		private SubmitButton approveButton;
		private SubmitButton rejectButton;

		private MyForm(String id, IModel model) {

			super(id, model);

			// create components

			this.openidPanel = new OpenIDPanel("openid");
			this.openidPanel.setRequired(true);
			this.tokenTextField = new TextField("token");
			this.tokenTextField.setLabel(new Model("Transfer Token"));
			this.tokenTextField.setRequired(true);
			this.approveButton = new SubmitButton("approve", true);
			this.rejectButton = new SubmitButton("reject", false);

			// add components

			this.add(this.openidPanel);
			this.add(this.tokenTextField);
			this.add(this.approveButton);
			this.add(this.rejectButton);
		}

		private class SubmitButton extends Button {

			private static final long serialVersionUID = -5618465237448994700L;

			private boolean approve;

			private SubmitButton(String id, boolean approve) {

				super(id);

				this.approve = approve;
			}

			@Override
			public void onSubmit() {

				EppTools eppTools = ((IbrokerApplication) this.getApplication()).getEppTools();
				XriStore xriStore = ((IbrokerApplication) this.getApplication()).getXriStore();

				// check if sender is authenticated

				if (! MyForm.this.openidPanel.isAuthenticated()) {

					TransferXRIOut.this.error(TransferXRIOut.this.getString("authenticate"));
					return;
				}

				// look up i-name in store

				Xri xri;

				try {

					xri = xriStore.findXri(TransferXRIOut.this.openid);
				} catch (Exception ex) {

					xri = null;
				}

				if (xri == null || ! (xri instanceof GrsXri)) {

					log.info("XRI " + TransferXRIOut.this.openid + " not found for Transfer Out operation.");
					this.error(TransferXRIOut.this.getString("notfound"));
					return;
				}

				// issue the approve/reject operation

				char gcs = TransferXRIOut.this.openid.charAt(0);
				String grsAuthorityId = ((GrsXri) xri).getAuthorityId();

				try {

					EppXriAuthority eppXriAuthority = eppTools.infoAuthority(gcs, grsAuthorityId, false);

					if (eppXriAuthority.getStatus().get(0).equals(EppXriAuthority.STATUS_PENDING_TRANSFER)) {

						if (this.approve) {

							xriStore.transferAuthorityOutApprove(xri, TransferXRIOut.this.token);
						} else {

							xriStore.transferAuthorityOutReject(xri, TransferXRIOut.this.token);
						}
					} else if (eppXriAuthority.getStatus().get(0).equals(EppXriAuthority.STATUS_PENDING_INAME_TRANSFER)) {

						if (this.approve) {

							xriStore.transferXriOutApprove(xri, TransferXRIOut.this.token);
						} else {

							xriStore.transferXriOutReject(xri, TransferXRIOut.this.token);
						}
					} else {

						log.info("No pending transfer for XRI " + TransferXRIOut.this.openid + " for Transfer Out operation.");
						this.error(TransferXRIOut.this.getString("notransfer"));
						return;
					}
				} catch (Exception ex) {

					log.warn("Problem while executing Transfer Out operation on i-name " + TransferXRIOut.this.openid + " with token " + TransferXRIOut.this.token + ".", ex);
					this.error(TransferXRIOut.this.getString("fail") + " " + ex.getLocalizedMessage());
					return;
				}

				log.info("Transfer Out operation for " + TransferXRIOut.this.openid + " completed with token " + TransferXRIOut.this.token + " (approve=" + this.approve + ").");
				this.info(TransferXRIOut.this.getString("success"));
			}
		}
	}

	public String getOpenid() {
		return (this.openid);
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getToken() {
		return (this.token);
	}
	public void setToken(String token) {
		this.token = token;
	}
}