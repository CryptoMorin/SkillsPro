package org.skills.utils;

import java.util.function.Function;

public final class BooleanEval {
    public static boolean evaluate(String expression, Function<String, String> function) {
        return evaluate(expression, function, false);
    }

    private static boolean evaluate(String expression, Function<String, String> function, boolean singular) {
        int subExpressionIndex = expression.indexOf('(');
        while (subExpressionIndex > -1) {
            String subExpression = getSubExpression(subExpressionIndex + 1, expression);
            int len = subExpression.length();
            int from = subExpressionIndex + len;

            if (hasLogicOperator(subExpression)) {
                boolean isTrue = evaluate(subExpression, function);
                expression = expression.substring(0, subExpressionIndex) + isTrue + expression.substring(from + 2);
            }

            subExpressionIndex = expression.indexOf('(', from);
        }

        boolean stillSingle = singular;
        int len = expression.length();
        for (int i = 0; i < len; i++) {
            char ch = expression.charAt(i);
            if (isLogicOperator(ch)) {
                if (ch == '!' && expression.charAt(i + 1) != '=') continue;
                try {
                    Pair<Integer, BooleanOperator> logicResult = getLogicOperator(i, expression);
                    BooleanOperator operator = logicResult.getValue();
                    if (operator != BooleanOperator.AND && operator != BooleanOperator.OR) {
                        if (!singular) {
                            i++;
                            continue;
                        }
                    } else stillSingle = false;

                    int keySize = logicResult.getKey();
                    String left = expression.substring(0, i).trim();
                    String right = expression.substring(i + keySize).trim();
                    if (function != null) {
                        left = function.apply(left);
                        right = function.apply(right);
                    }

                    return operator.evaluate(left, right);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (stillSingle && !singular) return false;

        expression = expression.trim();
        boolean reverse = expression.charAt(0) == '!';
        if (reverse) expression = expression.substring(1);
        if (function != null) expression = function.apply(expression);

        len = expression.length();
        if (len == 5 || len == 4) {
            if (expression.equals("true")) return !reverse;
            if (expression.equals("false")) return reverse;
        }

        if (stillSingle && singular) throw new IllegalArgumentException("Unknown boolean expression: " + expression);
        return evaluate(expression, function, true);
    }

    private static boolean hasLogicOperator(String expression) {
        int len = expression.length();
        for (int i = 0; i < len; i++) {
            if (isLogicOperator(expression.charAt(i))) return true;
        }
        return false;
    }

    private static String getSubExpression(int start, String expression) {
        int params = 1;
        int len = expression.length();

        for (int i = start; i < len; i++) {
            char ch = expression.charAt(i);

            if (ch == '(') params++;
            else if (ch == ')' && --params == 0) return expression.substring(start, i);
        }

        throw new IllegalArgumentException("Invalid sub expression in expression: " + expression);
    }

    private static boolean isLogicOperator(char letter) {
        return letter == '<' || letter == '>' || letter == '!' || letter == '=' || letter == '&' || letter == '|';
    }

    private static Pair<Integer, BooleanOperator> getLogicOperator(int start, String expression) {
        for (BooleanOperator operator : BooleanOperator.OPERATORS) {
            for (String symbol : operator.symbols) {
                int size = symbol.length();
                String fullOperator = expression.substring(start, start + size);
                if (symbol.equals(fullOperator)) return Pair.of(size, operator);
            }
        }

        throw new IllegalArgumentException(start + " Unknown logical operator starting from: '" + expression.substring(start) + "' in expression: " + expression);
    }

    private enum BooleanOperator {
        AND("&&") {
            @Override
            boolean evaluate(String left, String right) {
                return BooleanEval.evaluate(left, null, true) && BooleanEval.evaluate(right, null, true);
            }
        },
        OR("||") {
            @Override
            boolean evaluate(String left, String right) {
                return BooleanEval.evaluate(left, null, true) || BooleanEval.evaluate(right, null, true);
            }
        },
        NOT_EQUALS("!=") {
            @Override
            boolean evaluate(String left, String right) {
                try {
                    double first = MathUtils.evaluateEquation(left);
                    double second = MathUtils.evaluateEquation(right);
                    return first == second;
                } catch (Exception ex) {
                    return !left.equals(right);
                }
            }
        },
        EQUALS("=", "==") {
            @Override
            boolean evaluate(String left, String right) {
                try {
                    double first = MathUtils.evaluateEquation(left);
                    double second = MathUtils.evaluateEquation(right);
                    return first == second;
                } catch (Exception ex) {
                    return left.equals(right);
                }
            }
        },
        LESS_THAN_OR_EQUAL("<=") {
            @Override
            boolean evaluate(String left, String right) {
                double first = MathUtils.evaluateEquation(left);
                double second = MathUtils.evaluateEquation(right);
                return first <= second;
            }
        },
        LESS_THAN("<") {
            @Override
            boolean evaluate(String left, String right) {
                double first = MathUtils.evaluateEquation(left);
                double second = MathUtils.evaluateEquation(right);
                return first < second;
            }
        },
        GREATER_THAN_OR_EQUAL(">=") {
            @Override
            boolean evaluate(String left, String right) {
                double first = MathUtils.evaluateEquation(left);
                double second = MathUtils.evaluateEquation(right);
                return first >= second;
            }
        },
        GREATER_THAN(">") {
            @Override
            boolean evaluate(String left, String right) {
                double first = MathUtils.evaluateEquation(left);
                double second = MathUtils.evaluateEquation(right);
                return first > second;
            }
        };

        private static final BooleanOperator[] OPERATORS = values();
        private final String[] symbols;

        BooleanOperator(String... symbols) {
            this.symbols = symbols;
        }

        abstract boolean evaluate(String left, String right);
    }
}
