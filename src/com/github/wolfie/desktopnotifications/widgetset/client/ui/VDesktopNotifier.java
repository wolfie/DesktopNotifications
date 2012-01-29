package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VOverlay;

public class VDesktopNotifier extends Widget implements Paintable {

  private static class VPermissionButton extends VOverlay {
    public VPermissionButton(final String text) {
      setModal(true);
      setWidth("300px");

      final VerticalPanel panel = new VerticalPanel();

      panel.add(new HTML(text));

      final Anchor anchor = new Anchor("ok");
      anchor.addClickHandler(new ClickHandler() {
        public void onClick(final ClickEvent event) {
          requestPermission();
          hide();
        }
      });

      panel.add(anchor);
      add(panel);
    }

    private static native void requestPermission()
    /*-{
      $wnd.webkitNotifications.requestPermission();
    }-*/;
  }

  public static final String CLASSNAME = "v-desktopnotifier";

  public static final String VAR_BROWSER_SUPPORT_BOOL = "s";
  public static final String ATT_ICON_ARRAY_STR = "i";
  public static final String ATT_HEADING_ARRAY_STR = "h";
  public static final String ATT_BODY_ARRAY_STR = "b";
  public static final String ATT_REQUEST_PERMISSION = "p";
  public static final String ATT_TEXT_STRING = "t";
  public static final String ATT_HTML_NOTIFICAITONS_STRARR = "ph";

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
          notificationsAreSupported(), true);
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

  private static native boolean notificationsAreAllowed()
  /*-{
   return $wnd.webkitNotifications.checkPermission() == 0;
  }-*/;

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
      final VPermissionButton button = new VPermissionButton(text);
      button.show();
      button.center();
    }
  }
}
