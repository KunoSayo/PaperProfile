package io.github.euonmyoji.paperprofile.common;

/**
 * @author yinyangshi
 */
public class DiceException extends Exception {
    public final String[] args;
    private final String rawExpression;
    private final String tip;

    public DiceException(String rawExpression, String msg, String tip, String... args) {
        super("'" + rawExpression + "'" + msg);
        this.rawExpression = rawExpression;
        this.tip = tip;
        this.args = args;
    }

    public DiceException(String rawExpression, String msg, String tip, Throwable cause, String... args) {
        super("'" + rawExpression + "'" + msg, cause);
        this.rawExpression = rawExpression;
        this.tip = tip;
        this.args = args;
    }

    public String getExpression() {
        return this.rawExpression;
    }

    public String getTip() {
        return this.tip;
    }
}
