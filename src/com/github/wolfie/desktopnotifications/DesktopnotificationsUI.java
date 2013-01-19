package com.github.wolfie.desktopnotifications;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("dn")
@Title("Demo")
public class DesktopnotificationsUI extends UI {

	private final DesktopNotifier c = new DesktopNotifier();

	@Override
	protected void init(final VaadinRequest request) {

		final VerticalLayout root = new VerticalLayout();
		setContent(root);

		final Label label = new Label(
				"How many seconds before you want to feel special?");
		root.addComponent(label);

		final CssLayout cssLayout = new CssLayout();
		cssLayout.setWidth("100%");
		root.addComponent(cssLayout);
		addButton(0, cssLayout);
		addButton(2, cssLayout);
		addButton(4, cssLayout);
		addButton(6, cssLayout);
		addButton(8, cssLayout);
		addButton(10, cssLayout);

		addExtension(c);

		final Button awesomeButton = new Button("Make notifications awesome",
				new Button.ClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						c.requestPermission();
					}
				});
		root.addComponent(awesomeButton);

		c.addListener(new DesktopNotifier.SettingsListener() {
			@Override
			public void notificationSettingsChanged(
					final Boolean isSupportedByBrowser,
					final Boolean isAllowed, final Boolean isDisallowed) {

				awesomeButton.setVisible(isSupportedByBrowser == Boolean.TRUE
						|| isDisallowed == Boolean.TRUE);
				final boolean isNotYetEnabled = isAllowed != Boolean.TRUE;
				awesomeButton.setEnabled(isNotYetEnabled);

				if (!isNotYetEnabled) {
					awesomeButton
							.setCaption("okay, they're considerably more awesome now");
				}
			}
		});
	}

	private void addButton(final int i, final Layout layout) {
		final Button button = new Button(Integer.toString(i));
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					Thread.sleep(i * 1000);
					if (c.notificationsAreAllowedByUser()) {
						c.showNotification(new ThemeResource("icon.png"),
								"Header", "body");
					} else {
						Notification.show("You have mail.");
					}
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		});
		layout.addComponent(button);
	}
}
