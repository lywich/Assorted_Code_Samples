package sg.edu.nus.se.its.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import org.javatuples.Pair;

import sg.edu.nus.se.its.model.Expression;
import sg.edu.nus.se.its.model.Function;
import sg.edu.nus.se.its.validation.solverexpressions.BaseExpression;

/**
 * The SolverWrapper class provides utility methods for checking the equivalence of two functions.
 */
public class SolverWrapper {
    /**
     * The context.
     */
    private final Context ctx;

    /**
     * The variables map.
     */
    @SuppressWarnings("rawtypes")
    private final Map<String, Expr> variables;

    /**
     * The counter example.
     */
    private String counterExample = "";

    /**
     * Constructs a SolverWrapper with a new context.
     */
    public SolverWrapper() {
        this.ctx = new Context();
        this.variables = new HashMap<>();
    }

    /**
     * Gets the counter example.
     *
     * @return The counter example.
     */
    public String getCounterExample() {
        return counterExample;
    }

    /**
     * Gets the context.
     * 
     * @return The context.
     */
    public Context getCtx() {
        return ctx;
    }

    /**
     * Evaluates the given expression and updates the result in the variables map.
     *
     * @param varRemapping  The variable remapping map.
     * @param lhsVariable   The left-hand side variable name.
     * @param expression    The expression to evaluate.
     */
    @SuppressWarnings("rawtypes")
    private void evaluateExpression(
        Map<String, String> varRemapping,
        String lhsVariable,
        Expression expression) {
        BaseExpression parsedExpression = ExpressionFactory.parseExpression(expression);
        Expr result = parsedExpression.evaluate(this, varRemapping);
        updateVariable(varRemapping, lhsVariable, result);
    }

    public String getVariableName(
        Map<String, String> varRemapping,
        String variableName) {
        String newVarName = varRemapping.getOrDefault(variableName, variableName);
        if (!varRemapping.isEmpty() && !varRemapping.containsKey(variableName)) {
            newVarName = "$$" + newVarName;
        }
        return newVarName;
    }

    /**
     * Retrieves the variable from the variables map based on the variable name and variable remapping.
     *
     * @param varRemapping  The variable remapping map.
     * @param variableName  The variable name.
     * @return The variable expression.
     */
    @SuppressWarnings("rawtypes")
    public Expr getVariable(
        Map<String, String> varRemapping,
        String variableName) {
        String varName = getVariableName(varRemapping, variableName);
        return variables.get(varName);
    }

    /**
     * Updates the variable in the variables map based on the variable name, variable remapping, and new value.
     *
     * @param varRemapping  The variable remapping map.
     * @param variableName  The variable name.
     * @param value         The new value of the variable.
     */
    @SuppressWarnings("rawtypes")
    public void updateVariable(
        Map<String, String> varRemapping,
        String variableName,
        Expr value) {
        String varName = getVariableName(varRemapping, variableName);
        variables.put(varName, value);
    }

    /**
     * Builds the function expression based on the given expressions, parameters, and variable remapping.
     *
     * @param expressionLists   The list of expressions.
     * @param transitions       The expressions' transition map.
     * @param varRemapping  The variable remapping map.
     * @return The array of output expressions.
     */
    @SuppressWarnings("rawtypes")
    private Expr[] buildFunction(
        HashMap<Integer, ArrayList<Pair<String, Expression>>> expressionLists,
        HashMap<Integer, HashMap<Boolean, Integer>> transitions,
        Map<String, String> varRemapping,
        List<Pair<String, Expr>> paramsWithIdentifier) {
        initialiseVariables(paramsWithIdentifier, varRemapping);
        ArrayList<Pair<String, Expression>> initExpressions = expressionLists.get(1);
        int expressionLoc  = executeLocalExpression(initExpressions, transitions.get(1), varRemapping);
        ArrayList<Pair<String, Expression>> localExpressions = expressionLists.get(expressionLoc);

        while (localExpressions != null) {
            // Execute the local expressions
            expressionLoc = executeLocalExpression(localExpressions, transitions.get(expressionLoc), varRemapping);
            localExpressions = expressionLists.get(expressionLoc);
        }

        return Stream.of("$ret", "$out")
                .map(x -> getVariable(varRemapping, x))
                .toArray(Expr[]::new);
    }

    /**
     * Executes a list of local expressions, evaluating them and returning an integer result based on a transition map.
     * Handles conditional expressions when present.
     *
     * @param expressions List of pairs containing strings and expressions.
     * @param transition Boolean-to-integer map determining the output based on expression evaluations.
     * @param varRemapping The variable remapping map.
     * @return Integer indicating the next expression location to evaluate.
     */
    @SuppressWarnings("rawtypes")
    private int executeLocalExpression(
        ArrayList<Pair<String, Expression>> expressions,
        HashMap<Boolean, Integer> transition,
        Map<String, String> varRemapping) {
        if (expressions.size() > 0 && expressions.get(0).getValue0().equals("$cond")) {
            // Handle conditional expressions
            Pair<String, Expression> condExpr = expressions.get(0);
            evaluateExpression(varRemapping, condExpr.getValue0(), condExpr.getValue1());
            Expr condResult = getVariable(varRemapping, "$cond").simplify();
            BoolExpr isCondTrue;
            if (condResult.isBool()){
                isCondTrue = (BoolExpr) condResult;
            }else{
                isCondTrue = ctx.mkNot(ctx.mkEq(condResult, ctx.mkInt(0)));
            }
            isCondTrue = (BoolExpr) isCondTrue.simplify();
            return transition.get(isCondTrue.isTrue());
        } else{
            for (Pair<String, Expression> expression : expressions) {
                evaluateExpression(
                    varRemapping,
                    expression.getValue0(),
                    expression.getValue1());
            }
            if (transition.size() > 0){
                return transition.get(true);
            }else {
                return 0;
            }
        }
    }


