package com.github.wolfie.desktopnotifications;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.wolfie.desktopnotifications.widgetset.client.ui.VDesktopNotifier;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VDesktopNotifier widget.
 */
@com.vaadin.ui.ClientWidget(com.github.wolfie.desktopnotifications.widgetset.client.ui.VDesktopNotifier.class)
public class DesktopNotifier extends AbstractComponent {

  public class Notification {
    private final String icon;
    private final String heading;
    private final String body;

    public Notification(final String icon, final String heading, final String body) {
      super();
      this.icon = icon;
      this.heading = heading;
      this.body = body;
    }

    public String getIcon() {
      return icon;
    }

    public String getHeading() {
      return heading;
    }

    public String getBody() {
      return body;
    }
  }

  private Boolean isSupportedByBrowser = null;
  private final List<Notification> pendingNotifications = new ArrayList<Notification>();
  private boolean pendingPermissionRequest;

  private String text = null;
  private boolean pendingTextChange = false;

  private final Set<String> pendingHtmlNotifications = new HashSet<String>();

  @Override
  public void paintContent(final PaintTarget target) throws PaintException {
    super.paintContent(target);

    if (pendingPermissionRequest) {
      target.addAttribute(VDesktopNotifier.ATT_REQUEST_PERMISSION, true);
      pendingPermissionRequest = false;
    }

    if ((pendingTextChange || target.isFullRepaint()) && text != null) {
      target.addAttribute(VDesktopNotifier.ATT_TEXT_STRING, text);
      pendingTextChange = false;
    }

    if (!pendingNotifications.isEmpty()) {
      final String[] icons = new String[pendingNotifications.size()];
      final String[] headings = new String[pendingNotifications.size()];
      final String[] bodies = new String[pendingNotifications.size()];

      for (int i = 0; i < pendingNotifications.size(); i++) {
        final Notification n = pendingNotifications.get(i);
        icons[i] = n.getIcon();
        headings[i] = n.getHeading();
        bodies[i] = n.getBody();
      }
      target.addAttribute(VDesktopNotifier.ATT_ICON_ARRAY_STR, icons);
      target.addAttribute(VDesktopNotifier.ATT_HEADING_ARRAY_STR, headings);
      target.addAttribute(VDesktopNotifier.ATT_BODY_ARRAY_STR, bodies);

      pendingNotifications.clear();
    }

    if (!pendingHtmlNotifications.isEmpty()) {
      final String[] htmlUrlsArray = pendingHtmlNotifications
          .toArray(new String[pendingHtmlNotifications.size()]);
      target.addAttribute(VDesktopNotifier.ATT_HTML_NOTIFICAITONS_STRARR,
          htmlUrlsArray);
      pendingHtmlNotifications.clear();
    }
  }

  @Override
  public void changeVariables(final Object source,
      final Map<String, Object> variables) {
    super.changeVariables(source, variables);

    if (variables.containsKey(VDesktopNotifier.VAR_BROWSER_SUPPORT_BOOL)) {
      isSupportedByBrowser = (Boolean) variables
          .get(VDesktopNotifier.VAR_BROWSER_SUPPORT_BOOL);
    }
  }

  public boolean isSupportedByBrowser()
      throws MethodWasCalledBeforeClientRoundtripException {
    if (isSupportedByBrowser == null) {
      throw new MethodWasCalledBeforeClientRoundtripException();
    } else {
      return isSupportedByBrowser;
    }
  }

  public void showNotification(final String iconUrl, final String header,
      final String body) {
    pendingNotifications.add(new Notification(iconUrl, header, body));
    requestRepaint();
  }

  public void showHtmlNotification(final String url) {
    pendingHtmlNotifications.add(url);
    requestRepaint();
  }

  public void requestPermission() {
    pendingPermissionRequest = true;
    requestRepaint();
  }

  public void setFeatureDescriptionText(final String text) {
    this.text = text;
    pendingTextChange = true;
    requestRepaint();
  }

}
