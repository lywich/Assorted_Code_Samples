package sg.edu.nus.se.its.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

/**
 * The Parameter class provides the functionality to get the parameters information.
 */
public class Parameter {
    /**
     * Gets the parameters information based on the given mapping.
     *
     * @param mapping The mapping.
     * @param solverWrapper The solver wrapper.
     * @return The parameter's information.
     */
    @SuppressWarnings("rawtypes")
    public static ParamsInformation getParamsInformation(
        List<Pair<Pair<String, String>, String>> mapping,
        SolverWrapper solverWrapper) {
        Context ctx = solverWrapper.getCtx();

        Expr[] params = new Expr[mapping.size()];
        List<Pair<String, Expr>> paramsWithIdentifier = new ArrayList<>();
        Map<String, String> p2VarRemapping = new HashMap<>();

        for (int i = 0; i < mapping.size(); i++) {
            Pair<Pair<String, String>, String> pair = mapping.get(i);
            Pair<String, String> p = pair.getValue0();
            String varName = p.getValue0();
            String oldVarName = p.getValue1();
            String type = pair.getValue1();

            Expr var;
            switch(type) {
            case "bool_array":
                var = solverWrapper.makeArrayConst(varName, "bool");
                break;
            case "int_array":
                var = solverWrapper.makeArrayConst(varName, "int");
                break;
            case "float_array":
            case "double_array":
                var = solverWrapper.makeArrayConst(varName, "float");
                break;
            case "bool":
                var = ctx.mkBoolConst(varName);
                break;
            case "float":
            case "double":
                var = ctx.mkRealConst(varName);
                break;
            case "int":
                var = ctx.mkIntConst(varName);
                break;
            default:
                System.err.format("Unable to determine the type for parameter '%s'. Using int as the default type.\n",
                    varName);
                var = ctx.mkIntConst(varName);
                break;
            }
            paramsWithIdentifier.add(new Pair<>(varName, var));
            params[i] = var;
            p2VarRemapping.put(oldVarName, varName);
        }

        return new ParamsInformation(params, paramsWithIdentifier, p2VarRemapping);
    }
    
