package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.vaadin.terminal.gwt.client.communication.ServerRpc;

public interface DesktopNotifierServerRpc extends ServerRpc {
  void notificationsAreAllowedSupported(boolean allowed, boolean supported);
}
