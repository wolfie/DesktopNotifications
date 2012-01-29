package com.github.wolfie.desktopnotifications;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class DesktopnotificationsApplication extends Application {
  @Override
  public void init() {
    setTheme("dn");
    setMainWindow(new Window("Demo"));
    getMainWindow().addComponent(new Label("Foo"));

    final DesktopNotifier c = new DesktopNotifier();
    getMainWindow().addComponent(c);

    getMainWindow().addComponent(
        new Button("Make notifications awesome", new Button.ClickListener() {
          public void buttonClick(final ClickEvent event) {
            c.requestPermission();
          }
        }));
    getMainWindow().addComponent(
        new Button("show notificaiton", new Button.ClickListener() {
          public void buttonClick(final ClickEvent event) {
            c.showNotification("", "Awesome Notification", "This is a "
                + "notification your notifications wish they were.");
            c.showHtmlNotification(new ThemeResource("notification.html"));
          }
        }));
  }

}
