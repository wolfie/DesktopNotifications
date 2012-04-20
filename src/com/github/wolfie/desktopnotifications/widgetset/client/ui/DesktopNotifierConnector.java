package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.github.wolfie.desktopnotifications.DesktopNotifier;
import com.github.wolfie.desktopnotifications.widgetset.client.ui.VDesktopNotifier.SupportAndPermissionListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.communication.RpcProxy;
import com.vaadin.terminal.gwt.client.communication.StateChangeEvent;
import com.vaadin.terminal.gwt.client.communication.URLReference;
import com.vaadin.terminal.gwt.client.ui.AbstractComponentConnector;
import com.vaadin.terminal.gwt.client.ui.Connect;

@Connect(DesktopNotifier.class)
public class DesktopNotifierConnector extends AbstractComponentConnector
    implements SupportAndPermissionListener {

  private static final long serialVersionUID = 3927755257353627672L;
  private DesktopNotifierServerRpc serverRpc;

  @Override
  protected void init() {
    super.init();
    registerRpc(DesktopNotifierClientRpc.class, new DesktopNotifierClientRpc() {
      private static final long serialVersionUID = -8207606706071556057L;

      @Override
      public void requestPermission() {
        getWidget().requestPermission();
      }

      @Override
      public void showNotification(final URLReference icon,
          final String header, final String body) {
        VConsole
            .error("DesktopNotifierConnector.init().new DesktopNotifierClientRpc() {...}.showNotification(URL STRING STRING)");
        VDesktopNotifier.showNotification(icon.getURL(), header, body);
      }

      @Override
      public void showNotification(final String url) {
        VConsole
            .error("DesktopNotifierConnector.init().new DesktopNotifierClientRpc() {...}.showNotification(String)");
        VDesktopNotifier.showHtmlNotification(url);
      }

      @Override
      public void showNotification(final URLReference resource) {
        VConsole
            .error("DesktopNotifierConnector.init().new DesktopNotifierClientRpc() {...}.showNotification(URL)");
        VDesktopNotifier.showHtmlNotification(resource.getURL());
      }

      @Override
      public void showNotification(final String iconUrl, final String header,
          final String body) {
        VConsole
            .error("DesktopNotifierConnector.init().new DesktopNotifierClientRpc() {...}.showNotification(STRING STRING STRING)");
        VDesktopNotifier.showNotification(iconUrl, header, body);
      }
    });

    serverRpc = RpcProxy.create(DesktopNotifierServerRpc.class, this);

    getWidget().setListener(this);

    /*
     * do this at init, so that we get immediate, if it's been denied or
     * allowed.
     */
    getWidget().checkForSupportAndSendResultsToServer();
  }

  @Override
  protected Widget createWidget() {
    return GWT.create(VDesktopNotifier.class);
  }

  @Override
  public VDesktopNotifier getWidget() {
    return (VDesktopNotifier) super.getWidget();
  }

  @Override
  public DesktopNotifierState getState() {
    return (DesktopNotifierState) super.getState();
  }

  @Override
  public void onStateChanged(final StateChangeEvent stateChangeEvent) {
    super.onStateChanged(stateChangeEvent);

    getWidget().setText(getState().getText());
  }

  @Override
  public void notificationsAreAllowedSupported(final boolean allowed,
      final boolean supported) {
    serverRpc.notificationsAreAllowedSupported(allowed, supported);
  }
}
