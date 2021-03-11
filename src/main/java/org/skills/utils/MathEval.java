package org.skills.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Math Evaluator. Provides the ability to evaluate a String math expression, with support for pureFunctions, variables and
 * standard math constants.
 * <p>
 * Supported Operators:
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
 * @version 2020.3
 */
public final class MathEval {
    /**
     * Special "non-operator" representing an operand.
     */
    private static final Operator OPERAND = new Operator('\0', 0, 0, Side.NONE, false, null);
    private static final Map<String, Double> CONSTANTS = new HashMap<>();
    private static final Map<String, QuantumFunction> FUNCTIONS = new HashMap<>();
    private static final Map<Character, Operator> OPERATORS = new HashMap<>();
    private static Operator OPERATOR_EQUAL; // simple assignment, used as the final

    static {
        registerOperators();
        registerFunctions();

        CONSTANTS.put("E", Math.E);
        CONSTANTS.put("Euler", 0.5772156649015328606065120900824024310421);
        CONSTANTS.put("LN2", 0.693147180559945);
        CONSTANTS.put("LN10", 2.302585092994046);
        CONSTANTS.put("LOG2E", 1.442695040888963);
        CONSTANTS.put("LOG10E", 0.434294481903252);
        CONSTANTS.put("PHI", 1.6180339887498948482);
        CONSTANTS.put("PI", Math.PI);
    }

