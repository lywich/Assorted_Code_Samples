package sg.edu.nus.se.its.validation.solverexpressions;

import java.util.List;
import java.util.Map;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import sg.edu.nus.se.its.validation.SolverWrapper;

/**
 * Represents a unary expression in the solver expressions.
 */
public class UnaryExpression extends BaseExpression {
    private final BaseExpression operand;
    private final String operator;
    
    /**
     * Constructs a new UnaryExpression with the given operand and operator.
     * 
     * @param operand the operand of the unary expression
     * @param operator the operator of the unary expression
     */
    public UnaryExpression(BaseExpression operand, String operator) {
        super(null);
        this.operand = operand;
        this.operator = operator;
    }

    /**
     * Returns a string representation of the unary expression.
     * 
     * @return a string representation of the unary expression
     */
    @Override
    public String toString() {
        return "(" + operator + " " + operand + ")";
    }

    /**
     * Evaluates the unary expression in the given context with the provided variables and variable remapping.
     *
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param varRemapping the variable remapping used in the expression
     * @return the evaluated expression
     * @throws IllegalArgumentException if the operator is invalid
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Expr evaluate(SolverWrapper solverWrapper, Map<String, String> varRemapping) {
        Context ctx = solverWrapper.getCtx();
        Expr operandExpr = operand.evaluate(solverWrapper, varRemapping);
        switch (operator) {
        case "USub":
        case "-":
            return ctx.mkUnaryMinus(operandExpr);
        case "abs":
            return ctx.mkITE(ctx.mkGe(operandExpr, ctx.mkInt(0)), operandExpr, ctx.mkSub(ctx.mkInt(0), operandExpr));
        case "Not":
        case "!":
            return ctx.mkNot(convertToBoolExpr(ctx, operandExpr));
        case "ArrayCreate":
            return ctx.mkArrayConst(ctx.mkSymbol("placeholder_array"),
                ctx.getIntSort(),
                ctx.mkUninterpretedSort(ctx.mkSymbol("PlaceholderType")));
        default:
            return new NaryExpression(List.of(operand), operator)
                .evaluate(solverWrapper, varRemapping);
        }
    }
}
