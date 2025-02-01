package sg.edu.nus.se.its.validation.solverexpressions;

import java.util.List;
import java.util.Map;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;


import org.javatuples.Pair;
import sg.edu.nus.se.its.validation.SolverWrapper;

/**
 * Represents an n-ary expression that operates on multiple operands with a specified operator.
 * This class can handle operations like maximum, minimum, and sum of an arbitrary number of operands.
 */
public class NaryExpression extends BaseExpression {
    // List of operands for the n-ary expression
    private final List<BaseExpression> operands;
    // Operator for the n-ary operation, now contains "max", "min", and "sum"
    private final String operator;

    /**
     * Constructs a new NaryExpression with the given operands and operator.
     *
     * @param operands the operands of the n-ray expression
     * @param operator the operator of the n-ray expression
     */
    public NaryExpression(List<BaseExpression> operands, String operator) {
        super(null);
        this.operands = operands;
        this.operator = operator;
    }

    public NaryExpression(Pair<BaseExpression,BaseExpression> lower_upper, BaseExpression step, String operator) {
        super(null);
        this.operands = List.of(lower_upper.getValue0(), lower_upper.getValue1(), step);
        this.operator = operator;
    }

    /**
     * Returns a string representation of the n-ray expression.
     *
     * @return a string that represents the n-ary expression
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(operator);
        for (BaseExpression operand : operands) {
            sb.append(" ").append(operand);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Evaluates the n-ary expression in a given context with specified variables and variable remapping.
     * This method supports operations like "max", "min", and "sum" for any number of operands.
     *
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param varRemapping a mapping for variable name remapping
     * @return the Z3 expression resulting from evaluating the n-ary operation
     * @throws IllegalArgumentException if the operator is unsupported
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Expr evaluate(SolverWrapper solverWrapper, Map<String, String> varRemapping) {
        Context ctx = solverWrapper.getCtx();
        if (operands.isEmpty()) {
            throw new IllegalArgumentException("NaryExpression requires at least one operand.");
        }

        Expr[] exprs = new Expr[operands.size()];
        if (!operator.equals("range")) {
            exprs = operands.stream()
                    .map(operand -> operand.evaluate(solverWrapper, varRemapping))
                    .toArray(Expr[]::new);
        }

        switch (operator) {
        case "max":
            return max(ctx, exprs);
        case "min":
            return min(ctx, exprs);
        case "sum":
            return sum(ctx, exprs);
        case "ArrayDeclaration":
            return arrayDeclaration(solverWrapper, exprs);
        case "range":
            if (operands.size() == 1){
                solverWrapper.updateVariable(varRemapping, "range_lower", ctx.mkInt(0));
                solverWrapper.updateVariable(varRemapping, "range_upper", ctx.mkInt(operands.get(0).getValue()));
                solverWrapper.updateVariable(varRemapping, "range_step", ctx.mkInt(1));
                return rangeToArray(
                        solverWrapper,
                        new Pair<>(
                                new BaseExpression("0"),
                                operands.get(0)
                        ),
                        new BaseExpression("1")
                );

            } else if (operands.size() == 2){
                solverWrapper.updateVariable(varRemapping, "range_lower", ctx.mkInt(operands.get(0).getValue()));
                solverWrapper.updateVariable(varRemapping, "range_upper", ctx.mkInt(operands.get(1).getValue()));
                solverWrapper.updateVariable(varRemapping, "range_step", ctx.mkInt(1));

                return rangeToArray(
                        solverWrapper,
                        new Pair<>(
                                operands.get(0),
                                operands.get(1)
                        ),
                        new BaseExpression("1")
                );
            } else {
                solverWrapper.updateVariable(varRemapping, "range_lower", ctx.mkInt(operands.get(0).getValue()));
                solverWrapper.updateVariable(varRemapping, "range_upper", ctx.mkInt(operands.get(1).getValue()));
                solverWrapper.updateVariable(varRemapping, "range_step", ctx.mkInt(operands.get(2).getValue()));

                return rangeToArray(
                        solverWrapper,
                        new Pair<>(
                                operands.get(0),
                                operands.get(1)
                        ),
                        operands.get(2)
                );
            }



        case "len":
            return ctx.mkInt(
                    (int) Math.ceil(
                            (double) (Integer.parseInt(solverWrapper.getVariable(varRemapping, "range_upper").toString())
                                    - Integer.parseInt(solverWrapper.getVariable(varRemapping, "range_lower").toString()))
                                    / Integer.parseInt(solverWrapper.getVariable(varRemapping, "range_step").toString())
                    )
            );

        case "StrFormat":
        case "print":
            return strFormat(ctx, exprs);

        default:
            String msg = String.format("Invalid operator: %s\nOnly built in operators and some library functions "
            + "are supported.\nWe also do not support function calls to user made functions.", operator);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Computes the maximum value among the provided expressions.
     *
     * @param ctx the Z3 context
     * @param exprs the array of expressions among which to find the maximum
     * @return the expression representing the maximum value
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Expr max(Context ctx, Expr[] exprs) {
        Expr maxExpr = exprs[0];
        for (int i = 1; i < exprs.length; i++) {
            maxExpr = ctx.mkITE(ctx.mkGe(exprs[i], maxExpr), exprs[i], maxExpr);
        }
        return maxExpr;
    }

    /**
     * Computes the minimum value among the provided expressions.
     *
     * @param ctx the Z3 context
     * @param exprs the array of expressions among which to find the minimum
     * @return the expression representing the minimum value
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Expr min(Context ctx, Expr[] exprs) {
        Expr minExpr = exprs[0];
        for (int i = 1; i < exprs.length; i++) {
            minExpr = ctx.mkITE(ctx.mkLe(exprs[i], minExpr), exprs[i], minExpr);
        }
        return minExpr;
    }

    /**
     * Computes the sum of all provided expressions.
     *
     * @param ctx the Z3 context
     * @param exprs the array of expressions to be summed up
     * @return the expression representing the sum of the input expressions
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Expr sum(Context ctx, Expr[] exprs) {
        Expr sumExpr = exprs[0];
        for (int i = 1; i < exprs.length; i++) {
            sumExpr = ctx.mkAdd(sumExpr, exprs[i]);
        }
        return sumExpr;
    }

    /**
     * Computes the array declaration with the given type and initial values.
     *
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param exprs the array of expressions representing the type and initial values
     * @return the expression representing the array declaration
     */
    @SuppressWarnings({"rawtypes, unchecked", "rawtypes", "unchecked"})
    private Expr arrayDeclaration(SolverWrapper solverWrapper, Expr[] exprs) {
        Context ctx = solverWrapper.getCtx();

        String arrayType = exprs[1].toString();

        if (arrayType.equals("\"char\"")) {
            return exprs[2];
        }

        Expr array = solverWrapper.makeArrayConst("initialised_array",
            arrayType.substring(1, arrayType.length() - 1));

        for (int i = 2; i < exprs.length; i++) {
            Expr expr = exprs[i];
            array = ctx.mkStore(array, ctx.mkInt(i - 2), expr);
        }
        return array;
    }