    /**
     * Checks if two functions are equivalent.
     *
     * @param p1 The first function.
     * @param p2 The second function.
     * @return True if the functions are equivalent, false otherwise.
     */
    @SuppressWarnings("rawtypes")
    public boolean areFunctionsEquivalent(Function p1, Function p2) {
        if (p1 == null || p2 == null) {
            return false;
        }

        List<Pair<String, String>> p1Params = p1.getParams();
        List<Pair<String, String>> p2Params = p2.getParams();

        HashMap<Integer, ArrayList<Pair<String, Expression>>> p1LocExprs = p1.getLocexprs();
        HashMap<Integer, ArrayList<Pair<String, Expression>>> p2LocExprs = p2.getLocexprs();

        HashMap<Integer, HashMap<Boolean, Integer>> p1Transitions = p1.getLoctrans();
        HashMap<Integer, HashMap<Boolean, Integer>> p2Transitions = p2.getLoctrans();

        // Create all possible mappings between the parameters of the two functions
        HashMap<String, ArrayList<String>> p1ParamMap = new HashMap<>();
        HashMap<String, ArrayList<String>> p2ParamMap = new HashMap<>();
        boolean canMapParams = Parameter.canMap(p1Params, p2Params, p1ParamMap, p2ParamMap);
        if (!canMapParams) {
            return false;
        }

        List<List<Pair<Pair<String, String>, String>>> mappingsWithType = Parameter.formMapping(p1ParamMap, p2ParamMap);
        for (List<Pair<Pair<String, String>, String>> mapping : mappingsWithType) {
            Parameter.ParamsInformation paramsInformation = Parameter.getParamsInformation(mapping, this);
            Expr[] funcA = buildFunction(p1LocExprs,
                p1Transitions,
                new HashMap<>(),
                paramsInformation.paramsWithIdentifier);
            Expr[] funcB = buildFunction(p2LocExprs,
                p2Transitions,
                paramsInformation.p2VarRemapping,
                paramsInformation.paramsWithIdentifier);

            if (solve(funcA, funcB, paramsInformation.params)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Initialises the variables map with the given parameters.
     *
     * @param paramsWithIdentifier The parameters with their identifiers.
     * @param varRemapping The variable remapping map.
     */
    @SuppressWarnings("rawtypes")
    private void initialiseVariables(List<Pair<String, Expr>> paramsWithIdentifier,
        Map<String, String> varRemapping) {
        variables.clear();
        for (Pair<String, Expr> param : paramsWithIdentifier) {
            String paramName = param.getValue0();
            variables.put(paramName, param.getValue1());
        }
        updateVariable(varRemapping, "$ret", ctx.mkString(""));
        updateVariable(varRemapping, "$out", ctx.mkString(""));
    }

    /**
     * Solves the given functions and finds a counter example if they are not equivalent.
     *
     * @param funcA The first function.
     * @param funcB The second function.
     * @param params The parameters of the functions.
     * @return True if the functions are equivalent, false otherwise.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean solve(
        Expr[] funcA,
        Expr[] funcB,
        Expr[] params) {
            try {

                for (int i = 0; i < funcA.length; i++) {
                    BoolExpr notEquivalent = ctx.mkNot(ctx.mkEq(funcA[i], funcB[i]));
                    Solver solver = ctx.mkSolver();
                    solver.add(notEquivalent);

                    Status status = solver.check();
                    if (status == Status.SATISFIABLE) {
                        findCounterExample(solver, params);
                        System.out.println(counterExample);
                        return false;
                    } else if (status == Status.UNKNOWN) {
                        System.out.println("Unknown conclusion when testing for equivalence");
                        return false;
                    }
                }

                System.out.println("The functions are equivalent for all inputs.");
                return true;

            } catch (Z3Exception ignored) {
                System.err.println("The two programs do not align.");
            }
            return false;
    }


    /**
     * Finds a counter example for the functions.
     *
     * @param solver The solver.
     * @param params The parameters of the functions.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void findCounterExample(Solver solver, Expr[] params) {
        Model model = solver.getModel();
        StringBuilder counterExampleTemp = new StringBuilder();
        // Store the specific values of the arguments that caused the functions to be different
        for (Expr param : params) {
            Expr value = model.eval(param, false);
            if (value != null && value.toString().chars().anyMatch(Character::isAlphabetic)){
                counterExampleTemp.append(param).append(": For all ").append(value).append("\n");
            } else{
                counterExampleTemp.append(param).append(": ").append(value).append("\n");
            }
        }

        counterExample = counterExampleTemp.toString();
    }

    /**
     * Creates an array constant based on the given variable name and type.
     *
     * @param varName The variable name.
     * @param type The type of the array.
     * @return The array constant.
     */
    @SuppressWarnings("rawtypes")
    public Expr makeArrayConst(String varName, String type) {
        switch (type) {
        case "bool":
            return ctx.mkArrayConst(varName,
                ctx.getIntSort(),
                ctx.getBoolSort());
        case "int":
            return ctx.mkArrayConst(varName,
                ctx.getIntSort(),
                ctx.getIntSort());
        case "float":
        case "double":
            return ctx.mkArrayConst(varName,
                ctx.getIntSort(),
                ctx.getRealSort());
        default:
            assert !type.equals("char") : "char arrays are currently being treated as string";
            throw new IllegalArgumentException("Unknown array type");
        }
    }
}
