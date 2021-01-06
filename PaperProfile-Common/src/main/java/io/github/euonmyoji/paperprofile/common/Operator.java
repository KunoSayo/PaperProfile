package io.github.euonmyoji.paperprofile.common;

/**
 * @author yinyangshi
 */
public class Operator {
    private final IOperator operator;
    private final char character;

    public Operator(IOperator operator, char character) {
        this.operator = operator;
        this.character = character;
    }

    public int op(int a, int b) {
        return this.operator.op(a, b);
    }

    public char getChar() {
        return character;
    }

    @FunctionalInterface
    public interface IOperator {
        /**
         * a &lt;op&gt; b
         *
         * @param a the number a
         * @param b the number b
         * @return the result after operate
         */
        int op(int a, int b);
    }
}
