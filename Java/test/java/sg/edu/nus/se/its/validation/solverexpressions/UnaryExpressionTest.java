package sg.edu.nus.se.its.validation.solverexpressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import sg.edu.nus.se.its.validation.SolverWrapper;

public class UnaryExpressionTest {
    private final SolverWrapper solverWrapper = new SolverWrapper();
    private final Map<String, String> varRemapping = new HashMap<>();

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateUsubOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression operand = new BaseExpression("5");
        UnaryExpression expression = new UnaryExpression(operand, "USub");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(ctx.mkUnaryMinus(ctx.mkInt(5)), result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateAbsOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression operand = new BaseExpression("-5");
        UnaryExpression expression = new UnaryExpression(operand, "abs");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(ctx.mkITE(ctx.mkGe(ctx.mkInt(-5), ctx.mkInt(0)),
                ctx.mkInt(-5),
                ctx.mkSub(ctx.mkInt(0), ctx.mkInt(-5))),
            result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateNotOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression operand = new BaseExpression("true");
        UnaryExpression expression = new UnaryExpression(operand, "Not");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(ctx.mkNot(ctx.mkTrue()), result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateArrayCreateOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression operand = new BaseExpression("?");
        UnaryExpression expression = new UnaryExpression(operand, "ArrayCreate");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        Expr expected = ctx.mkArrayConst(ctx.mkSymbol("placeholder_array"),
            ctx.getIntSort(),
            ctx.mkUninterpretedSort(ctx.mkSymbol("PlaceholderType")));
        assertEquals(expected, result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testUnaryIsNaryExpression() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression operand = new BaseExpression("2");
        UnaryExpression expression = new UnaryExpression(operand, "min");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        Expr expected = ctx.mkInt(2);
        assertEquals(expected, result);
    }

    @Test
    public void testInvalidOperator() {
        BaseExpression operand = new BaseExpression("5");
        UnaryExpression expression = new UnaryExpression(operand, "InvalidOperator");

        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(solverWrapper, varRemapping));
    }
}
