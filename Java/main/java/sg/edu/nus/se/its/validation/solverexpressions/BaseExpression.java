package sg.edu.nus.se.its.validation.solverexpressions;

import java.util.Map;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;

import sg.edu.nus.se.its.validation.ExpressionFactory;
import sg.edu.nus.se.its.validation.SolverWrapper;

/**
 * Represents a base expression in the solver.
 */
public class BaseExpression {
    private final String value;

    /**
     * Constructs a new BaseExpression object with the given value.
     *
     * @param value the value of the expression
     */
    public BaseExpression(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the expression.
     *
     * @return the value of the expression
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a RealExpr to a BoolExpr representing whether the real expression is equal to zero.
     *
     * @param ctx  the Z3 context in which the conversion is performed
     * @param expr the expression to be converted
     * @return a boolean expression indicating whether the real expression is equal to zero
     */
    @SuppressWarnings({ "rawtypes" })
    protected Expr convertToBoolExpr(Context ctx, Expr expr) {
        if (expr instanceof RealExpr) {
            return ctx.mkNot(ctx.mkEq(expr, ctx.mkReal(0)));
        } else if (expr instanceof IntExpr) {
            return ctx.mkNot(ctx.mkEq(expr, ctx.mkInt(0)));
        }
        return expr;
    }

    /**
     * Returns a string representation of the expression.
     *
     * @return a string representation of the expression
     */
    @Override
    public String toString() {
        return "<" + value + ">";
    }

    /**
     * Evaluates the expression in the given context with the provided variables and variable remapping.
     *
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param varRemapping the variable remapping used in the expression
     * @return the evaluated expression
     */
    @SuppressWarnings("rawtypes")
    public Expr evaluate(SolverWrapper solverWrapper, Map<String, String> varRemapping) {
        Context ctx = solverWrapper.getCtx();
        String value = this.getValue();
        // If the value is a number, return it as an Expr containing the real number
        if (Character.isDigit(value.charAt(0)) || value.charAt(0) == '-') {
            if (value.contains(".")) {
                return ctx.mkReal(value);
            } else {
                return ctx.mkInt(Integer.parseInt(value));
            }
        } else if (value.equalsIgnoreCase("true")) {
            return ctx.mkTrue();
        } else if (value.equalsIgnoreCase("false")) {
            return ctx.mkFalse();
        } else if (ExpressionFactory.isReservedKeyword(value)) {
            return ctx.mkString(value);
        } else if (value.length() > 1 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return ctx.mkString(value.substring(1, value.length() - 1));
        } else {
            // The parser service adds an extra ' to the end of the variable name, 
            // so we need to remove it
            if (value.contains("'")) {
                value = value.substring(0, value.length() - 1);
            }
            return solverWrapper.getVariable(varRemapping, value);
        }
    }
}