    //    private final Map<String, Double> variables = new HashMap<>();
    private final String expression;

//*************************************************************************************************
// Internals
//*************************************************************************************************
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
        OPERATORS.put(opr.symbol, opr);
    }

    private static void validateName(String nam) {
        char ch = nam.charAt(0);
        if (!(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z'))
            throw new IllegalArgumentException("Names for constants, variables and functions must start with a letter");
        if (nam.indexOf('(') != -1 || nam.indexOf(')') != -1)
            throw new IllegalArgumentException("Names for constants, variables and functions may not contain a parenthesis");
    }

    private static void registerOperators() {
        Operator opt = new Operator('=', 99, 99, Side.RIGHT, true, (a, b) -> b);
        OPERATOR_EQUAL = opt;
        setOperator(opt);

        setOperator(new Operator('^', 80, 81, Side.NONE, false, Math::pow));
        setOperator(new Operator('±', 60, 60, Side.RIGHT, true, (a, b) -> -b));
        setOperator(new Operator('*', 40, (a, b) -> a * b));
        setOperator(new Operator('·', 40, (a, b) -> a * b));
        setOperator(new Operator('(', 40, (a, b) -> a * b));
        setOperator(new Operator('/', 40, (a, b) -> a / b));
        setOperator(new Operator('÷', 40, (a, b) -> a / b));
        setOperator(new Operator('%', 40, (a, b) -> a % b));
        setOperator(new Operator('+', 20, Double::sum));
        setOperator(new Operator('-', 20, (a, b) -> a - b));

        // Bitwise Operators
        setOperator(new Operator('|', 20, (a, b) -> (double) ((int) a | (int) b)));
        setOperator(new Operator('&', 20, (a, b) -> (double) ((int) a & (int) b)));
        setOperator(new Operator('>', 20, (a, b) -> (double) ((int) a >> (int) b)));
        setOperator(new Operator('<', 20, (a, b) -> (double) ((int) a << (int) b)));
        setOperator(new Operator('$', 20, (a, b) -> (double) ((int) a >>> (int) b)));
        setOperator(new Operator('!', 20, (a, b) -> (double) ((int) a ^ (int) b)));
        setOperator(new Operator('~', 20, (a, b) -> (double) (~(int) b)));
    }

//*************************************************************************************************
//INSTANCE METHODS - PRIVATE IMPLEMENTATION
//*************************************************************************************************

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
        FUNCTIONS.put("max", (p) -> Math.max(p.next(), p.next()));
        FUNCTIONS.put("min", (p) -> Math.min(p.next(), p.next()));
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
    }

    public static double evaluate(String expression) throws NumberFormatException, ArithmeticException {
        return new MathEval(expression).evaluate(0, expression.length() - 1);
    }

    /**
     * Evaluate a complete (sub-)expression.
     * @param beg Inclusive begin offset for subexpression.
     * @param end Inclusive end offset for subexpression.
     */
    private double evaluate(int beg, int end) throws NumberFormatException, ArithmeticException {
        return evaluate(beg, end, 0, MathEval.OPERAND, OPERATOR_EQUAL);
    }

    /**
     * Evaluate the next operand of an expression.
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
                if (signCheck) {
                    if (chr == '-' || chr == '+') {
                        sign = sign ? chr == '+' : chr == '-';
                        signOffset++;
                        continue;
                    }
                    signCheck = false;
                }
                if ((nxt = getOperator(chr)) != MathEval.OPERAND) {
                    if (nxt.internal) nxt = MathEval.OPERAND;
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
                nxt = ofs <= end ? getOperator(expression.charAt(ofs)) : MathEval.OPERAND;                     // modify next operator
            } else if (ch != '-' && !(ch >= '0' && ch <= '9')) {
                if (nxt.symbol == '(') {
                    rgt = doFunction(beg, end);
                    ofs = skipWhitespace(expression, offset + 1, end);                                        // skip past ')' and any following whitespace
                    nxt = ofs <= end ? getOperator(expression.charAt(ofs)) : MathEval.OPERAND;                     // modify next operator
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
                        evaluated = expression.substring(offset, ofs).trim();
                        rgt = Double.parseDouble(evaluated);
                        if (!sign) rgt = -rgt;
                    }
                } catch (NumberFormatException thr) {
                    throw exception(beg, "Invalid numeric value \"" + evaluated + '"');
                }
            }

            if (cur.opPrecedence(Side.LEFT) < nxt.opPrecedence(Side.RIGHT)) {   // correct even for last (non-operator)
                // character, since non-operators have the artificial "precedence" zero
                rgt = evaluate(ofs + 1, end, rgt, cur, nxt);       // from after operator to end of current subexpression
                ofs = offset;           // modify offset to after subexpression
                nxt = (ofs <= end ? getOperator(expression.charAt(ofs)) : MathEval.OPERAND);                         // modify next operator
            }

            lft = doOperation(beg, lft, cur, rgt);
            cur = nxt;

            if (pnd.opPrecedence(Side.LEFT) >= cur.opPrecedence(Side.RIGHT)) break;
            if (cur.symbol == '(') ofs--; // operator omitted for implicit multiplication of subexpression
        }

        if (ofs > end && cur != MathEval.OPERAND) {
            if (cur.unary == Side.LEFT) {
                lft = doOperation(beg, lft, cur, Double.NaN);
            } else {
                throw exception(ofs, "Expression ends with a blank operand after operator '" + nxt.symbol + '\'');
            }
        }
        this.offset = ofs;
        return lft;
    }

    private Operator getOperator(char chr) {
        Operator opr = OPERATORS.get(chr);
        return opr == null ? OPERAND : opr;
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

//*************************************************************************************************
//STATIC NESTED CLASSES - OPERATOR
//*************************************************************************************************

    /**
     * Instance Inner Classes - Function Argument Parser
     */
    private double doFunction(int beg, int end) {
        int index = expression.indexOf('(', beg);
        if (index == -1) throw exception(beg, "Could not find the end of parathensis ')' for function");

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
        String nam = expression.substring(beg, end + 1);
        Double val;

        if ((val = CONSTANTS.get(nam)) != null) return val;
//        if ((val = variables.get(nam)) != null) return val;
        throw exception(beg, "Unrecognized constant or variable \"" + nam + '"');
    }

    private ArithmeticException exception(int ofs, String txt) {
        return new ArithmeticException(txt + " at offset " + ofs + " in expression \"" + expression + '"');
    }

    private ArithmeticException exception(int ofs, String txt, Throwable thr) {
        return new ArithmeticException(txt + " at offset " + ofs + " in expression \"" + expression + '"' + " (Cause: " +
                (thr.getMessage() != null ? thr.getMessage() : thr.toString()) + ')');
    }

    private boolean isHexDecimal(String str, int ofs) {
        return str.length() > ofs + 2 && str.charAt(ofs) == '0' && str.charAt(ofs + 1) == 'x';
    }

    private int skipWhitespace(String exp, int ofs, int end) {
        while (ofs <= end && exp.charAt(ofs) == ' ') ofs++;
        return ofs;
    }

    //*************************************************************************************************
// Constants
//*************************************************************************************************
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

    /**
     * Operator Structure.
     * <p>
     * This class is immutable and threadsafe, but note that whether it can be used in multiple MathEval instances (as
     * opposed to for multiple operators in one instance) depends on the threadsafety of the handler it contains.
     */
    private static final class Operator {
        private final char symbol;
        private final int precedenceL;   // precedence when on the left
        private final int precedenceR;  // precedence when on the right
        private final Side unary;
        private final boolean internal; // internal pseudo operator
        private final TriFunction function;

        /**
         * Create a binary operator with the same precedence on the left and right.
         */
        public Operator(char sym, int prc, TriFunction function) {
            this(sym, prc, prc, Side.NONE, false, function);
        }

        Operator(char sym, int prclft, int prcrgt, Side side, boolean intern, TriFunction function) {
            this.symbol = sym;
            this.precedenceL = prclft;
            this.precedenceR = prcrgt;
            this.unary = side;
            this.internal = intern;
            this.function = function;
        }

        @Override
        public String toString() {
            return ("MathOperator['" + symbol + "']");
        }

        private int opPrecedence(Side sid) {
            if (unary == Side.NONE || unary != sid) // Operator is binary or is unary and bound to the operand on the other side
                return sid == Side.LEFT ? precedenceL : precedenceR;
            else // Operator is unary and associates with the operand on this side
                return Integer.MAX_VALUE;
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
         * @throws ArithmeticException If there are no more arguments.
         */
        public double next() {
            char chr = expression.charAt(index);
            if (chr == ')') throw exception(index, "Function has too few arguments");

            if (chr == ',' || chr == ';') index++;
            double evaluatedParam = evaluate(index, end);
            this.index = offset;
            return evaluatedParam;
        }

        /**
         * Test whether there is another argument to parse.
         */
        private boolean hasNext() {
            return expression.charAt(index) != ')';
        }
    }
}