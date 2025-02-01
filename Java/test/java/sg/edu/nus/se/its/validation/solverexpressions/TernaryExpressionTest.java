package sg.edu.nus.se.its.validation.solverexpressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import org.junit.jupiter.api.Test;

import sg.edu.nus.se.its.validation.SolverWrapper;

public class TernaryExpressionTest {
    private final SolverWrapper solverWrapper = new SolverWrapper();
    private final Map<String, String> varRemapping = new HashMap<>();

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateIteOperator() {
        Context ctx = solverWrapper.getCtx();

        BaseExpression conditionExpr = new BaseExpression("true");
        BaseExpression leftOperand = new BaseExpression("3");
        BaseExpression rightOperand = new BaseExpression("5");

        TernaryExpression expression = new TernaryExpression(conditionExpr, leftOperand, rightOperand, "ite");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);

        Expr expected = ctx.mkITE(ctx.mkTrue(), ctx.mkInt(3), ctx.mkInt(5));
        assertEquals(expected, result);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testTernaryIsNaryExpression() {
        BaseExpression condition = new BaseExpression("2");
        BaseExpression leftOperand = new BaseExpression("5");
        BaseExpression rightOperand = new BaseExpression("8");
        TernaryExpression expression = new TernaryExpression(condition, leftOperand, rightOperand, "min");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertEquals("(ite (<= 8 (ite (<= 5 2) 5 2)) 8 (ite (<= 5 2) 5 2))",
            result.toString());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testEvaluateArrayAssignOperator() {
        Context ctx = solverWrapper.getCtx();
        String varName = "arr";
        Sort int_type = ctx.getIntSort();
        solverWrapper.updateVariable(varRemapping,
            varName,
            ctx.mkArrayConst(varName, int_type, int_type));

        BaseExpression conditionExpr = new BaseExpression(varName);
        BaseExpression leftOperand = new BaseExpression("0");
        BaseExpression rightOperand = new BaseExpression("5");

        TernaryExpression expression = new TernaryExpression(conditionExpr, leftOperand, rightOperand,
            "ArrayAssign");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);

        Expr expected = ctx.mkStore(solverWrapper.getVariable(varRemapping, varName), ctx.mkInt(0),
            ctx.mkInt(5));
        assertEquals(expected, result);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testEvaluateArrayAssignOperatorFromUninitialised() {
        Context ctx = solverWrapper.getCtx();
        String varName = "placeholder_array";
        solverWrapper.updateVariable(varRemapping,
            varName, ctx.mkArrayConst(varName, ctx.getIntSort(),
                ctx.mkUninterpretedSort(ctx.mkSymbol("PlaceholderType"))));

        BaseExpression conditionExpr = new BaseExpression(varName);
        BaseExpression leftOperand = new BaseExpression("0");

        // int array
        BaseExpression rightOperand0 = new BaseExpression("5");
        TernaryExpression expression0 = new TernaryExpression(conditionExpr, leftOperand, rightOperand0,
            "ArrayAssign");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);
        Expr expectedArray0 = ctx.mkArrayConst("initialised_array",  ctx.getIntSort(),  ctx.getIntSort());
        Expr expected0 = ctx.mkStore(expectedArray0, ctx.mkInt(0), ctx.mkInt(5));
        assertNotNull(result0);
        assertEquals(expected0, result0);

        // bool array
        BaseExpression rightOperand1 = new BaseExpression("true");
        TernaryExpression expression1 = new TernaryExpression(conditionExpr, leftOperand, rightOperand1,
            "ArrayAssign");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);
        Expr expectedArray1 = ctx.mkArrayConst("initialised_array",  ctx.getIntSort(),  ctx.getBoolSort());
        Expr expected1 = ctx.mkStore(expectedArray1, ctx.mkInt(0), ctx.mkTrue());
        assertNotNull(result1);
        assertEquals(expected1, result1);

        // float/double array
        BaseExpression rightOperand2 = new BaseExpression("1.3");
        TernaryExpression expression2 = new TernaryExpression(conditionExpr, leftOperand, rightOperand2,
            "ArrayAssign");
        Expr result2 = expression2.evaluate(solverWrapper, varRemapping);
        Expr expectedArray2 = ctx.mkArrayConst("initialised_array",  ctx.getIntSort(),  ctx.getRealSort());
        Expr expected2 = ctx.mkStore(expectedArray2, ctx.mkInt(0), ctx.mkReal("1.3"));
        assertNotNull(result2);
        assertEquals(expected2, result2);

        // invalid array
        BaseExpression rightOperand3 = new BaseExpression("int");
        TernaryExpression expression3 = new TernaryExpression(conditionExpr, leftOperand, rightOperand3,
            "ArrayAssign");
        assertThrows(IllegalArgumentException.class, () -> expression3.evaluate(solverWrapper, varRemapping));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateIte_NullPlaceholder() {
        Context ctx = solverWrapper.getCtx();
        String varName = "uninitialized_var";
        solverWrapper.updateVariable(varRemapping,
            varName, ctx.mkIntConst(varName)); // Simulate uninitialized variable

        BaseExpression conditionExpr = new BaseExpression("true");
        BaseExpression rightOperand = new BaseExpression("var");

        BaseExpression leftOperand0 = new BaseExpression("true");
        BaseExpression leftOperand1 = new BaseExpression("0");
        BaseExpression leftOperand2 = new BaseExpression("1.25");

        TernaryExpression expression0 = new TernaryExpression(conditionExpr, leftOperand0, rightOperand,
            "ite");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);
        assertNotNull(result0);
        assertTrue(result0.toString().contains("NULL"));

        TernaryExpression expression1 = new TernaryExpression(conditionExpr, leftOperand1, rightOperand,
            "ite");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);
        assertNotNull(result1);
        assertTrue(result1.toString().contains("NULL"));

        TernaryExpression expression2 = new TernaryExpression(conditionExpr, leftOperand2, rightOperand,
            "ite");
        Expr result2 = expression2.evaluate(solverWrapper, varRemapping);
        assertNotNull(result2);
        assertTrue(result2.toString().contains("NULL"));
    }


    @Test
    public void testInvalidOperator() {
        BaseExpression condition = new BaseExpression("true");
        BaseExpression leftOperand = new BaseExpression("5");
        BaseExpression rightOperand = new BaseExpression("2");
        TernaryExpression expression = new TernaryExpression(condition,
            leftOperand, rightOperand, "InvalidOperator");

        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(solverWrapper, varRemapping));
    }
}
