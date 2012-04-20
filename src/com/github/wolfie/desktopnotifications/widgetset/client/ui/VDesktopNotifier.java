package com.github.wolfie.desktopnotifications.widgetset.client.ui;

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
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VOverlay;

public class VDesktopNotifier extends Widget {

  public interface SupportAndPermissionListener {
    void notificationsAreAllowedSupported(boolean allowed, boolean supported);
  }

  private static class VPermissionPopup extends VOverlay {
    public VPermissionPopup(final String text,
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
        public void onClick(final ClickEvent event) {
          requestPermission(VPermissionPopup.this, requestCallback);
          hide();
        }
      });

      panel.add(button);
      panel.setCellHorizontalAlignment(button,
          HasHorizontalAlignment.ALIGN_CENTER);
      panel.setCellVerticalAlignment(button, HasVerticalAlignment.ALIGN_MIDDLE);
      add(panel);
    }

    private native void requestPermission(VPermissionPopup x,
        AsyncCallback<Void> callback)
    /*-{
      $wnd.webkitNotifications.requestPermission($entry(function() {
              x.@com.github.wolfie.desktopnotifications.widgetset.client.ui.VDesktopNotifier.VPermissionPopup::callbackRequestPermission(Lcom/google/gwt/user/client/rpc/AsyncCallback;)(callback);
      }));
    }-*/;

    private void callbackRequestPermission(final AsyncCallback<Void> callback) {
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
  public VDesktopNotifier() {
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
      final String parsedResourceUrl = url.substring(RESOURCE_STRING_PREFIX
          .length());
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
      sapListener.notificationsAreAllowedSupported(true,
          notificationsAreSupported());
    } else if (notificationsAreDisallowed()) {
      sapListener.notificationsAreAllowedSupported(false,
          notificationsAreSupported());
    } else {
      /*
       * since the notification allowing/disallowing isn't answered to yet,
       * don't send anything to the server.
       */
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

  public static native boolean showNotification(String icon, String heading,
      String body)
  /*-{
   $wnd.webkitNotifications.createNotification(icon, heading, body).show();
  }-*/;

  public static native boolean showHtmlNotification(String url)
  /*-{
   $wnd.webkitNotifications.createHTMLNotification(url).show();
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
      final VPermissionPopup popup = new VPermissionPopup(text,
          new AsyncCallback<Void>() {
            public void onSuccess(final Void result) {
              checkForSupportAndSendResultsToServer();
            }

            public void onFailure(final Throwable caught) {
              // ignore
            }
          });
      popup.show();
      popup.center();
      popup.setPopupPosition(popup.getPopupLeft(), Window.getScrollTop() + 50);
    }
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void requestPermission() {
    trickUserIntoFiringAUserEvent(text);
  }
}
