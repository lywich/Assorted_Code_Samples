package sg.edu.nus.se.its.validation.solverexpressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import sg.edu.nus.se.its.validation.SolverWrapper;

public class BaseExpressionTest {
    private final SolverWrapper solverWrapper = new SolverWrapper();
    private final Map<String, String> varRemapping = new HashMap<>();

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateIntValue() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("123");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(
            ctx.mkInt(123),
            result
        );
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateNegativeIntValue() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("-123");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(
            ctx.mkInt(-123),
            result
        );
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateFloatValue() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("123.123");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(
            ctx.mkReal("123.123"),
            result
        );
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateNegativeFloatValue() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("-123.123");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(
            ctx.mkReal("-123.123"),
            result
        );
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateTrueValue() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("true");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(ctx.mkTrue(), result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateFalseValue() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("false");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(ctx.mkFalse(), result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateReservedKeyword() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression expression = new BaseExpression("int");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(
            ctx.mkString("int"),
            result
        );
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateVariable1Value() {
        Context ctx = solverWrapper.getCtx();
        String variableName = "x";
        BaseExpression expression = new BaseExpression(variableName);

        solverWrapper.updateVariable(varRemapping, variableName, ctx.mkReal(1));
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(solverWrapper.getVariable(varRemapping, variableName), result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateVariable2Value() {
        Context ctx = solverWrapper.getCtx();
        String variableName = "x";

        BaseExpression expression = new BaseExpression(variableName + "'");

        solverWrapper.updateVariable(varRemapping, variableName, ctx.mkReal(1));
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(solverWrapper.getVariable(varRemapping, variableName), result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateLogicOperationWithNumerals() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression operand0 = new BaseExpression("1");
        BaseExpression operand1 = new BaseExpression("1.5");
        BinaryExpression expression = new BinaryExpression(operand0, operand1, "&&");

        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        assertEquals(ctx.mkAnd(ctx.mkNot(ctx.mkEq(ctx.mkInt("1"),
            ctx.mkInt("0"))), ctx.mkNot(ctx.mkEq(ctx.mkReal("1.5"), ctx.mkReal("0")))), result);
    }
}
