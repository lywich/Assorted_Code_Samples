package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.HashMap;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.junit.jupiter.api.Test;

public class SolverWrapperTest {
    private final SolverWrapper solverWrapper = new SolverWrapper();
    private final Map<String, String> varRemapping = new HashMap<>();
    private final String variableName = "x";

    @SuppressWarnings("rawtypes")
    public SolverWrapperTest() {
        Context ctx = solverWrapper.getCtx();
        Expr value = ctx.mkBool(true);
        solverWrapper.updateVariable(varRemapping, variableName, value);
    }

    @Test
    public void testGetVariableName() {
        Map<String, String> varRemapping = new HashMap<>();
        varRemapping.put("oldVarName", "newVarName");

        String variableName = "oldVarName";
        String expectedNewVarName = "newVarName";
        String actualNewVarName = solverWrapper.getVariableName(varRemapping, variableName);
        assertEquals(expectedNewVarName, actualNewVarName);

        variableName = "anotherVarName";
        expectedNewVarName = "$$anotherVarName";
        actualNewVarName = solverWrapper.getVariableName(varRemapping, variableName);
        assertEquals(expectedNewVarName, actualNewVarName);

        varRemapping.clear();
        variableName = "someVarName";
        expectedNewVarName = "someVarName";
        actualNewVarName = solverWrapper.getVariableName(varRemapping, variableName);
        assertEquals(expectedNewVarName, actualNewVarName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testUpdateVariable() {
        Context ctx = solverWrapper.getCtx();

        Expr value = ctx.mkBool(false);

        solverWrapper.updateVariable(varRemapping, variableName, value);

        assertEquals(value, solverWrapper.getVariable(varRemapping, variableName));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetVariable() {
        Expr result = solverWrapper.getVariable(varRemapping, variableName);

        assertNotNull(result);
        assertEquals("true", result.toString());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testMakeArrayConst() {
        Context ctx = solverWrapper.getCtx();
        String varName = "x";

        Expr result0 = solverWrapper.makeArrayConst(varName, "bool");
        assertNotNull(result0);
        assertEquals(ctx.mkArrayConst(varName, ctx.getIntSort(), ctx.getBoolSort()), result0);

        Expr result1 = solverWrapper.makeArrayConst(varName, "int");
        assertNotNull(result1);
        assertEquals(ctx.mkArrayConst(varName, ctx.getIntSort(), ctx.getIntSort()), result1);

        Expr result2 = solverWrapper.makeArrayConst(varName, "float");
        assertNotNull(result2);
        assertEquals(ctx.mkArrayConst(varName, ctx.getIntSort(), ctx.getRealSort()), result2);

        Expr result3 = solverWrapper.makeArrayConst(varName, "double");
        assertNotNull(result3);
        assertEquals(ctx.mkArrayConst(varName, ctx.getIntSort(), ctx.getRealSort()), result3);

        assertThrows(IllegalArgumentException.class, () -> solverWrapper.makeArrayConst(varName, "invalid type"));
    }

    @Test
    public void testNullFunctionsReturnFalse() {
        assertFalse(solverWrapper.areFunctionsEquivalent(null, null));
    }

    @Test
    public void testSolverWrapperNotNull() {
        assertNotNull(solverWrapper);
    }
}
