package sg.edu.nus.se.its.validation.solverexpressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import sg.edu.nus.se.its.validation.SolverWrapper;

public class NaryExpressionTest {
    private final SolverWrapper solverWrapper = new SolverWrapper();
    private final Map<String, String> varRemapping = new HashMap<>();
    
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testEvaluateMaxOperator() {
        Context ctx = solverWrapper.getCtx();

        NaryExpression expression = new NaryExpression(
            Arrays.asList(new BaseExpression("3"), new BaseExpression("5"), new BaseExpression("4")),
            "max");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        // Construct the expected max operation using Z3 conditional expressions
        Expr expected = ctx.mkITE(ctx.mkGe(ctx.mkInt(5), ctx.mkInt(3)),
            ctx.mkITE(ctx.mkGe(ctx.mkInt(5), ctx.mkInt(4)), ctx.mkInt(5), ctx.mkInt(4)),
            ctx.mkITE(ctx.mkGe(ctx.mkInt(4), ctx.mkInt(3)), ctx.mkInt(4), ctx.mkInt(3)));

        Solver solver = ctx.mkSolver();
        solver.add(ctx.mkNot(ctx.mkEq(result, expected)));
        assertEquals(Status.UNSATISFIABLE, solver.check());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testEvaluateMinOperator() {
        Context ctx = solverWrapper.getCtx();

        NaryExpression expression = new NaryExpression(
            Arrays.asList(new BaseExpression("3"), new BaseExpression("5"), new BaseExpression("4")),
            "min");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        // Construct the expected min operation using Z3 conditional expressions
        Expr expected = ctx.mkITE(ctx.mkLe(ctx.mkInt(3), ctx.mkInt(5)),
            ctx.mkITE(ctx.mkLe(ctx.mkInt(3), ctx.mkInt(4)), ctx.mkInt(3), ctx.mkInt(4)),
            ctx.mkITE(ctx.mkLe(ctx.mkInt(4), ctx.mkInt(5)), ctx.mkInt(4), ctx.mkInt(5)));

        Solver solver = ctx.mkSolver();
        solver.add(ctx.mkNot(ctx.mkEq(result, expected)));
        assertEquals(Status.UNSATISFIABLE, solver.check());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testEvaluateSumOperator() {
        Context ctx = solverWrapper.getCtx();

        NaryExpression expression = new NaryExpression(
            Arrays.asList(new BaseExpression("3"), new BaseExpression("5"), new BaseExpression("4")),
            "sum");
        Expr result = expression.evaluate(solverWrapper, varRemapping);

        assertNotNull(result);
        // Directly use Z3's mkAdd for the sum operation
        Expr expected = ctx.mkAdd(ctx.mkInt(3), ctx.mkInt(5), ctx.mkInt(4));

        Solver solver = ctx.mkSolver();
        solver.add(ctx.mkNot(ctx.mkEq(result, expected)));
        assertEquals(Status.UNSATISFIABLE, solver.check());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEvaluateArrayDeclarationOperator() {
        Context ctx = solverWrapper.getCtx();

        NaryExpression expression0 = new NaryExpression(
            Arrays.asList(new BaseExpression("ArrayCreate"),
                new BaseExpression("int"), new BaseExpression("1"), new BaseExpression("2")),
            "ArrayDeclaration");
        Expr result0 = expression0.evaluate(solverWrapper, varRemapping);
        Expr expected0 = ctx.mkStore(ctx.mkStore(ctx.mkArrayConst("initialised_array", ctx.mkIntSort(),
                    ctx.mkIntSort()), ctx.mkInt(0), ctx.mkInt(1)), ctx.mkInt(1), ctx.mkInt(2));
        assertNotNull(result0);
        assertEquals(expected0, result0);

        NaryExpression expression1 = new NaryExpression(
            Arrays.asList(new BaseExpression("ArrayCreate"),
                new BaseExpression("bool"), new BaseExpression("true"), new BaseExpression("false")),
            "ArrayDeclaration");
        Expr result1 = expression1.evaluate(solverWrapper, varRemapping);
        Expr expected1 = ctx.mkStore(ctx.mkStore(ctx.mkArrayConst("initialised_array", ctx.mkIntSort(),
            ctx.mkBoolSort()), ctx.mkInt(0), ctx.mkTrue()), ctx.mkInt(1), ctx.mkFalse());
        assertNotNull(result1);
        assertEquals(expected1, result1);

        NaryExpression expression2 = new NaryExpression(
            Arrays.asList(new BaseExpression("ArrayCreate"),
                new BaseExpression("float"), new BaseExpression("1.1"), new BaseExpression("12.12")),
            "ArrayDeclaration");
        Expr result2 = expression2.evaluate(solverWrapper, varRemapping);
        Expr expected2 = ctx.mkStore(ctx.mkStore(ctx.mkArrayConst("initialised_array", ctx.mkIntSort(),
            ctx.mkRealSort()), ctx.mkInt(0), ctx.mkReal("1.1")), ctx.mkInt(1), ctx.mkReal("12.12"));
        assertNotNull(result2);
        assertEquals(expected2, result2);
    }

    @Test
    public void testInvalidOperator() {
        NaryExpression expression = new NaryExpression(
            Arrays.asList(new BaseExpression("3"), new BaseExpression("5"), new BaseExpression("4")),
            "InvalidOperator");
            
        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(solverWrapper, varRemapping));
    }

    @Test
    public void testInvalidOperand() {
        NaryExpression expression = new NaryExpression(List.of(), "max");
        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(solverWrapper, varRemapping));
    }
}
