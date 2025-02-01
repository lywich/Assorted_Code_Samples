package sg.edu.nus.se.its.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.se.its.model.Constant;
import sg.edu.nus.se.its.model.Expression;
import sg.edu.nus.se.its.model.Operation;
import sg.edu.nus.se.its.model.Variable;
import sg.edu.nus.se.its.validation.solverexpressions.BaseExpression;
import sg.edu.nus.se.its.validation.solverexpressions.UnaryExpression;
import sg.edu.nus.se.its.validation.solverexpressions.BinaryExpression;
import sg.edu.nus.se.its.validation.solverexpressions.TernaryExpression;
import sg.edu.nus.se.its.validation.solverexpressions.NaryExpression;

/**
 * The ExpressionFactory class provides a static method to parse an expression
 * string and create a corresponding expression object.
 */
public class ExpressionFactory {
    /**
     * A set of reserved keywords that cannot be used as variable names.
     */
    private static final HashSet<String> reservedKeywords = new HashSet<>(
            Arrays.asList("not", "and", "or", "int", "bool", "float", "double", "char"));

    /**
     * Parses the given expression string and creates a corresponding expression
     * object.
     *
     * @param expression the expression string to parse
     * @return the expression object
     */

    public static BaseExpression parseExpression(Expression expression) {
        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            List<BaseExpression> args = operation.getArgs().stream()
                    .map(ExpressionFactory::parseExpression)
                    .collect(Collectors.toList());

            switch (args.size()) {
                case 1:
                    return new UnaryExpression(args.get(0), operation.getName());
                case 2:
                    return new BinaryExpression(args.get(0), args.get(1), operation.getName());
                case 3:
                    return new TernaryExpression(args.get(0), args.get(1), args.get(2), operation.getName());
                default:
                    return new NaryExpression(args, operation.getName());
            }
        } else if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            return new BaseExpression(variable.getName());
        } else if (expression instanceof Constant) {
            Constant constant = (Constant) expression;
            return new BaseExpression(constant.getValue());
        }

        throw new IllegalArgumentException("Invalid expression type");
    }

    /**
     * Checks if the given name is a reserved keyword.
     *
     * @param name the name to check
     * @return true if the name is a reserved keyword, false otherwise
     */
    public static boolean isReservedKeyword(String name) {
        return reservedKeywords.contains(name);
    }
}
