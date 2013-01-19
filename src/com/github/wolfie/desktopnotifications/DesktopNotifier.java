package com.github.wolfie.desktopnotifications;

import java.util.ArrayList;
import java.util.List;

import com.github.wolfie.desktopnotifications.widgetset.client.ui.DesktopNotifierClientRpc;
import com.github.wolfie.desktopnotifications.widgetset.client.ui.DesktopNotifierServerRpc;
import com.github.wolfie.desktopnotifications.widgetset.client.ui.DesktopNotifierState;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VDesktopNotifier widget.
 */
public class DesktopNotifier extends AbstractComponent implements
		DesktopNotifierServerRpc {
	private static final long serialVersionUID = -4935586799603427503L;

	public interface SettingsListener {

		/**
		 * @param isSupportedByBrowser
		 * @param isAllowed
		 *            <code>null</code> if the user hasn't given an answer yet
		 * @param isDisallowed
		 *            <code>null</code> if the user hasn't given an answer yet
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

	private final List<SettingsListener> listeners = new ArrayList<DesktopNotifier.SettingsListener>();

	public DesktopNotifier() {
		registerRpc(this);
	}

	/**
	 * Checks whether the browser supports desktop notifications as a feature.
	 * 
	 * @throws MethodWasCalledBeforeClientRoundtripException
	 *             if the method has been called before there has been a
	 *             server&rarr;client&rarr;server roundtrip.
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
	 * @return <code>true</code> iff the user has given an explicit permission
	 *         for the server to send desktop notifications. <code>false</code>
	 *         is returned if the user hasn't answered to the permission prompt,
	 *         or if the user has explicitly disallowed notifications.
	 * @throws MethodWasCalledBeforeClientRoundtripException
	 *             if the method was called before a server-client roundtrip was
	 *             completed.
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
		return isSupportedByBrowser != null || isSupportedByBrowser != null;
	}

	/**
	 * Checks whether the user has disallowed desktop notifications
	 * 
	 * @return <code>true</code> iff the user has explicitly denied the server
	 *         to send desktop notifications. <code>false</code> is returned if
	 *         the user hasn't answered to the permission prompt, or if the user
	 *         has explicitly allowed notifications.
	 * @throws MethodWasCalledBeforeClientRoundtripException
	 *             if the method was called before a server-client roundtrip was
	 *             completed.
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
	 * <li>{@link #notificationsAreSupportedByBrowser()} is <code>false</code>
	 * OR
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
		getRpcProxy(DesktopNotifierClientRpc.class).showNotification4(iconUrl,
				header, body);
	}

	/**
	 * Show a plaintext notification
	 * <p/>
	 * <strong>Note:</strong> This method does nothing if
	 * 
	 * <ul>
	 * <li>{@link #notificationsAreSupportedByBrowser()} is <code>false</code>
	 * OR
	 * <li>{@link #notificationsAreDisallowedByUser()} is <code>true</code> OR
	 * <li>{@link #notificationsAreAllowedByUser()} is <code>false</code>
	 * </ul>
	 * 
	 * @param iconResource
	 * @param header
	 * @param body
	 */
	public void showNotification(final Resource iconResource,
			final String header, final String body) {
		setResource(DesktopNotifierState.ICON, iconResource);
		getRpcProxy(DesktopNotifierClientRpc.class).showNotification1(header,
				body);
	}

	/**
	 * Prompts the user with a request for permission for showing desktop
	 * notifications.
	 * 
	 * @see #setFeatureDescriptionText(String)
	 */
	public void requestPermission() {
		getRpcProxy(DesktopNotifierClientRpc.class).requestPermission();
	}

	@Override
	public DesktopNotifierState getState() {
		return (DesktopNotifierState) super.getState();
	}

	/**
	 * Change the text that is shown to the user in the popup that will be
	 * presented before the user can decide whether she allows desktop
	 * notifications.
	 * 
	 * @param text
	 */
	public void setFeatureDescriptionText(final String text) {
		getState().text = text;
	}

	public void addListener(final SettingsListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeListener(final SettingsListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void notificationsAreAllowedAndSupported(final boolean allowed,
			final boolean supported) {
		isAllowed = allowed;
		isDisallowed = !allowed;
		isSupportedByBrowser = supported;

		fireListeners();
	}

	private void fireListeners() {
		for (final SettingsListener listener : listeners) {
			listener.notificationSettingsChanged(isSupportedByBrowser,
					isAllowed, isDisallowed);
		}
	}

	@Override
	public void notificationsAreSupported(final boolean supported) {
		isSupportedByBrowser = supported;
		fireListeners();
	}
}
