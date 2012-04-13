package com.github.wolfie.desktopnotifications;

import com.vaadin.annotations.Theme;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Root;

@SuppressWarnings("serial")
@Theme("dn")
public class DesktopnotificationsRoot extends Root {

  private final DesktopNotifier c = new DesktopNotifier();

  @Override
  protected void init(final WrappedRequest request) {
    setDescription("Demo");

    final Label label = new Label(
        "How many seconds before you want to feel special?");
    addComponent(label);

    final CssLayout cssLayout = new CssLayout();
    cssLayout.setWidth("100%");
    addComponent(cssLayout);
    addButton(0, cssLayout);
    addButton(2, cssLayout);
    addButton(4, cssLayout);
    addButton(6, cssLayout);
    addButton(8, cssLayout);
    addButton(10, cssLayout);

    addComponent(c);

    final Button awesomeButton = new Button("Make notifications awesome",
        new Button.ClickListener() {
          public void buttonClick(final ClickEvent event) {
            c.requestPermission();
          }
        });
    addComponent(awesomeButton);

    c.addListener(new DesktopNotifier.SettingsListener() {
      public void notificationSettingsChanged(
          final Boolean isSupportedByBrowser, final Boolean isAllowed,
          final Boolean isDisallowed) {

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
    button.addListener(new Button.ClickListener() {
      public void buttonClick(final ClickEvent event) {
        try {
          Thread.sleep(i * 1000);
          if (c.notificationsAreAllowedByUser()) {
            c.showHtmlNotification(new ThemeResource("notification.html"));
            c.showNotification(new ThemeResource("icon.png"), "Header", "body");
          } else {
            getRoot().showNotification("You have mail.");
          }
        } catch (final InterruptedException e) {
          // ignore
        }
      }
    });
    layout.addComponent(button);
  }
}
