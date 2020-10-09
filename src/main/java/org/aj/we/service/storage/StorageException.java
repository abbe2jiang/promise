package org.aj.we.service.storage;

public class StorageException extends Exception {

  private static final long serialVersionUID = 1L;

  public StorageException(String message) { super(message); }

  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
