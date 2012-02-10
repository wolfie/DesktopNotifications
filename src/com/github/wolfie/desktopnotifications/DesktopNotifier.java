package com.github.wolfie.desktopnotifications;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.wolfie.desktopnotifications.widgetset.client.ui.VDesktopNotifier;
import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.JsonPaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VDesktopNotifier widget.
 */
@com.vaadin.ui.ClientWidget(com.github.wolfie.desktopnotifications.widgetset.client.ui.VDesktopNotifier.class)
public class DesktopNotifier extends AbstractComponent {

  public interface SettingsListener {

    /**
     * @param isSupportedByBrowser
     * @param isAllowed
     *          <code>null</code> if the user hasn't given an answer yet
     * @param isDisallowed
     *          <code>null</code> if the user hasn't given an answer yet
     */
    void notificationSettingsChanged(Boolean isSupportedByBrowser,
        Boolean isAllowed, Boolean isDisallowed);

  }

  public class Notification {
    private final String icon;
    private final String heading;
    private final String body;

    public Notification(final String icon, final String heading,
        final String body) {
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
  private Boolean isAllowed;
  private Boolean isDisallowed;

  private final List<Notification> pendingNotifications = new ArrayList<Notification>();
  private final List<Resource> pendingHtmlNotificationsResources = new ArrayList<Resource>();
  private boolean pendingPermissionRequest;

  private String text = null;
  private boolean pendingTextChange = false;

  private final Set<String> pendingHtmlNotifications = new HashSet<String>();
  private final List<SettingsListener> listeners = new ArrayList<DesktopNotifier.SettingsListener>();

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
      target.addAttribute(VDesktopNotifier.ATT_HTML_NOTIFICATIONS_STRARR,
          htmlUrlsArray);
      pendingHtmlNotifications.clear();
    }

    if (!pendingHtmlNotificationsResources.isEmpty()) {
      final Resource[] htmlResourcesArray = pendingHtmlNotificationsResources
          .toArray(new Resource[pendingHtmlNotificationsResources.size()]);

      final String[] htmlResourceStringsArray = new String[htmlResourcesArray.length];
      for (int i = 0; i < htmlResourcesArray.length; i++) {
        htmlResourceStringsArray[i] = convertResourceToString(htmlResourcesArray[i]);
      }

      target.addAttribute(
          VDesktopNotifier.ATT_HTML_NOTIFICATIONS_RESOURCE_STRARR,
          htmlResourceStringsArray);

      pendingHtmlNotificationsResources.clear();
    }
  }

  @Override
  public void changeVariables(final Object source,
      final Map<String, Object> variables) {
    super.changeVariables(source, variables);

    boolean shouldCallListeners = false;

    if (variables.containsKey(VDesktopNotifier.VAR_BROWSER_SUPPORT_BOOL)) {
      isSupportedByBrowser = (Boolean) variables
          .get(VDesktopNotifier.VAR_BROWSER_SUPPORT_BOOL);
      shouldCallListeners = true;
    }

    if (variables.containsKey(VDesktopNotifier.VAR_ALLOWED_BOOL)) {
      final boolean allowed = (Boolean) variables
          .get(VDesktopNotifier.VAR_ALLOWED_BOOL);
      isAllowed = allowed;
      isDisallowed = !allowed;
      shouldCallListeners = true;
    }

    if (shouldCallListeners) {
      for (final SettingsListener listener : listeners) {
        listener.notificationSettingsChanged(isSupportedByBrowser, isAllowed,
            isDisallowed);
      }
    }
  }

  /**
   * Checks whether the browser supports desktop notifications as a feature.
   * 
   * @throws MethodWasCalledBeforeClientRoundtripException
   *           if the method has been called before there has been a
   *           server&rarr;client&rarr;server roundtrip.
   */
  public boolean notificationsAreSupportedByBrowser()
      throws MethodWasCalledBeforeClientRoundtripException {
    if (isSupportedByBrowser == null) {
      throw new MethodWasCalledBeforeClientRoundtripException();
    } else {
      return isSupportedByBrowser;
    }
  }

