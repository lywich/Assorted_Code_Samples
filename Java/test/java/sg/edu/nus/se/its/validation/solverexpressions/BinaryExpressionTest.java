package sg.edu.nus.se.its.validation.solverexpressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.z3.Sort;
import org.junit.jupiter.api.Test;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import sg.edu.nus.se.its.validation.SolverWrapper;

public class BinaryExpressionTest {
    private final SolverWrapper solverWrapper = new SolverWrapper();
    private final Map<String, String> varRemapping = new HashMap<>();

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateAddOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression left_operand = new BaseExpression("3");
        BaseExpression right_operand = new BaseExpression("2");

        BinaryExpression expression0 = new BinaryExpression(left_operand, right_operand, "Add");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(left_operand, right_operand, "AssAdd");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression2 = new BinaryExpression(left_operand, right_operand, "+");
        Expr result2 = expression2.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);
        assertNotNull(result2);

        Expr expected = ctx.mkAdd(ctx.mkInt(3),  ctx.mkInt(2));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
        assertEquals(expected, result2);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateSubOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("5");
        BaseExpression rightOperand = new BaseExpression("2");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Sub");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "-");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkSub(ctx.mkInt(5), ctx.mkInt(2));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateMultOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("3");
        BaseExpression rightOperand = new BaseExpression("2");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Mult");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "*");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkMul(ctx.mkInt(3), ctx.mkInt(2));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateDivOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("6");
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Div");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "/");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkDiv(ctx.mkInt(6), ctx.mkInt(3));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluatePowOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression left_operand = new BaseExpression("2");
        BaseExpression right_operand = new BaseExpression("3");
        Expr expected = ctx.mkPower(ctx.mkInt(2), ctx.mkInt(3));
        
        BinaryExpression expression0 = new BinaryExpression(left_operand, right_operand, "Pow");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertEquals(expected, result0);

        BinaryExpression expression1 = new BinaryExpression(left_operand, right_operand, "pow");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result1);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateModOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("10");
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Mod");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "%");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkMod(ctx.mkInt(10), ctx.mkInt(3));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateGtOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("5");
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Gt");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, ">");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkGt(ctx.mkInt(5), ctx.mkInt(3));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateGteOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("5");
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "GtE");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, ">=");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkGe(ctx.mkInt(5), ctx.mkInt(3));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateLtOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("3");
        BaseExpression rightOperand = new BaseExpression("5");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Lt");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "<");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkLt(ctx.mkInt(3), ctx.mkInt(5));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateLteOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("3");
        BaseExpression rightOperand = new BaseExpression("5");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "LtE");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "<=");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkLe(ctx.mkInt(3), ctx.mkInt(5));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateEqOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("3");
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Eq");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "==");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkEq(ctx.mkInt(3), ctx.mkInt(3));
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateNotEqOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("3");
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression = new BinaryExpression(leftOperand, rightOperand, "!=");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        Expr expected = ctx.mkNot(ctx.mkEq(ctx.mkInt(3), ctx.mkInt(3)));
        assertEquals(expected, result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateAndOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("true");
        BaseExpression rightOperand = new BaseExpression("false");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "And");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "&&");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkAnd(ctx.mkTrue(), ctx.mkFalse());
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateOrOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression leftOperand = new BaseExpression("true");
        BaseExpression rightOperand = new BaseExpression("false");

        BinaryExpression expression0 = new BinaryExpression(leftOperand, rightOperand, "Or");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);

        BinaryExpression expression1 = new BinaryExpression(leftOperand, rightOperand, "||");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);

        assertNotNull(result0);
        assertNotNull(result1);

        Expr expected = ctx.mkOr(ctx.mkTrue(), ctx.mkFalse());
        assertEquals(expected, result0);
        assertEquals(expected, result1);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testBinaryIsNaryExpression() {
        BaseExpression left_operand = new BaseExpression("2");
        BaseExpression right_operand = new BaseExpression("5");
        BinaryExpression expression = new BinaryExpression(left_operand, right_operand, "min");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertEquals("(ite (<= 5 2) 5 2)", result.toString());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testEvaluateArrayIndexOperator() {
        Context ctx = solverWrapper.getCtx();
        String varName = "arr";
        Sort int_type = ctx.getIntSort();
        solverWrapper.updateVariable(varRemapping,
            varName,
            ctx.mkArrayConst(varName, int_type, int_type));

        BaseExpression leftOperand = new BaseExpression(varName);
        BaseExpression rightOperand = new BaseExpression("3");

        BinaryExpression expression = new BinaryExpression(leftOperand, rightOperand, "[]");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);

        Expr expected = ctx.mkSelect(solverWrapper.getVariable(varRemapping, varName), ctx.mkInt(3));
        assertEquals(expected, result);
    }
    
    @Test
    public void testInvalidOperator() {
        BaseExpression left_operand = new BaseExpression("5");
        BaseExpression right_operand = new BaseExpression("2");
        BinaryExpression expression = new BinaryExpression(left_operand, right_operand, "InvalidOperator");

        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(solverWrapper, varRemapping));
    }
}