    /**
     * Check if two functions can be mapped to each other
     * 
     * @param p1Params Parameters of the first function
     * @param p2Params Parameters of the second function
     * @param p1ParamMap Mapping of parameters of the first function
     * @param p2ParamMap Mapping of parameters of the second function
     * @return True if the functions can be mapped to each other, false otherwise
     */
    public static boolean canMap(@NotNull List<Pair<String, String>> p1Params,
                                 List<Pair<String, String>> p2Params,
                                 HashMap<String, ArrayList<String>> p1ParamMap,
                                 HashMap<String, ArrayList<String>> p2ParamMap) {
        for (Pair<String, String> p1Param : p1Params) {
            Pair<String, String> parsedParams = parseInputType(p1Param.getValue0(), p1Param.getValue1());
            String varName = parsedParams.getValue0();
            String type = parsedParams.getValue1();
            p1ParamMap.putIfAbsent(type, new ArrayList<>());
            p1ParamMap.get(type).add(varName);
        }

        for (Pair<String, String> p2Param : p2Params) {
            Pair<String, String> parsedParams = parseInputType(p2Param.getValue0(), p2Param.getValue1());
            String varName = parsedParams.getValue0();
            String type = parsedParams.getValue1();
            p2ParamMap.putIfAbsent(type, new ArrayList<>());
            p2ParamMap.get(type).add(varName);
        }

        for (String type : p1ParamMap.keySet()) {
            ArrayList<String> p1ParamsOfType = p1ParamMap.get(type);
            ArrayList<String> p2ParamsOfType = p2ParamMap.getOrDefault(type, new ArrayList<>());

            if (p1ParamsOfType.size() != p2ParamsOfType.size()) {
                return false;
            }
        }

        for (String type : p2ParamMap.keySet()) {
            ArrayList<String> p2ParamsOfType = p2ParamMap.get(type);
            ArrayList<String> p1ParamsOfType = p1ParamMap.getOrDefault(type, new ArrayList<>());

            if (p2ParamsOfType.size() != p1ParamsOfType.size()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Form mappings between parameters of two functions
     * 
     * @param p1ParamMap Mapping of parameters of the first function
     * @param p2ParamMap Mapping of parameters of the second function
     * @return List of mappings between parameters of two functions
     */
    public static List<List<Pair<Pair<String, String>, String>>> formMapping(
        HashMap<String, ArrayList<String>> p1ParamMap,
        HashMap<String, ArrayList<String>> p2ParamMap) {
        // Form mappings
        HashMap<String, List<List<Pair<String, String>>>> eachBijections = new HashMap<>();
        for (String type : p1ParamMap.keySet()) {
            ArrayList<String> p1ParamsOfType = p1ParamMap.get(type);
            ArrayList<String> p2ParamsOfType = p2ParamMap.getOrDefault(type, new ArrayList<>());

            List<List<Pair<String, String>>> bijections = findAllBijections(p1ParamsOfType, p2ParamsOfType);
            eachBijections.put(type, bijections);
        }

        String[] keys = eachBijections.keySet().toArray(new String[0]);
        List<List<List<Pair<String, String>>>> permutations = new ArrayList<>();
        List<List<Pair<String, String>>> current = new ArrayList<>();
        findPermutations(eachBijections, permutations, current, keys, 0);

        List<List<Pair<Pair<String, String>, String>>> mappingsWithType = new ArrayList<>();
        for (List<List<Pair<String, String>>> permutation : permutations) {
            List<Pair<Pair<String, String>, String>> mappingWithType = new ArrayList<>();
            for (int i = 0; i < permutation.size(); i++) {
                String type = keys[i];
                for (Pair<String, String> params : permutation.get(i)) {
                    mappingWithType.add(new Pair<>(params, type));
                }
            }
            mappingsWithType.add(mappingWithType);
        }

        return mappingsWithType;
    }

    /**
     * Parse the input type
     * 
     * @param name Name of the parameter
     * @param type Type of the parameter
     * @return Pair of the name and type of the parameter
     */
    private static Pair<String, String> parseInputType(String name, String type) {
        if (name.length() <= 2) {
            return new Pair<>(name, type);
        }
        String lastTwoCharacters = name.substring(name.length() - 2);
        String nameWithoutBrackets = name.substring(0, name.length() - 2);
        if (lastTwoCharacters.equals("[]")) {
            // transforms int arr[] -> arr: int[]
            // returns int_array, bool_array, char_array etc
            return new Pair<>(nameWithoutBrackets, type + "_array");
        } else {
            return new Pair<>(name, type);
        }
    }

    /**
     * Find all bijections between two lists
     * 
     * @param list1 First list
     * @param list2 Second list
     * @return List of bijections between two lists
     */
    private static List<List<Pair<String, String>>> findAllBijections(List<String> list1, List<String> list2) {
        List<List<Pair<String, String>>> bijections = new ArrayList<>();
        findAllBijectionsHelper(list1, list2, new ArrayList<>(), bijections);
        return bijections;
    }

    /**
     * Helper function to find all bijections between two lists
     * 
     * @param list1 First list
     * @param list2 Second list
     * @param currentBijection Current bijection
     * @param bijections List of bijections
     */
    private static void findAllBijectionsHelper(List<String> list1, List<String> list2, List<Pair<String, String>> currentBijection, List<List<Pair<String, String>>> bijections) {
        if (currentBijection.size() == list1.size()) {
            // If the bijection is complete, add it to the list of bijections
            bijections.add(new ArrayList<>(currentBijection));
            return;
        }

        // Try all possible mappings for the current element
        String currentKey = list1.get(currentBijection.size());
        for (String value : list2) {
            if (!isValueUsed(currentBijection, value)) {
                currentBijection.add(new Pair<>(currentKey, value));
                findAllBijectionsHelper(list1, list2, currentBijection, bijections);
                currentBijection.remove(currentBijection.size() - 1);
            }
        }
    }

    /**
     * Check if a value is already used in a bijection
     * 
     * @param bijection List of pairs representing a bijection
     * @param value Value to check
     * @return True if the value is already used in the bijection, false otherwise
     */
    private static boolean isValueUsed(List<Pair<String, String>> bijection, String value) {
        for (Pair<String, String> pair : bijection) {
            if (pair.getValue1().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find all permutations of bijections between parameters of two functions
     * 
     * @param hashMap Mapping of parameters of two functions
     * @param permutations List of permutations of bijections
     * @param current Current permutation
     * @param keys Keys of the mapping
     * @param index Index of the key
     */
    private static void findPermutations(HashMap<String, List<List<Pair<String, String>>>> hashMap,
        List<List<List<Pair<String, String>>>> permutations,
        List<List<Pair<String, String>>> current,
        String[] keys,
        int index) {
        if (index == keys.length) {
            permutations.add(new ArrayList<>(current));
            return;
        }

        String key = keys[index];

        List<List<Pair<String, String>>> values = hashMap.get(key);
        for (List<Pair<String, String>> value : values) {
            current.add(value);
            findPermutations(hashMap, permutations, current, keys, index + 1);
            current.remove(current.size() - 1);
        }
    }

    /**
     * The ParamsInformation class represents the information of the parameters.
     */
    public static class ParamsInformation {
        @SuppressWarnings("rawtypes")
        final Expr[] params;
        @SuppressWarnings("rawtypes")
        final List<Pair<String, Expr>> paramsWithIdentifier;
        final Map<String, String> p2VarRemapping;

        @SuppressWarnings("rawtypes")
        public ParamsInformation(Expr[] params,
            List<Pair<String, Expr>> paramsWithIdentifier,
            Map<String, String> p2VarRemapping) {
            this.params = params;
            this.paramsWithIdentifier = paramsWithIdentifier;
            this.p2VarRemapping = p2VarRemapping;
        }
    }
}