  /**
   * Checks whether the user has allowed desktop notifications.
   * 
   * @return <code>true</code> iff the user has given an explicit permission for
   *         the server to send desktop notifications. <code>false</code> is
   *         returned if the user hasn't answered to the permission prompt, or
   *         if the user has explicitly disallowed notifications.
   * @throws MethodWasCalledBeforeClientRoundtripException
   *           if the method was called before a server-client roundtrip was
   *           completed.
   */
  public boolean notificationsAreAllowedByUser()
      throws MethodWasCalledBeforeClientRoundtripException {
    if (browserHasSentSupportInformation()) {
      return (isAllowed != null) ? isAllowed : false;
    } else {
      throw new MethodWasCalledBeforeClientRoundtripException();
    }
  }

  private boolean browserHasSentSupportInformation() {
    return isSupportedByBrowser != null;
  }

  /**
   * Checks whether the user has disallowed desktop notifications
   * 
   * @return <code>true</code> iff the user has explicitly denied the server to
   *         send desktop notifications. <code>false</code> is returned if the
   *         user hasn't answered to the permission prompt, or if the user has
   *         explicitly allowed notifications.
   * @throws MethodWasCalledBeforeClientRoundtripException
   *           if the method was called before a server-client roundtrip was
   *           completed.
   */
  public boolean notificationsAreDisallowedByUser()
      throws MethodWasCalledBeforeClientRoundtripException {
    if (browserHasSentSupportInformation()) {
      return (isDisallowed != null) ? isDisallowed : false;
    } else {
      throw new MethodWasCalledBeforeClientRoundtripException();
    }
  }

  /**
   * Show a plaintext notification
   * <p/>
   * <strong>Note:</strong> This method does nothing if
   * 
   * <ul>
   * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code> OR
   * <li>{@link #notificationsAreDisallowedByUser()} is <code>true</code> OR
   * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code>
   * </ul>
   * 
   * @param iconUrl
   * @param header
   * @param body
   */
  public void showNotification(final String iconUrl, final String header,
      final String body) {
    pendingNotifications.add(new Notification(iconUrl, header, body));
    requestRepaint();
  }

  /**
   * Show a HTML notification
   * <p/>
   * <strong>Note:</strong> This method does nothing if
   * 
   * <ul>
   * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code> OR
   * <li>{@link #notificationsAreDisallowedByUser()} is <code>true</code> OR
   * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code>
   * </ul>
   * 
   * @param url
   */
  public void showHtmlNotification(final String url) {
    pendingHtmlNotifications.add(url);
    requestRepaint();
  }

  /**
   * Show a HTML notification
   * <p/>
   * <strong>Note:</strong> This method does nothing if
   * 
   * <ul>
   * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code> OR
   * <li>{@link #notificationsAreDisallowedByUser()} is <code>true</code> OR
   * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code>
   * </ul>
   * 
   * @param resource
   */
  public void showHtmlNotification(final Resource resource) {
    pendingHtmlNotificationsResources.add(resource);
    requestRepaint();
  }

  /**
   * Prompts the user with a request for permission for showing desktop
   * notifications.
   * 
   * @see #setFeatureDescriptionText(String)
   */
  public void requestPermission() {
    pendingPermissionRequest = true;
    requestRepaint();
  }

  /**
   * Change the text that is shown to the user in the popup that will be
   * presented before the user can decide whether she allows desktop
   * notifications.
   * 
   * @param text
   */
  public void setFeatureDescriptionText(final String text) {
    this.text = text;
    pendingTextChange = true;
    requestRepaint();
  }

  /**
   * Since there's no support for Resource arrays, we need to hack it. Copied
   * from {@link JsonPaintTarget#addAttribute(String, Resource)}
   */
  private static String convertResourceToString(final Resource resource)
      throws PaintException {
    if (resource instanceof ExternalResource) {
      return ((ExternalResource) resource).getURL();

    } else if (resource instanceof ApplicationResource) {
      final ApplicationResource r = (ApplicationResource) resource;
      final Application a = r.getApplication();
      if (a == null) {
        throw new PaintException("Application not specified for resorce "
            + resource.getClass().getName());
      }
      final String uri = a.getRelativeLocation(r);
      return uri;
    } else if (resource instanceof ThemeResource) {
      final String uri = "theme://"
          + ((ThemeResource) resource).getResourceId();
      return uri;
    } else {
      throw new PaintException("Ajax adapter does not "
          + "support resources of type: " + resource.getClass().getName());
    }
  }

  public void addListener(final SettingsListener listener) {
    if (listener != null) {
      listeners.add(listener);
    }
  }

  public void removeListener(final SettingsListener listener) {
    listeners.remove(listener);
  }
}
