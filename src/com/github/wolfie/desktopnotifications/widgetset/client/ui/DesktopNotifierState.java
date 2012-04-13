package com.github.wolfie.desktopnotifications.widgetset.client.ui;

import com.vaadin.terminal.gwt.client.ComponentState;

public class DesktopNotifierState extends ComponentState {
  private static final long serialVersionUID = -1888591892008043579L;

  private String text = "If you wish to enable this feature, you need to click \"Allow\" in the toolbar that appears after you close this window.";

  public String getText() {
    return text;
  }

  public void setText(final String text) {
    this.text = text;
  }
}
