package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.vaadin.terminal.gwt.client.communication.ClientRpc;
import com.vaadin.terminal.gwt.client.communication.URLReference;

public interface DesktopNotifierClientRpc extends ClientRpc {
  void requestPermission();

  void showNotification(URLReference resourceReference, String header,
      String body);

  void showNotification(String url);

  void showNotification(URLReference resourceReference);

  void showNotification(String iconUrl, String header, String body);
}
