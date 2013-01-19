package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.github.wolfie.desktopnotifications.DesktopNotifier;
import com.github.wolfie.desktopnotifications.widgetset.client.ui.DesktopNotifierWidget.SupportAndPermissionListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(DesktopNotifier.class)
public class DesktopNotifierConnector extends AbstractComponentConnector
		implements SupportAndPermissionListener {

	private static final long serialVersionUID = 3927755257353627672L;
	private DesktopNotifierServerRpc serverRpc;

	@Override
	protected void init() {
		super.init();
		registerRpc(DesktopNotifierClientRpc.class,
				new DesktopNotifierClientRpc() {
					private static final long serialVersionUID = -8207606706071556057L;

					@Override
					public void requestPermission() {
						getWidget().requestPermission();
					}

					@Override
					public void showNotification1(final String header,
							final String body) {
						final String iconUrl = getResourceUrl(DesktopNotifierState.ICON);
						DesktopNotifierWidget.showNotification(iconUrl, header,
								body);
					}

					@Override
					public void showNotification4(final String iconUrl,
							final String header, final String body) {
						DesktopNotifierWidget.showNotification(iconUrl, header,
								body);
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
		return GWT.create(DesktopNotifierWidget.class);
	}

	@Override
	public DesktopNotifierWidget getWidget() {
		return (DesktopNotifierWidget) super.getWidget();
	}

	@Override
	public DesktopNotifierState getState() {
		return (DesktopNotifierState) super.getState();
	}

	@Override
	public void onStateChanged(final StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		getWidget().setText(getState().text);
	}

	@Override
	public void notificationsAreAllowedAndSupported(final boolean allowed,
			final boolean supported) {
		serverRpc.notificationsAreAllowedAndSupported(allowed, supported);
	}

	@Override
	public void notificationsAreSupported(final boolean supported) {
		serverRpc.notificationsAreSupported(supported);
	}
}
