package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.vaadin.terminal.gwt.client.communication.ClientRpc;
import com.vaadin.terminal.gwt.client.communication.URLReference;

public interface DesktopNotifierClientRpc extends ClientRpc {
  void requestPermission();

  /*
   * The number suffixes for the method names are due to
   * http://dev.vaadin.com/ticket/8456
   */

  void showNotification1(URLReference resourceReference, String header,
      String body);

  void showNotification2(String url);

  void showNotification3(URLReference resourceReference);

  void showNotification4(String iconUrl, String header, String body);
}
