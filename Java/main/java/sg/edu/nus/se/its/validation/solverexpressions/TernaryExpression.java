package sg.edu.nus.se.its.validation.solverexpressions;

import java.util.Arrays;
import java.util.Map;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.RealExpr;

import org.javatuples.Pair;
import sg.edu.nus.se.its.validation.SolverWrapper;

/**
 * Represents a ternary expression in the solver.
 */
public class TernaryExpression extends BaseExpression {
    private final BaseExpression conditionExpr;
    private final BaseExpression leftOperand;
    private final BaseExpression rightOperand;
    private final String operator;

    /**
     * Constructs a new TernaryExpression with the given operands and operator.
     * 
     * @param conditionExpr the condition operand of the ternary expression
     * @param leftOperand  the left operand of the ternary expression
     * @param rightOperand the right operand of the ternary expression
     * @param operator     the operator of the ternary expression
     */
    public TernaryExpression(BaseExpression conditionExpr, BaseExpression leftOperand, BaseExpression rightOperand, String operator) {
        super(null);
        this.conditionExpr = conditionExpr;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }

    /**
     * Returns a string representation of the ternary expression.
     *
     * @return a string representation of the ternary expression
     */
    @Override
    public String toString() {
        return "(" + conditionExpr + " " + operator + " " + leftOperand + " " + rightOperand + ")";
    }

    /**
     * Evaluates the ternary expression in the given context and returns the result.
     *
     * @param solverWrapper the solver wrapper used to evaluate the expression
     * @param varRemapping the variable remapping used in the expression
     * @return the result of evaluating the ternary expression
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Expr evaluate(SolverWrapper solverWrapper, Map<String, String> varRemapping) {
        Context ctx = solverWrapper.getCtx();
        Expr firstExpr = conditionExpr.evaluate(solverWrapper, varRemapping);
        Expr secondExpr = leftOperand.evaluate(solverWrapper, varRemapping);
        Expr thirdExpr = rightOperand.evaluate(solverWrapper, varRemapping);
        
        // If the third expression is null, create a placeholder expression
        if (thirdExpr == null) {
            if (secondExpr instanceof BoolExpr) {
                thirdExpr = ctx.mkBoolConst("NULL");
            } else if (secondExpr instanceof IntExpr) {
                thirdExpr = ctx.mkIntConst("NULL");
            } else {
                thirdExpr = ctx.mkRealConst("NULL");
            }
        }

        switch (operator) {
        case "ite":
            return ctx.mkITE(convertToBoolExpr(ctx, firstExpr), secondExpr, thirdExpr);
        case "ArrayAssign":
            if (firstExpr.toString().equals("placeholder_array")) {
                String arrayType = "";
                if (thirdExpr instanceof BoolExpr) {
                    arrayType = "bool";
                } else if (thirdExpr instanceof IntExpr) {
                    arrayType = "int";
                } else if (thirdExpr instanceof RealExpr) {
                    arrayType = "float";
                }
                firstExpr = solverWrapper.makeArrayConst("initialised_array", arrayType);
            }
            return ctx.mkStore(firstExpr, secondExpr, thirdExpr);
        case "range":
            return new NaryExpression(new Pair<>(conditionExpr, leftOperand), rightOperand, operator)
                .evaluate(solverWrapper, varRemapping);
        default:
            return new NaryExpression(Arrays.asList(conditionExpr, leftOperand, rightOperand), operator)
                .evaluate(solverWrapper, varRemapping);
        }
    }
}