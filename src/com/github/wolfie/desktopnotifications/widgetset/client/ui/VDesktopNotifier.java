package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VOverlay;

public class VDesktopNotifier extends Widget implements Paintable {

  private static class VPermissionPopup extends VOverlay {
    public VPermissionPopup(final String text) {
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
          requestPermission();
          hide();
        }
      });

      panel.add(button);
      panel.setCellHorizontalAlignment(button,
          HasHorizontalAlignment.ALIGN_CENTER);
      panel.setCellVerticalAlignment(button, HasVerticalAlignment.ALIGN_MIDDLE);
      add(panel);
    }

    private static native void requestPermission()
    /*-{
      $wnd.webkitNotifications.requestPermission();
    }-*/;
  }

  public static final String CLASSNAME = "v-desktopnotifier";

  public static final String VAR_ALLOWED_BOOL = "a";
  public static final String VAR_BROWSER_SUPPORT_BOOL = "s";
  public static final String ATT_ICON_ARRAY_STR = "i";
  public static final String ATT_HEADING_ARRAY_STR = "h";
  public static final String ATT_BODY_ARRAY_STR = "b";
  public static final String ATT_REQUEST_PERMISSION = "p";
  public static final String ATT_TEXT_STRING = "t";
  public static final String ATT_HTML_NOTIFICAITONS_STRARR = "ph";

  private static final int NOTIFICATIONS_ALLOWED = 0;
  private static final int NOTIFICATIONS_NOT_YET_ANSWERED = 1;
  private static final int NOTIFICATIONS_DISALLOWED = 2;

  protected String paintableId;
  ApplicationConnection client;

  private boolean supportHasBeenChecked = false;

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

  /**
   * Called whenever an update is received from the server
   */
  public void updateFromUIDL(final UIDL uidl, final ApplicationConnection client) {
    if (client.updateComponent(this, uidl, true)) {
      return;
    }

    this.client = client;
    paintableId = uidl.getId();

    if (uidl.hasAttribute(ATT_TEXT_STRING)) {
      text = uidl.getStringAttribute(ATT_TEXT_STRING);
    }

    if (uidl.hasAttribute(ATT_REQUEST_PERMISSION)) {
      trickUserIntoFiringAUserEvent(text);
    }

    if (containsNotifications(uidl)) {
      if (!notificationsAreSupported()) {
        VConsole.log("Tried to send unsupported desktop notifications");
      } else if (!notificationsAreAllowed()) {
        VConsole.log("Notifications aren't allowed");
      } else {
        final String[] icons = uidl.getStringArrayAttribute(ATT_ICON_ARRAY_STR);
        final String[] headings = uidl
            .getStringArrayAttribute(ATT_HEADING_ARRAY_STR);
        final String[] bodies = uidl
            .getStringArrayAttribute(ATT_BODY_ARRAY_STR);

        for (int i = 0; i < icons.length; i++) {
          showNotification(icons[i], headings[i], bodies[i]);
        }
      }
    }

    if (uidl.hasAttribute(ATT_HTML_NOTIFICAITONS_STRARR)) {
      if (!notificationsAreSupported()) {
        VConsole.log("Tried to send unsupported desktop notifications");
      } else if (!notificationsAreAllowed()) {
        VConsole.log("Notifications aren't allowed");
      } else {
        final String[] htmlUrls = uidl
            .getStringArrayAttribute(ATT_HTML_NOTIFICAITONS_STRARR);
        for (final String htmlUrl : htmlUrls) {
          showHtmlNotification(htmlUrl);
        }
      }
    }

    if (!supportHasBeenChecked) {
      client.updateVariable(paintableId, VAR_BROWSER_SUPPORT_BOOL,
          notificationsAreSupported(), false);

      if (notificationsAreAllowed()) {
        client.updateVariable(paintableId, VAR_ALLOWED_BOOL, true, false);
      } else if (notificationsAreDisallowed()) {
        client.updateVariable(paintableId, VAR_ALLOWED_BOOL, false, false);
      } else {
        /*
         * since the notification allowing/disallowing isn't answered to yet,
         * don't send anything to the server.
         */
      }

      client.sendPendingVariableChanges();
      supportHasBeenChecked = true;
    }
  }

  private static native boolean showNotification(String icon, String heading,
      String body)
  /*-{
   $wnd.webkitNotifications.createNotification(icon, heading, body).show();
  }-*/;

  private static native boolean showHtmlNotification(String url)
  /*-{
   $wnd.webkitNotifications.createHTMLNotification(url).show();
  }-*/;

  private static native int checkPermission()
  /*-{
   return $wnd.webkitNotifications.checkPermission();
  }-*/;

  private static boolean notificationsAreAllowed() {
    return checkPermission() == NOTIFICATIONS_ALLOWED;
  }

  private static boolean notificationsAreDisallowed() {
    return checkPermission() == NOTIFICATIONS_DISALLOWED;
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

  private static void trickUserIntoFiringAUserEvent(final String text) {
    if (notificationsAreSupported() && !notificationsAreAllowed()) {
      final VPermissionPopup popup = new VPermissionPopup(text);
      popup.show();
      popup.center();
      popup.setPopupPosition(popup.getPopupLeft(), Window.getScrollTop() + 50);
    }
  }
}