    /**
     * Converts the range into an array based on the lower and upper bound with the step size
     * 
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param lower_upper the lower and upper bound (exclusive) of the range
     * @param step the increment of the range
     * @return the expression representing the range in an array
     */
    @SuppressWarnings({"rawtypes, unchecked", "rawtypes", "unchecked"})
    private Expr rangeToArray(SolverWrapper solverWrapper, Pair<BaseExpression,BaseExpression> lower_upper, BaseExpression step) {
        Context ctx = solverWrapper.getCtx();
        Expr array = ctx.mkArrayConst(ctx.mkSymbol("int_array"),
                ctx.getIntSort(),
                ctx.getIntSort());
        int lower = Integer.parseInt(lower_upper.getValue0().getValue());
        int upper = Integer.parseInt(lower_upper.getValue1().getValue());
        int step_size = Integer.parseInt(step.getValue());
        int count = 0;
        for (int i = lower; i < upper; i+=step_size) {
            array = ctx.mkStore(array, ctx.mkInt(count), ctx.mkInt(i)); //if the step size is 1
            count++;
        }
        return array;
    }

    /**
     * Formats the first parameter in the array as if it was an f-string, using the
     * other values as parameters
     * Currently only accepts String Constants as parameters
     *
     * @param ctx   the Z3 context
     * @param exprs a StringExpr array, with the first element being the String
     *              format Expr,
     *              and the rest are String Constants
     * @return the expression representing the new string
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Expr strFormat(Context ctx, Expr[] exprs) {
        for (int i = 1; i < exprs.length; i++) {
            if (exprs[i].getSort().equals(ctx.getStringSort())) {
                exprs[0] = ctx.mkReplace(exprs[0], ctx.mkString("%s"), exprs[i]);
            } else {
                throw new IllegalArgumentException("Non-string arguments are not yet supported.");
            }
        }
        return exprs[0];
    }
}
