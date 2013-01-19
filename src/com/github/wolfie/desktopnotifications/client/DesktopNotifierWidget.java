package com.github.wolfie.desktopnotifications.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.VConsole;
import com.vaadin.client.ui.VOverlay;

public class DesktopNotifierWidget extends Widget {

	public interface SupportAndPermissionListener {
		void notificationsAreAllowedAndSupported(boolean allowed,
				boolean supported);

		void notificationsAreSupported(boolean notificationsAreSupported);
	}

	private static class PermissionPopup extends VOverlay {
		public PermissionPopup(final String text,
				final AsyncCallback<Void> requestCallback) {
			setModal(true);
			setWidth("400px");
			setStyleName("v-permissionpopup");

			final VerticalPanel panel = new VerticalPanel();

			final HTML html = new HTML(text);
			html.setStyleName(null);
			panel.add(html);

			final Button button = new Button("OK");
			button.setStyleName("okbutton");
			button.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(final ClickEvent event) {
					requestPermission(PermissionPopup.this, requestCallback);
					hide();
				}
			});

			panel.add(button);
			panel.setCellHorizontalAlignment(button,
					HasHorizontalAlignment.ALIGN_CENTER);
			panel.setCellVerticalAlignment(button,
					HasVerticalAlignment.ALIGN_MIDDLE);
			add(panel);
		}

		private native void requestPermission(PermissionPopup x,
				AsyncCallback<Void> callback)
		/*-{
		  $wnd.webkitNotifications.requestPermission($entry(function() {
		          x.@com.github.wolfie.desktopnotifications.client.DesktopNotifierWidget.PermissionPopup::callbackRequestPermission(Lcom/google/gwt/user/client/rpc/AsyncCallback;)(callback);
		  }));
		}-*/;

		private void callbackRequestPermission(
				final AsyncCallback<Void> callback) {
			if (callback != null) {
				callback.onSuccess(null);
			}
		}
	}

	public static final String CLASSNAME = "v-desktopnotifier";

	public static final String ATT_ICON_ARRAY_STR = "i";
	public static final String ATT_HEADING_ARRAY_STR = "h";
	public static final String ATT_BODY_ARRAY_STR = "b";
	public static final String ATT_HTML_NOTIFICATIONS_STRARR = "ph";
	public static final String ATT_HTML_NOTIFICATIONS_RESOURCE_STRARR = "phr";

	private static final int NOTIFICATIONS_ALLOWED = 0;
	private static final int NOTIFICATIONS_NOT_YET_ANSWERED = 1;
	private static final int NOTIFICATIONS_DISALLOWED = 2;

	/**
	 * An identifier that differentiates between urls and resoruces in
	 * {@link com.github.wolfie.desktopnotifications.DesktopNotifier#showNotification(com.vaadin.terminal.Resource, String, String)
	 * DesktopNotifier.showNotification(Resource, String, String)} and
	 * {@link com.github.wolfie.desktopnotifications.DesktopNotifier#showNotification(String, String, String)
	 * DesktopNotifier.showNotification(String, String, String)}
	 */
	public static final String RESOURCE_STRING_PREFIX = "!";

	protected String paintableId;
	ApplicationConnection client;

	private boolean supportHasBeenChecked = false;

	private SupportAndPermissionListener sapListener = null;

	private String text = "If you wish to enable this feature, you need to click \"Allow\" in the toolbar that appears after you close this window.";

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public DesktopNotifierWidget() {
		setElement(Document.get().createDivElement());
		setStyleName(CLASSNAME);
		setWidth("0");
		setHeight("0");
	}

	public void setListener(final SupportAndPermissionListener listener) {
		sapListener = listener;
	}

	private String convertResourceUrlToRealUrlIfNeeded(final String url) {
		if (url.startsWith(RESOURCE_STRING_PREFIX)) {
			final String parsedResourceUrl = url
					.substring(RESOURCE_STRING_PREFIX.length());
			return getSrc(parsedResourceUrl, client);
		} else {
			return url;
		}
	}

	public void checkForSupportAndSendResultsToServer() {
		if (sapListener == null) {
			VConsole.log("No " + SupportAndPermissionListener.class
					+ " is listening, can't tell anyone about support");
			return;
		}

		if (notificationsAreAllowed()) {
			sapListener.notificationsAreAllowedAndSupported(true,
					notificationsAreSupported());
		} else if (notificationsAreDisallowed()) {
			sapListener.notificationsAreAllowedAndSupported(false,
					notificationsAreSupported());
		} else {
			sapListener.notificationsAreSupported(notificationsAreSupported());
		}

		supportHasBeenChecked = true;
	}

	/**
	 * Helper to return translated src-attribute from embedded's UIDL
	 * 
	 * @param uidl
	 * @param client
	 * @return
	 */
	private static String getSrc(final String string,
			final ApplicationConnection client) {
		final String url = client.translateVaadinUri(string);
		if (url == null) {
			return "";
		}
		return url;
	}

	public static native void showNotification(String icon, String heading,
			String body)
	/*-{
	 $wnd.webkitNotifications.createNotification(icon, heading, body).show();
	}-*/;

	private static native int checkPermission()
	/*-{
	 return $wnd.webkitNotifications.checkPermission();
	}-*/;

	private static boolean notificationsAreAllowed() {
		if (notificationsAreSupported()) {
			return checkPermission() == NOTIFICATIONS_ALLOWED;
		} else {
			return false;
		}
	}

	private static boolean notificationsAreDisallowed() {
		if (notificationsAreSupported()) {
			return checkPermission() == NOTIFICATIONS_DISALLOWED;
		} else {
			return false;
		}
	}

	private static boolean containsNotifications(final UIDL uidl) {
		return uidl.hasAttribute(ATT_BODY_ARRAY_STR);
	}

	private static native boolean notificationsAreSupported()
	/*-{
	 if ($wnd.webkitNotifications) 
	   return true;
	 else
	   return false;
	}-*/;

	private void trickUserIntoFiringAUserEvent(final String text) {
		if (notificationsAreSupported() && !notificationsAreAllowed()) {
			final PermissionPopup popup = new PermissionPopup(text,
					new AsyncCallback<Void>() {
						@Override
						public void onSuccess(final Void result) {
							checkForSupportAndSendResultsToServer();
						}

						@Override
						public void onFailure(final Throwable caught) {
							// ignore
						}
					});
			popup.show();
			popup.center();
			popup.setPopupPosition(popup.getPopupLeft(),
					Window.getScrollTop() + 50);
		}
	}

	public void setText(final String text) {
		this.text = text;
	}

	public void requestPermission() {
		trickUserIntoFiringAUserEvent(text);
	}
}
