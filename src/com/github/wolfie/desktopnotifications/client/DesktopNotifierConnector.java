package com.github.wolfie.desktopnotifications.client;

import com.github.wolfie.desktopnotifications.DesktopNotifier;
import com.github.wolfie.desktopnotifications.client.DesktopNotifierExtension.SupportAndPermissionListener;
import com.github.wolfie.desktopnotifications.shared.DesktopNotifierClientRpc;
import com.github.wolfie.desktopnotifications.shared.DesktopNotifierServerRpc;
import com.github.wolfie.desktopnotifications.shared.DesktopNotifierState;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

@Connect(DesktopNotifier.class)
public class DesktopNotifierConnector extends AbstractExtensionConnector
		implements SupportAndPermissionListener {

	private static final long serialVersionUID = 3927755257353627672L;
	private DesktopNotifierServerRpc serverRpc;
	private final DesktopNotifierExtension notifier = new DesktopNotifierExtension();

	@Override
	protected void init() {
		super.init();
		registerRpc(DesktopNotifierClientRpc.class,
				new DesktopNotifierClientRpc() {
					private static final long serialVersionUID = -8207606706071556057L;

					@Override
					public void requestPermission() {
						getExtension().requestPermission();
					}

					@Override
					public void showNotification1(final String header,
							final String body) {
						final String iconUrl = getResourceUrl(DesktopNotifierState.ICON);
						DesktopNotifierExtension.showNotification(iconUrl, header,
								body);
					}

					@Override
					public void showNotification4(final String iconUrl,
							final String header, final String body) {
						DesktopNotifierExtension.showNotification(iconUrl, header,
								body);
					}
				});

		serverRpc = RpcProxy.create(DesktopNotifierServerRpc.class, this);

		getExtension().setListener(this);

		/*
		 * do this at init, so that we get immediate, if it's been denied or
		 * allowed.
		 */
		getExtension().checkForSupportAndSendResultsToServer();
	}

	public DesktopNotifierExtension getExtension() {
		return notifier;
	}

	@Override
	public DesktopNotifierState getState() {
		return (DesktopNotifierState) super.getState();
	}

	@Override
	public void onStateChanged(final StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		getExtension().setText(getState().text);
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

	@Override
	protected void extend(final ServerConnector target) {
		// nothing to do.
	}
}
