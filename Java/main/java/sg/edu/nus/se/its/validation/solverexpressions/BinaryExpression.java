package sg.edu.nus.se.its.validation.solverexpressions;

import java.util.Arrays;
import java.util.Map;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import sg.edu.nus.se.its.validation.SolverWrapper;

/**
 * Represents a binary expression in the solver expressions.
 */
public class BinaryExpression extends BaseExpression {
    private final BaseExpression leftOperand;
    private final BaseExpression rightOperand;
    private final String operator;

    /**
     * Constructs a new BinaryExpression with the given operands and operator.
     *
     * @param leftOperand  the left operand of the binary expression
     * @param rightOperand the right operand of the binary expression
     * @param operator     the operator of the binary expression
     */
    public BinaryExpression(BaseExpression leftOperand, BaseExpression rightOperand, String operator) {
        super(null);
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    /**
     * Returns a string representation of the binary expression.
     *
     * @return a string representation of the binary expression
     */
    @Override
    public String toString() {
        return "(" + leftOperand + " " + operator + " " + rightOperand + ")";
    }

    /**
     * Evaluates the binary expression in the given context and returns the result.
     *
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param varRemapping the variable remapping used in the expression
     * @return the result of evaluating the binary expression
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Expr evaluate(SolverWrapper solverWrapper, Map<String, String> varRemapping) {
        Context ctx = solverWrapper.getCtx();
        Expr leftExpr = leftOperand.evaluate(solverWrapper, varRemapping);
        Expr rightExpr = rightOperand.evaluate(solverWrapper, varRemapping);
        switch (operator) {
        case "Add":
        case "AssAdd":
        case "+":
            return ctx.mkAdd(leftExpr, rightExpr);
        case "Sub":
        case "-":
            return ctx.mkSub(leftExpr, rightExpr);
        case "Mult":
        case "*":
            return ctx.mkMul(leftExpr, rightExpr);
        case "Div":
        case "/":
        case "FloorDiv":
            return ctx.mkDiv(leftExpr, rightExpr);
        case "Pow":
        case "pow":
            return ctx.mkPower(leftExpr, rightExpr);
        case "Mod":
        case "%":
            return ctx.mkMod(leftExpr, rightExpr);
        case "Gt":
        case ">":
            return ctx.mkGt(leftExpr, rightExpr);
        case "GtE":
        case ">=":
            return ctx.mkGe(leftExpr, rightExpr);
        case "Lt":
        case "<":
            return ctx.mkLt(leftExpr, rightExpr);
        case "LtE":
        case "<=":
            return ctx.mkLe(leftExpr, rightExpr);
        case "Eq":
        case "==":
            return ctx.mkEq(leftExpr, rightExpr);
        case "!=":
            return ctx.mkNot(ctx.mkEq(leftExpr, rightExpr));
        case "And":
        case "&&":
            return ctx.mkAnd(convertToBoolExpr(ctx, leftExpr),
                convertToBoolExpr(ctx, rightExpr));
        case "Or":
        case "||":
            return ctx.mkOr(convertToBoolExpr(ctx, leftExpr),
                convertToBoolExpr(ctx, rightExpr));
        case "[]":
        case "GetElement":
            return ctx.mkSelect(leftExpr, rightExpr);
        case "StrAppend":
            // Extra parameter is required to select correct function amongst overloaded ones
            return ctx.mkConcat(leftExpr, rightExpr, ctx.mkString(""));
        default:
            return new NaryExpression(Arrays.asList(leftOperand, rightOperand), operator)
                .evaluate(solverWrapper, varRemapping);
        }
    }
}
