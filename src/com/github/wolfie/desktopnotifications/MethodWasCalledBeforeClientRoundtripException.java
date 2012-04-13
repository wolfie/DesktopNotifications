package com.github.wolfie.desktopnotifications;

public class MethodWasCalledBeforeClientRoundtripException extends
    RuntimeException {
  private static final long serialVersionUID = 1566608245949998198L;

  public MethodWasCalledBeforeClientRoundtripException() {
  }

  public MethodWasCalledBeforeClientRoundtripException(final String arg0) {
    super(arg0);
  }

  public MethodWasCalledBeforeClientRoundtripException(final Throwable arg0) {
    super(arg0);
  }

  public MethodWasCalledBeforeClientRoundtripException(final String arg0,
      final Throwable arg1) {
    super(arg0, arg1);
  }
}
