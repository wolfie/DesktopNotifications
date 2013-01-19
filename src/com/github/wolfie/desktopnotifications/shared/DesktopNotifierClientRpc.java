package com.github.wolfie.desktopnotifications.shared;

import com.vaadin.shared.communication.ClientRpc;

public interface DesktopNotifierClientRpc extends ClientRpc {
	void requestPermission();

	/*
	 * The number suffixes for the method names are due to
	 * http://dev.vaadin.com/ticket/8456
	 */

	void showNotification1(String header, String body);

	void showNotification4(String iconUrl, String header, String body);
}
