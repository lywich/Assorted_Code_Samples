package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.se.its.model.Constant;
import sg.edu.nus.se.its.model.Operation;
import sg.edu.nus.se.its.model.Variable;
import sg.edu.nus.se.its.validation.solverexpressions.BaseExpression;

public class ExpressionFactoryTest {
    @Test
    public void testParseBaseExpression() {
        String integerLiteral = "123";
        BaseExpression expression0 = ExpressionFactory.parseExpression(new Constant(integerLiteral, 1));
        assertEquals("<123>", expression0.toString());

        String realLiteral = "123.123";
        BaseExpression expression1 = ExpressionFactory.parseExpression(new Constant(realLiteral, 1));
        assertEquals("<123.123>", expression1.toString());

        String booleanLiteral = "true";
        BaseExpression expression2 = ExpressionFactory.parseExpression(new Constant(booleanLiteral, 1));
        assertEquals("<true>", expression2.toString());
    }

    @Test
    public void testParseSimpleUnaryExpression() {
        // USub(x)
        BaseExpression simpleResult0 = ExpressionFactory.parseExpression(new Operation("USub",
            List.of(new Variable("x", 1)), 1));
        assertEquals("(USub <x>)", simpleResult0.toString());

        // -(x)
        BaseExpression simpleResult1 = ExpressionFactory.parseExpression(new Operation("-",
            List.of(new Variable("x", 1)), 1));
        assertEquals("(- <x>)", simpleResult1.toString());
    }

    @Test
    public void testParseSimpleBinaryExpression() {
        // Add(x, y)
        BaseExpression simpleResult0 = ExpressionFactory.parseExpression(new Operation("Add", Arrays.asList(new Variable("x", 1), new Variable("y", 1)), 1));
        assertEquals("(<x> Add <y>)", simpleResult0.toString());

        // +(x, y)
        BaseExpression simpleResult1 = ExpressionFactory.parseExpression(new Operation("+", Arrays.asList(new Variable("x", 1), new Variable("y", 1)), 1));
        assertEquals("(<x> + <y>)", simpleResult1.toString());
    }

    @Test
    public void testParseNestedExpression() {
        // Add(Mult(Add(x, y), z), Add(x, 2))
        BaseExpression expression0 = ExpressionFactory.parseExpression(new Operation("Add", Arrays.asList(
            new Operation("Mult", Arrays.asList(new Operation("Add", Arrays.asList(new Variable("x", 1), new Variable("y", 1)), 1), new Variable("z", 1)), 1),
            new Operation("Add", Arrays.asList(new Variable("x", 1), new Constant("2", 1)), 1)
        ), 1));
        assertEquals("(((<x> Add <y>) Mult <z>) Add (<x> Add <2>))", expression0.toString());

        // +(*(+(x, y), z), +(z, 2))
        BaseExpression expression1 = ExpressionFactory.parseExpression(new Operation("+", Arrays.asList(
            new Operation("*", Arrays.asList(new Operation("+", Arrays.asList(new Variable("x", 1), new Variable("y", 1)), 1), new Variable("z", 1)), 1),
            new Operation("+", Arrays.asList(new Variable("z", 1), new Constant("2", 1)), 1)
        ), 1));
        assertEquals("(((<x> + <y>) * <z>) + (<z> + <2>))", expression1.toString());
    }

    @Test
    public void testParseSimpleTernaryExpression() {
        // ite(x, y, z)
        BaseExpression expression = ExpressionFactory.parseExpression(new Operation("ite", Arrays.asList(new Variable("x", 1), new Variable("y", 1), new Variable("z", 1)), 1));
        assertEquals("(<x> ite <y> <z>)", expression.toString());
    }

    @Test
    public void testParseNestedTernaryExpression() {
        // ite(x, ite(y, m, ite(n, a, b)), z)
        BaseExpression expression = ExpressionFactory.parseExpression(new Operation("ite", Arrays.asList(
            new Variable("x", 1),
            new Operation("ite", Arrays.asList(new Variable("y", 1), new Variable("m", 1), new Operation("ite", Arrays.asList(new Variable("n", 1), new Variable("a", 1), new Variable("b", 1)), 1)), 1),
            new Variable("z", 1)
        ), 1));
        assertEquals("(<x> ite (<y> ite <m> (<n> ite <a> <b>)) <z>)", expression.toString());
    }

    @Test
    public void testParseSimpleNaryExpression() {
        // min(x, y, z)
        BaseExpression expression = ExpressionFactory.parseExpression(new Operation("min", Arrays.asList(new Variable("x", 1), new Variable("y", 1), new Variable("z", 1)), 1));
        assertEquals("(<x> min <y> <z>)", expression.toString());
    }

    @Test
    public void testParseNestedNaryExpression() {
        // sum(max(min(a, b, c, d), min(e, f, g, h), a, b), g, h)
        BaseExpression expression = ExpressionFactory.parseExpression(new Operation("sum", Arrays.asList(
            new Operation("max", Arrays.asList(
                new Operation("min", Arrays.asList(new Variable("a", 1), new Variable("b", 1), new Variable("c", 1), new Variable("d", 1)), 1),
                new Operation("min", Arrays.asList(new Variable("e", 1), new Variable("f", 1), new Variable("g", 1), new Variable("h", 1)), 1)
            ), 1),
            new Variable("g", 1),
            new Variable("h", 1)
        ), 1));
        assertEquals("(((min <a> <b> <c> <d>) max (min <e> <f> <g> <h>)) sum <g> <h>)", expression.toString());
    }

    @Test
    public void testParseInvalidExpression() {
        assertThrows(IllegalArgumentException.class, () -> ExpressionFactory.parseExpression(null));
    }

    @Test
    public void testIsReservedKeyword() {
        assertTrue(ExpressionFactory.isReservedKeyword("int"));
        assertTrue(ExpressionFactory.isReservedKeyword("bool"));
        assertTrue(ExpressionFactory.isReservedKeyword("float"));
        assertTrue(ExpressionFactory.isReservedKeyword("double"));
    }

    @Test
    public void testExpressionFactoryNotNull() {
        assertNotNull(new ExpressionFactory());
    }
}
