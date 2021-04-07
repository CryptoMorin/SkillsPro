package org.skills.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Math Evaluator. Provides the ability to evaluate a String math expression, with support for functions, operators, variables, constants and ...
 * <p>
 * Supported Operators: (outdated)
 * <pre>
 *     Operator  Precedence  Unary Binding  Description
 *     --------- ----------- -------------- ------------------------------------------------
 *     '='       99 / 99     RIGHT_SIDE     Simple assignment (internal, used for the final operation)
 *     '^'       80 / 81     NO_SIDE        Power
 *     '±'       60 / 60     RIGHT_SIDE     Unary negation (internal, substituted for '-')
 *     '*'       40 / 40     NO_SIDE        Multiple (conventional computer notation)
 *     '×'       40 / 40     NO_SIDE        Multiple (because it's a Unicode world out there)
 *     '·'       40 / 40     NO_SIDE        Multiple (because it's a Unicode world out there)
 *     '('       40 / 40     NO_SIDE        Multiply (implicit due to brackets, e.g "(a)(b)")
 *     '/'       40 / 40     NO_SIDE        Divide (conventional computer notation)
 *     '÷'       40 / 40     NO_SIDE        Divide (because it's a Unicode world out there)
 *     '%'       40 / 40     NO_SIDE        Remainder
 *     '+'       20 / 20     NO_SIDE        Add/unary-positive
 *     '-'       20 / 20     NO_SIDE        Subtract/unary-negative
 * </pre>
 * <p>Note that this class should be opened in UTF-8</p>
 *
 * @author Crypto Morin
 * @version 2020.4
 */
public final class MathEval {
    /**
     * Special "non-operator" representing an operand.
     */
    private static final Map<String, Double> CONSTANTS = new HashMap<>(8);
    private static final Map<String, QuantumFunction> FUNCTIONS = new HashMap<>(44);
    private static final Operator[] OPERATORS = new Operator[127]; // 126 for ~
    private static final Operator OPERAND = new Operator('\0', 0, 0, Side.NONE, null);
    private static final Operator OPERATOR_EQUAL = new Operator('=', Byte.MAX_VALUE, Byte.MAX_VALUE, Side.RIGHT, (a, b) -> b); // simple assignment, used as the final

    static {
        registerOperators();
        registerFunctions();
        registerConstants();
    }

    //    private final Map<String, Double> variables = new HashMap<>();
    private final String expression;

    /**
     * Used when returning from a higher precedence sub-expression evaluation.
     */
    private int offset;

    private MathEval(String expression) {
        this.expression = expression;
    }

    /**
     * Set a custom operator, replacing any existing operator with the same symbol. Operators cannot be removed, only replaced.
     */
    public static void setOperator(Operator opr) {
        if (opr.symbol >= OPERATORS.length) throw new IllegalArgumentException("Operator handler cannot handle char '" + opr.symbol + "' with char code: " + ((int) opr.symbol));
        OPERATORS[opr.symbol] = opr;
//        OPERATORS.put(opr.symbol, opr);
    }

    @SuppressWarnings("unused")
    private static void validateName(String nam) {
        char ch = nam.charAt(0);
        if (!(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z'))
            throw new IllegalArgumentException("Names for constants, variables and functions must start with a letter");
        if (nam.indexOf('(') != -1 || nam.indexOf(')') != -1)
            throw new IllegalArgumentException("Names for constants, variables and functions may not contain a parenthesis");
    }

    private static void registerConstants() {
        CONSTANTS.put("E", Math.E);
        CONSTANTS.put("Euler", 0.577215664901532860606512);
        CONSTANTS.put("LN2", 0.693147180559945);
        CONSTANTS.put("LN10", 2.302585092994046);
        CONSTANTS.put("LOG2E", 1.442695040888963);
        CONSTANTS.put("LOG10E", 0.434294481903252);
        CONSTANTS.put("PHI", 1.6180339887498948482);
        CONSTANTS.put("PI", Math.PI);
    }

    private static void registerOperators() {
        setOperator(OPERATOR_EQUAL);

        // https://en.cppreference.com/w/c/language/operator_precedence
        setOperator(new Operator('^', 12, 13, Side.NONE, Math::pow));
        //setOperator(new Operator('±', 10, 10, Side.RIGHT, true, (a, b) -> -b));
        setOperator(new Operator('*', 10, (a, b) -> a * b));
        //setOperator(new Operator('·', 10, (a, b) -> a * b));
        setOperator(new Operator('(', 10, (a, b) -> a * b));
        setOperator(new Operator('/', 10, (a, b) -> a / b));
        //setOperator(new Operator('÷', 10, (a, b) -> a / b));
        setOperator(new Operator('%', 10, (a, b) -> a % b));
        setOperator(new Operator('+', 9, Double::sum));
        setOperator(new Operator('-', 9, (a, b) -> a - b));

        // Bitwise Operators
        setOperator(new Operator('~', 10, (a, b) -> ~(long) b));
        setOperator(new Operator('@', 8, (a, b) -> Long.rotateLeft((long) a, (int) b)));  // Rotate Left
        setOperator(new Operator('#', 8, (a, b) -> Long.rotateRight((long) a, (int) b))); // Rotate Right
        setOperator(new Operator('>', 8, (a, b) -> (long) a >> (long) b));
        setOperator(new Operator('<', 8, (a, b) -> (long) a << (long) b));
        setOperator(new Operator('$', 8, (a, b) -> (long) a >>> (long) b));// NOT
        setOperator(new Operator('&', 7, (a, b) -> (long) a & (long) b));
        setOperator(new Operator('!', 6, (a, b) -> (long) a ^ (long) b));
        setOperator(new Operator('|', 5, (a, b) -> (long) a | (long) b));// XOR
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    private static void registerFunctions() {
        FUNCTIONS.put("abs", (p) -> Math.abs(p.next()));
        FUNCTIONS.put("acos", (p) -> Math.acos(p.next()));
        FUNCTIONS.put("asin", (p) -> Math.asin(p.next()));
        FUNCTIONS.put("atan", (p) -> Math.atan(p.next()));
        FUNCTIONS.put("cbrt", (p) -> Math.cbrt(p.next()));
        FUNCTIONS.put("ceil", (p) -> Math.ceil(p.next()));
        FUNCTIONS.put("cos", (p) -> Math.cos(p.next()));
        FUNCTIONS.put("cosh", (p) -> Math.cosh(p.next()));
        FUNCTIONS.put("exp", (p) -> Math.exp(p.next()));
        FUNCTIONS.put("expm1", (p) -> Math.expm1(p.next()));
        FUNCTIONS.put("floor", (p) -> Math.floor(p.next()));
        FUNCTIONS.put("getExponent", (p) -> Math.getExponent(p.next()));
        FUNCTIONS.put("log", (p) -> Math.log(p.next()));
        FUNCTIONS.put("log10", (p) -> Math.log10(p.next()));
        FUNCTIONS.put("log1p", (p) -> Math.log1p(p.next()));
        FUNCTIONS.put("max", (p) -> {
            double a = p.next(), b = p.next();
            return (a >= b) ? a : b;
        });
        FUNCTIONS.put("min", (p) -> {
            double a = p.next(), b = p.next();
            return (a <= b) ? a : b;
        });
        FUNCTIONS.put("nextUp", (p) -> Math.nextUp(p.next()));
        FUNCTIONS.put("nextDown", (p) -> Math.nextDown(p.next()));
        FUNCTIONS.put("nextAfter", (p) -> Math.nextAfter(p.next(), p.next()));
        FUNCTIONS.put("random", (p) -> ThreadLocalRandom.current().nextDouble(p.next(), p.next() + 1));
        FUNCTIONS.put("randInt", (p) -> ThreadLocalRandom.current().nextInt((int) p.next(), (int) p.next() + 1));
        FUNCTIONS.put("round", (p) -> Math.round(p.next()));
        FUNCTIONS.put("rint", (p) -> Math.rint(p.next()));
        FUNCTIONS.put("signum", (p) -> Math.signum(p.next()));
        FUNCTIONS.put("whatPercentOf", (p) -> (p.next() / p.next()) * 100);
        FUNCTIONS.put("percentOf", (p) -> (p.next() / 100) * p.next());
        FUNCTIONS.put("sin", (p) -> Math.sin(p.next()));
        FUNCTIONS.put("sinh", (p) -> Math.sinh(p.next()));
        FUNCTIONS.put("bits", (p) -> Double.doubleToRawLongBits(p.next()));
        FUNCTIONS.put("hash", (p) -> Double.hashCode(p.next()));
        FUNCTIONS.put("identityHash", (p) -> System.identityHashCode(p.next()));
        FUNCTIONS.put("time", (p) -> System.currentTimeMillis());
        FUNCTIONS.put("sqrt", (p) -> Math.sqrt(p.next()));
        FUNCTIONS.put("tan", (p) -> Math.tan(p.next()));
        FUNCTIONS.put("tanh", (p) -> Math.tanh(p.next()));
        FUNCTIONS.put("toDegrees", (p) -> Math.toDegrees(p.next()));
        FUNCTIONS.put("toRadians", (p) -> Math.toRadians(p.next()));
        FUNCTIONS.put("ulp", (p) -> Math.ulp(p.next()));
        FUNCTIONS.put("scalb", (p) -> Math.scalb(p.next(), (int) p.next()));
        FUNCTIONS.put("hypot", (p) -> Math.hypot(p.next(), p.next()));
        FUNCTIONS.put("copySign", (p) -> Math.copySign(p.next(), p.next()));
        FUNCTIONS.put("IEEEremainder", (p) -> Math.IEEEremainder(p.next(), p.next()));
        FUNCTIONS.put("naturalSum", (p) -> {
            int n = (int) p.next();
            return n * (n + 1) / 2.0;
        });
        FUNCTIONS.put("reverse", (p) -> Long.reverse((long) p.next()));
        FUNCTIONS.put("reverseBytes", (p) -> Long.reverseBytes((long) p.next()));

        // https://www.tutorialspoint.com/unix/unix-relational-operators.htm
//        FUNCTIONS.put("gt", (p) -> p.next() > p.next() ? p.next() : p.next(2));
//        FUNCTIONS.put("lt", (p) -> p.next() < p.next() ? p.next() : p.next(2));
//        FUNCTIONS.put("ge", (p) -> p.next() >= p.next() ? p.next() : p.next(2));
//        FUNCTIONS.put("le", (p) -> p.next() <= p.next() ? p.next() : p.next(2));
    }

    public static double evaluate(String expression) throws NumberFormatException, ArithmeticException {
        return new MathEval(expression).evaluate(0, expression.length() - 1);
    }

    private static Operator getOperator(char chr) {
        if (chr >= OPERATORS.length) return OPERAND;
        Operator opr = OPERATORS[chr];
        return opr == null ? OPERAND : opr;
    }

    private static boolean isHexDecimal(String str, int ofs) {
        return str.length() > ofs + 2 && str.charAt(ofs) == '0' && str.charAt(ofs + 1) == 'x';
    }

    private static int skipWhitespace(String exp, int ofs, int end) {
        while (ofs <= end && exp.charAt(ofs) == ' ') ofs++;
        return ofs;
    }

    /**
     * Evaluate a complete (sub-)expression.
     *
     * @param beg Inclusive begin offset for subexpression.
     * @param end Inclusive end offset for subexpression.
     */
    private double evaluate(int beg, int end) throws NumberFormatException, ArithmeticException {
        return evaluate(beg, end, 0, OPERAND, OPERATOR_EQUAL);
    }

    private boolean isScientificNotation(int index) {
        char notation = expression.charAt(index);
        return notation == 'E' || notation == 'e'; // Would be optimized to: (notation & ~32) == 'E'
    }

    /**
     * Evaluate the next operand of an expression.
     *
     * @param beg Inclusive begin offset for subexpression.
     * @param end Inclusive end offset for subexpression.
     * @param pnd Pending operator (operator previous to this subexpression).
     * @param lft Left-value with which to initialize this subexpression.
     * @param cur Current operator (the operator for this subexpression).
     */
    private double evaluate(int beg, int end, double lft, Operator pnd, Operator cur) throws NumberFormatException, ArithmeticException {
        Operator nxt = OPERAND;
        int ofs; // current expression offset
        for (ofs = beg; (ofs = skipWhitespace(expression, ofs, end)) <= end; ofs++) {
            int signOffset = 0;
            boolean sign = true;
            boolean signCheck = true;

            for (beg = ofs; ofs <= end; ofs++) {
                char chr = expression.charAt(ofs);
                boolean isSign = chr == '-' || chr == '+';

                if (isSign && ofs != 0 && isScientificNotation(ofs - 1)) continue;
                if (signCheck) {
                    if (isSign) {
                        sign = sign ? chr == '+' : chr == '-';
                        signOffset++;
                        continue;
                    }
                    signCheck = false;
                }

                if ((nxt = getOperator(chr)) != OPERAND) {
                    if (nxt == OPERATOR_EQUAL) nxt = OPERAND;
                    else break; // must kill operator to prevent spurious "Expression ends with a blank sub-expression" at end of function
                } else if (chr == ')' || chr == ',' || chr == ';') break; // end of subexpression or function argument.
            }

            char ch = expression.charAt(beg);
            double rgt; // next operand (right-value) to process

            if (beg == ofs && (cur.unary == Side.LEFT || nxt.unary == Side.RIGHT)) {
                rgt = Double.NaN; // Left-binding unary operator; right value will not be used and should be blank.
            } else if (ch == '(') {
                rgt = evaluate(beg + 1, end);
                ofs = skipWhitespace(expression, offset + 1, end);                                        // skip past ')' and any following whitespace
                nxt = ofs <= end ? getOperator(expression.charAt(ofs)) : OPERAND;                     // modify next operator
            } else if (ch != '-' && !(ch >= '0' && ch <= '9')) {
                if (nxt.symbol == '(') {
                    rgt = doFunction(beg, end);
                    ofs = skipWhitespace(expression, offset + 1, end);                                        // skip past ')' and any following whitespace
                    nxt = ofs <= end ? getOperator(expression.charAt(ofs)) : OPERAND;                     // modify next operator
                } else {
                    rgt = getVariable(beg, ofs - 1);
                }
            } else {
                String evaluated = null;
                try {
                    int offset = beg + signOffset;
                    if (isHexDecimal(expression, beg)) {
                        offset += 2;
                        evaluated = expression.substring(offset, ofs).trim();
                        rgt = (double) Long.parseLong(evaluated, 16);
                    } else {
                        evaluated = expression.substring(offset, ofs);
                        rgt = Double.parseDouble(evaluated);
                    }

                    if (!sign) rgt = -rgt;
                } catch (NumberFormatException thr) {
                    throw exception(beg, "Invalid numeric value \"" + evaluated + '"');
                }
            }

            if (cur.opPrecedence(Side.LEFT) < nxt.opPrecedence(Side.RIGHT)) {   // correct even for last (non-operator)
                // character, since non-operators have the artificial "precedence" zero
                rgt = evaluate(ofs + 1, end, rgt, cur, nxt);       // from after operator to end of current subexpression
                ofs = offset;           // modify offset to after subexpression
                nxt = ofs <= end ? getOperator(expression.charAt(ofs)) : OPERAND; // modify next operator
            }

            lft = doOperation(beg, lft, cur, rgt);
            cur = nxt;

            if (pnd.opPrecedence(Side.LEFT) >= cur.opPrecedence(Side.RIGHT)) break;
            if (cur.symbol == '(') ofs--; // operator omitted for implicit multiplication of subexpression
        }

        if (ofs > end && cur != OPERAND) {
            if (cur.unary == Side.LEFT) {
                lft = doOperation(beg, lft, cur, Double.NaN);
            } else {
                throw exception(ofs, "Expression ends with a blank operand after operator '" + nxt.symbol + '\'');
            }
        }

        this.offset = ofs;
        return lft;
    }

    private double doOperation(int beg, double lft, Operator opr, double rgt) {
        if (opr.unary != Side.RIGHT && Double.isNaN(lft)) throw exception(beg, "Mathematical NaN detected in right-operand");
        if (opr.unary != Side.LEFT && Double.isNaN(rgt)) throw exception(beg, "Mathematical NaN detected in left-operand");

        try {
            return opr.function.apply(lft, rgt);
        } catch (ArithmeticException thr) {
            throw exception(beg, "Mathematical expression \"" + expression + "\" failed to evaluate", thr);
        } catch (UnsupportedOperationException thr) {
            int tmp = beg;
            while (tmp > 0 && getOperator(expression.charAt(tmp)) == null) tmp--; // set up for offset of the offending operator
            throw exception(tmp, "Operator \"" + opr.symbol + "\" not handled by math engine (Programmer error: The list of operators is inconsistent within the engine)");
        }
    }

    private double doFunction(int beg, int end) {
        int index = expression.indexOf('(', beg);
        if (index == -1) throw exception(beg, "Could not find the start of parathensis '(' for function");

        // Function name and its arguments.
        String func = expression.substring(beg, index).trim();
        QuantumFunction function = FUNCTIONS.get(func);
        if (function == null) throw exception(beg, "Function \"" + func + "\" not recognized");

        ArgParser funcArgs = new ArgParser(index, end);
        try {
            double result = function.apply(funcArgs);
            if (funcArgs.hasNext()) throw exception(funcArgs.index, "Function has too many arguments");

            this.offset = funcArgs.index;
            return result;
        } catch (NoSuchMethodError thr) {
            throw exception(beg, "Function not supported in this JVM: \"" + func + '"');
        } catch (UnsupportedOperationException thr) {
            throw exception(beg, thr.getMessage());
        } catch (Throwable thr) {
            throw exception(beg, "Unexpected exception parsing function arguments", thr);
        }
    }

    private double getVariable(int beg, int end) {
        while (beg < end && expression.charAt(end) == ' ') end--;

        // since a letter triggers a named value, this can never reduce to beg==end
        String name = expression.substring(beg, end + 1);
        Double val;

        if ((val = CONSTANTS.get(name)) != null) return val;
//        if ((val = variables.get(nam)) != null) return val;
        throw exception(beg, beg + " Unrecognized constant or variable \"" + name + '"');
    }

    private ArithmeticException exception(int ofs, String txt) {
        return new ArithmeticException(txt + " at offset " + ofs + " in expression \"" + expression + '"');
    }

    private ArithmeticException exception(int ofs, String txt, Throwable thr) {
        return new ArithmeticException(txt + " at offset " + ofs + " in expression \"" + expression + '"' + " (Cause: " +
                (thr.getMessage() != null ? thr.getMessage() : thr.toString()) + ')');
    }

    private enum Side {
        /**
         * Operator/operand on on the left.
         */
        RIGHT,
        /**
         * Operator/operand on on the right.
         */
        LEFT,
        /**
         * Operator/operand side is immaterial.
         */
        NONE;
    }

    @FunctionalInterface
    private interface QuantumFunction {
        double apply(ArgParser params);
    }

    @FunctionalInterface
    private interface TriFunction {
        double apply(double a, double b);
    }

    @SuppressWarnings("unused")
    @FunctionalInterface
    public interface VariableFunction {
        double apply(String variable);
    }

    /**
     * Operator Structure.
     * <p>
     * This class is immutable and threadsafe, but note that whether it can be used in multiple MathEval instances (as
     * opposed to for multiple operators in one instance) depends on the threadsafety of the handler it contains.
     */
    private static final class Operator {
        private final char symbol;
        /**
         * https://en.wikipedia.org/wiki/Order_of_operations
         */
        private final byte precedenceLeft;   // precedence when on the left
        private final byte precedenceRight;  // precedence when on the right
        private final Side unary;
        private final TriFunction function;

        /**
         * Create a binary operator with the same precedence on the left and right.
         */
        protected Operator(char sym, int precedence, TriFunction function) {
            this(sym, precedence, precedence, Side.NONE, function);
        }

        Operator(char sym, int precedenceL, int precedenceR, Side side, TriFunction function) {
            this.symbol = sym;
            this.precedenceLeft = (byte) precedenceL;
            this.precedenceRight = (byte) precedenceR;
            this.unary = side;
            this.function = function;
        }

        @Override
        public String toString() {
            return ("MathOperator['" + symbol + "']");
        }

        private byte opPrecedence(Side side) {
            if (unary == Side.NONE || unary != side) // Operator is binary or is unary and bound to the operand on the other side
                return side == Side.LEFT ? precedenceLeft : precedenceRight;
            else // Operator is unary and associates with the operand on this side
                return Byte.MAX_VALUE;
        }
    }

    /**
     * An abstract parser for function arguments.
     */
    private final class ArgParser {
        private final int end;
        private int index;

        ArgParser(int index, int end) {
            this.end = end;
            this.index = skipWhitespace(expression, index + 1, end - 1);
        }

        /**
         * Parse the next argument, throwing an exception if there are no more arguments.
         *
         * @throws ArithmeticException If there are no more arguments.
         */
        public double next() {
            char chr = expression.charAt(index);
            if (chr == ')') throw exception(index, "Function has too few arguments");
            if (chr == ',' || chr == ';') index++;

            /*
            while (index <= end) {
                if (chr == ',' || chr == ';') {
                    index++;
                    break;
                }
                chr = expression.charAt(index++);
            }
             */

            double evaluatedParam = evaluate(index, end);
            this.index = offset;
            return evaluatedParam;
        }

        public double next(int jump) {
            double result = 0;
            for (int i = 0; i < jump; i++) result = next();
            return result;
        }

        /**
         * Test whether there is another argument to parse.
         */
        private boolean hasNext() {
            return expression.charAt(index) != ')';
        }
    }
}