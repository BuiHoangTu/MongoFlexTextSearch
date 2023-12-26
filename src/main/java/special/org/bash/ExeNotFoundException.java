package special.org.bash;

/**
 * Throw when the exe is not in PATH and custom folder
 */
public class ExeNotFoundException extends Exception{
    private static final String FORMAT = "Exe %s not found";

    public ExeNotFoundException() {
        super(FORMAT.formatted(""));
    }

    public ExeNotFoundException(String exeName) {
        super(FORMAT.formatted(exeName));
    }

    public ExeNotFoundException(String exeName, Throwable cause) {
        super(FORMAT.formatted(exeName), cause);
    }

    public ExeNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ExeNotFoundException(String exeName, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(FORMAT.formatted(exeName), cause, enableSuppression, writableStackTrace);
    }
}
