package io.forestframework.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MultiCauseException extends RuntimeException {
    private final List<Throwable> causes;

    public MultiCauseException(Throwable... causes) {
        this.causes = Collections.unmodifiableList(Arrays.asList(causes));
    }

    public MultiCauseException(String message, Throwable... causes) {
        super(message);
        this.causes = Collections.unmodifiableList(Arrays.asList(causes));
    }

    public List<? extends Throwable> getCauses() {
        return causes;
    }

    @Override
    public Throwable getCause() {
        return causes.isEmpty() ? null : causes.get(0);
    }

    @Override
    public void printStackTrace(PrintStream printStream) {
        PrintWriter writer = new PrintWriter(printStream);
        printStackTrace(writer);
        writer.flush();
    }

    @Override
    public void printStackTrace(PrintWriter printWriter) {
        if (causes.isEmpty()) {
            super.printStackTrace(printWriter);
            return;
        }

        super.printStackTrace(printWriter);

        if (causes.size() == 1) {
            printSingleCauseStackTrace(printWriter);
        } else {
            printMultiCauseStackTrace(printWriter);
        }
    }

    private void printSingleCauseStackTrace(PrintWriter printWriter) {
        Throwable cause = causes.get(0);
        printWriter.print("Caused by: ");
        cause.printStackTrace(printWriter);
    }

    private void printMultiCauseStackTrace(PrintWriter printWriter) {
        for (int i = 0; i < causes.size(); i++) {
            Throwable cause = causes.get(i);
            printWriter.format("Cause %s: ", i + 1);
            cause.printStackTrace(printWriter);
        }
    }
}
