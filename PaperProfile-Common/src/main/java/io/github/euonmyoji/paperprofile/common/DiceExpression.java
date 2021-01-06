package io.github.euonmyoji.paperprofile.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author yinyangshi
 */
public class DiceExpression {
    private static Random random = new Random();
    private final String raw;
    private final LinkedList<DiceExpression> child = new LinkedList<>();
    private final List<Operator> ops = new LinkedList<>();
    private final List<Character> opsChar = new LinkedList<>();
    private final ExprType exprType;
    private int n;
    private int v;
    private int[] result;
    private int total;

    public DiceExpression(String s) throws DiceException {
        this(s.replace(" ", ""), true);
    }

    private DiceExpression(DiceExpression a, DiceExpression b, Operator o) {
        this.raw = a.getRaw() + o.getChar() + b.getRaw();
        this.child.add(a);
        this.ops.add(o);
        this.child.add(b);

        this.total = o.op(a.total, b.total);
        exprType = ExprType.DEF;
    }

    private DiceExpression(String s, boolean root) throws DiceException {
        this.raw = s;
        int i = 0;
        int rootStart = 0;
        try {
            while (i < s.length()) {
                char c = s.charAt(i);
                switch (c) {
                    case '(': {
                        int brace = 1;
                        //skip '('
                        i += 1;
                        int start = i;
                        w:
                        while (i < s.length()) {
                            switch (s.charAt(i)) {
                                case '(': {
                                    brace += 1;
                                    break;
                                }
                                case ')': {
                                    brace -= 1;
                                    if (brace == 0) {
                                        break w;
                                    }
                                    break;
                                }
                            }
                            i += 1;
                        }
                        if (brace > 0) {
                            throw new DiceException(s, "bracket mismatch", "pp.dice.expr.bm");
                        }
                        //index now is point )
                        child.add(new DiceExpression(s.substring(start, i), false));
                        rootStart = i + 1;
                        break;
                    }
                    case ')': {
                        throw new DiceException(s, "bracket mismatch", "pp.dice.expr.bm");
                    }
                    case '+':
                    case '-': {
                        if (rootStart != i) {
                            child.add(new DiceExpression(s.substring(rootStart, i), false));
                        }
                        ops.add(getOperator(c));
                        rootStart = i + 1;
                        break;
                    }
                    case '*':
                    case '/':
                    case '%': {
                        Operator op = getOperator(c);
                        DiceExpression lv = rootStart == i ? child.removeLast() : new DiceExpression(s.substring(rootStart, i), false);
                        i += 1;
                        int start = i;
                        //skip first n or op
                        i += 1;
                        while (i < s.length()) {
                            if (isOperator(s.charAt(i))) {
                                break;
                            }
                            i += 1;
                        }
                        DiceExpression diceExpression = new DiceExpression(s.substring(start, i), false);
                        child.add(new DiceExpression(lv, diceExpression, op));
                        rootStart = i;
                        //make next point op
                        i -= 1;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                i += 1;
            }
            if (rootStart != i) {
                String value = s.substring(rootStart);
                if (value.contains("d")) {
                    String[] arg = value.split("d", 2);
                    int n;
                    int v;
                    if (value.startsWith("d")) {
                        n = 1;
                    } else {
                        n = Integer.parseInt(arg[0]);
                    }
                    v = Integer.parseInt(arg[1]);
                    child.add(new DiceExpression(n, v));
                } else {
                    child.add(new DiceExpression(Integer.parseInt(value)));
                }
                if (child.size() == 1) {
                    this.n = child.getFirst().n;
                    this.v = child.getFirst().v;
                    this.result = child.getFirst().result;
                    this.total = child.getFirst().total;
                    this.exprType = child.getFirst().exprType;
                    this.child.clear();
                    if (ops.size() == 1 && ops.get(0).getChar() == '-') {
                        this.total = -this.total;
                    }
                    return;
                }
            }

            if (ops.size() + 1 != child.size()) {
                throw new DiceException(s, "operator mismatch", "pp.dice.expr.om");
            }

            Iterator<DiceExpression> diceExpressionIterator = this.child.iterator();
            Iterator<Operator> opIt = this.ops.iterator();
            DiceExpression cur = diceExpressionIterator.next();
            this.total = cur.total;
            while (diceExpressionIterator.hasNext()) {
                DiceExpression next = diceExpressionIterator.next();
                this.total = opIt.next().op(total, next.total);
            }
            this.exprType = ExprType.DEF;
        } catch (DiceException e) {
            if (root) {
                //show the full raw string.
                throw new DiceException("in expression '" + s + "' that ", e.getMessage(), e.getTip(), e, e.args);
            } else {
                //cost all the power.jpg
                throw e;
            }
        } catch (Exception e) {
            throw new DiceException(s, e.getMessage(), "pp.dice.expr.e", e);
        }

    }

    private DiceExpression(int n, int v) throws DiceException {
        if (n <= 0 || n > 100) {
            throw new DiceException(n + "d" + v, "cannot dice", "pp.dice.expr.cd", n + "");
        }
        if (v <= 0 || v > 1000) {
            throw new DiceException(n + "d" + v, "no dice", "pp.dice.expr.nd", v + "");
        }
        this.raw = n + "d" + v;
        this.n = n;
        this.v = v;
        this.result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = random.nextInt(v) + 1;
            total += result[i];
        }
        exprType = ExprType.RANDOM_VALUE;
    }

    private DiceExpression(int constValue) {
        this.raw = String.valueOf(constValue);
        this.total = constValue;
        exprType = ExprType.CONST;
    }

    private static Operator getOperator(char c) {
        switch (c) {
            case '+': {
                return new Operator(Integer::sum, c);
            }
            case '-': {
                return new Operator((a, b) -> a - b, c);
            }
            case 'X':
            case '*': {
                return new Operator((a, b) -> a * b, c);
            }
            case '/': {
                return new Operator((a, b) -> a / b, c);
            }
            case '%':
            default: {
                //this is unsafe method.jpg
                return new Operator((a, b) -> a % b, c);
            }
        }
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == 'X';
    }

    public static void randomRandom() {
        random = new Random();
    }

    private StringBuilder buildString() {
        if (child.size() == 0) {
            StringBuilder sb = new StringBuilder();
            if (n > 1) {
                sb.append("(");
                for (int i : this.result) {
                    sb.append(i).append("+");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append(")");
            } else {
                sb.append(this.total);
            }
            return sb;
        } else {
            Iterator<DiceExpression> diceExpressionIterator = this.child.iterator();
            Iterator<Operator> opIt = this.ops.iterator();
            StringBuilder sb = diceExpressionIterator.next().buildString();
            while (diceExpressionIterator.hasNext()) {
                sb.append(opIt.next().getChar());
                DiceExpression next = diceExpressionIterator.next();
                sb.append(next.buildString());
            }
            return sb;
        }
    }

    public void calcAgain() {
        switch (exprType) {
            case CONST: {
                break;
            }
            case RANDOM_VALUE: {
                this.total = 0;
                for (int i = 0; i < n; i++) {
                    result[i] = random.nextInt(v) + 1;
                    total += result[i];
                }
                break;
            }
            default: {
                this.total = 0;
                Iterator<DiceExpression> diceExpressionIterator = this.child.iterator();
                Iterator<Operator> opIt = this.ops.iterator();
                DiceExpression cur = diceExpressionIterator.next();
                cur.calcAgain();
                this.total = cur.total;
                while (diceExpressionIterator.hasNext()) {
                    DiceExpression next = diceExpressionIterator.next();
                    next.calcAgain();
                    this.total = opIt.next().op(total, next.total);
                }
                break;
            }
        }

    }

    private boolean isSingle() {
        return this.exprType == ExprType.CONST || (this.exprType == ExprType.DEF && n == 1);
    }

    public String getMsgNode(boolean reason) {
        return (reason ? "pp.dice.result.reason" : "pp.dice.result") + (isSingle() ? "" : ".single");
    }

    public String getRaw() {
        return this.raw;
    }

    @Override
    public String toString() {
        return buildString().toString();
    }

    public int getTotalResult() {
        return total;
    }

    private enum ExprType {
        CONST, RANDOM_VALUE, DEF
    }
}
