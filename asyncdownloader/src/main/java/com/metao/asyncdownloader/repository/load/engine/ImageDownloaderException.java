package com.metao.asyncdownloader.repository.load.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.metao.asyncdownloader.repository.load.DataSource;
import com.metao.asyncdownloader.repository.load.Key;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An exception with zero or more causes indicating why a load in ImageDownloader failed.
 */
public final class ImageDownloaderException extends Exception {
  private static final StackTraceElement[] EMPTY_ELEMENTS = new StackTraceElement[0];

  private final List<Exception> causes;
  private Key key;
  private DataSource dataSource;
  private Class<?> dataClass;

  public ImageDownloaderException(String message) {
    this(message, Collections.<Exception>emptyList());
  }

  public ImageDownloaderException(String detailMessage, Exception cause) {
    this(detailMessage, Collections.singletonList(cause));
  }

  public ImageDownloaderException(String detailMessage, List<Exception> causes) {
    super(detailMessage);
    setStackTrace(EMPTY_ELEMENTS);
    this.causes = causes;
  }

  void setLoggingDetails(Key key, DataSource dataSource) {
    setLoggingDetails(key, dataSource, null);
  }

  void setLoggingDetails(Key key, DataSource dataSource, Class<?> dataClass) {
    this.key = key;
    this.dataSource = dataSource;
    this.dataClass = dataClass;
  }

  @Override
  public Throwable fillInStackTrace() {
    // Avoid an expensive allocation by doing nothing here. Causes should contain all relevant
    // stack traces.
    return this;
  }

  /**
   * Returns a list of causes that are immediate children of this exception.
   *
   * <p>Causes may or may not be {@link ImageDownloaderException ImageDownloaderExceptions}. Causes may also not be root
   * causes, and in turn my have been caused by other failures.</p>
   *
   * @see #getRootCauses()
   */
  public List<Exception> getCauses() {
    return causes;
  }

  /**
   * Returns the list of root causes that are the leaf nodes of all children of this exception.
   *
   * <p>Use this method to do things like look for http exceptions that indicate the load may have
   * failed due to an error that can be retried. Keep in mind that because ImageDownloader may attempt to load
   * a given model using multiple different pathways, there may be multiple related or unrelated
   * reasons for a load to fail.
   */
  public List<Exception> getRootCauses() {
    List<Exception> rootCauses = new ArrayList<>();
    addRootCauses(this, rootCauses);
    return rootCauses;
  }

  /**
   * Logs all root causes using the given tag.
   *
   * <p>Each root cause is logged separately to avoid throttling. {@link #printStackTrace()} will
   * provide a more succinct overview of why the exception occurred, although it does not include
   * complete stack traces.
   */
  public void logRootCauses(String tag) {
    Log.e(tag, getClass() + ": " + getMessage());
    List<Exception> causes = getRootCauses();
    for (int i = 0, size = causes.size(); i < size; i++) {
      Log.i(tag, "Root cause (" + (i + 1) + " of " + size + ")", causes.get(i));
    }
  }

  private void addRootCauses(Exception exception, List<Exception> rootCauses) {
    if (exception instanceof ImageDownloaderException) {
      ImageDownloaderException ImageDownloaderException = (ImageDownloaderException) exception;
      for (Exception e : ImageDownloaderException.getCauses()) {
        addRootCauses(e, rootCauses);
      }
    } else {
      rootCauses.add(exception);
    }
  }

  @Override
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  @Override
  public void printStackTrace(PrintStream err) {
    printStackTrace((Appendable) err);
  }

  @Override
  public void printStackTrace(PrintWriter err) {
    printStackTrace((Appendable) err);
  }

  private void printStackTrace(Appendable appendable) {
    appendExceptionMessage(this, appendable);
    appendCauses(getCauses(), new IndentedAppendable(appendable));
  }

  @Override
  public String getMessage() {
    return super.getMessage()
        + (dataClass != null ? ", " + dataClass : "")
        + (dataSource != null ? ", " + dataSource : "")
        + (key != null ? ", " + key : "");
  }

  // Appendable throws, PrintWriter, PrintStream, and IndentedAppendable do not, so this should
  // never happen.
  @SuppressWarnings("PMD.PreserveStackTrace")
  private static void appendExceptionMessage(Exception e, Appendable appendable) {
    try {
      appendable.append(e.getClass().toString()).append(": ").append(e.getMessage()).append('\n');
    } catch (IOException e1) {
      throw new RuntimeException(e);
    }
  }

  // Appendable throws, PrintWriter, PrintStream, and IndentedAppendable do not, so this should
  // never happen.
  @SuppressWarnings("PMD.PreserveStackTrace")
  private static void appendCauses(List<Exception> causes, Appendable appendable) {
    try {
      appendCausesWrapped(causes, appendable);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  private static void appendCausesWrapped(List<Exception> causes, Appendable appendable)
      throws IOException {
    int size = causes.size();
    for (int i = 0; i < size; i++) {
      appendable.append("Cause (")
          .append(String.valueOf(i + 1))
          .append(" of ")
          .append(String.valueOf(size))
          .append("): ");

      Exception cause = causes.get(i);
      if (cause instanceof ImageDownloaderException) {
        ImageDownloaderException ImageDownloaderCause = (ImageDownloaderException) cause;
        ImageDownloaderCause.printStackTrace(appendable);
      } else {
        appendExceptionMessage(cause, appendable);
      }
    }
  }

  private static final class IndentedAppendable implements Appendable {
    private static final String EMPTY_SEQUENCE = "";
    private static final String INDENT = "  ";
    private final Appendable appendable;
    private boolean printedNewLine = true;

    IndentedAppendable(Appendable appendable) {
      this.appendable = appendable;
    }

    @Override
    public Appendable append(char c) throws IOException {
      if (printedNewLine) {
        printedNewLine = false;
        appendable.append(INDENT);
      }
      printedNewLine = c == '\n';
      appendable.append(c);
      return this;
    }

    @Override
    public Appendable append(@Nullable CharSequence charSequence) throws IOException {
      charSequence = safeSequence(charSequence);
      return append(charSequence, 0, charSequence.length());
    }

    @Override
    public Appendable append(@Nullable CharSequence charSequence, int start, int end)
        throws IOException {
      charSequence = safeSequence(charSequence);
      if (printedNewLine) {
        printedNewLine = false;
        appendable.append(INDENT);
      }
      printedNewLine = charSequence.length() > 0 && charSequence.charAt(end - 1) == '\n';
      appendable.append(charSequence, start, end);
      return this;
    }

    @NonNull
    private CharSequence safeSequence(@Nullable CharSequence sequence) {
      if (sequence == null) {
        return EMPTY_SEQUENCE;
      } else {
        return sequence;
      }
    }
  }
}
