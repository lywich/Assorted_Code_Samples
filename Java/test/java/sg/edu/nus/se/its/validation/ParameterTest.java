package sg.edu.nus.se.its.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ParameterTest {
    @Test
    void canMapReturnsTrueForEqualLists() {
        List<Pair<String, String>> p1Params = new ArrayList<>();
        p1Params.add(new Pair<>("param1", "type1"));
        p1Params.add(new Pair<>("param2", "type2"));

        List<Pair<String, String>> p2Params = new ArrayList<>();
        p2Params.add(new Pair<>("paramA", "type1"));
        p2Params.add(new Pair<>("paramB", "type2"));

        HashMap<String, ArrayList<String>> p1ParamMap = new HashMap<>();
        HashMap<String, ArrayList<String>> p2ParamMap = new HashMap<>();

        assertTrue(Parameter.canMap(p1Params, p2Params, p1ParamMap, p2ParamMap));
    }

    @Test
    void canMapReturnsFalseForDifferentLists() {
        List<Pair<String, String>> p1Params = new ArrayList<>();
        p1Params.add(new Pair<>("param1", "type1"));

        List<Pair<String, String>> p2Params = new ArrayList<>();
        p2Params.add(new Pair<>("paramA", "type1"));
        p2Params.add(new Pair<>("paramB", "type2"));

        HashMap<String, ArrayList<String>> p1ParamMap = new HashMap<>();
        HashMap<String, ArrayList<String>> p2ParamMap = new HashMap<>();

        assertFalse(Parameter.canMap(p1Params, p2Params, p1ParamMap, p2ParamMap));
    }

    @Test
    void formMappingReturnsCorrectMapping() {
        HashMap<String, ArrayList<String>> p1ParamMap = new HashMap<>();
        p1ParamMap.put("type1", new ArrayList<>(List.of("param1", "param2")));
        p1ParamMap.put("type2", new ArrayList<>(List.of("param3", "param4")));

        HashMap<String, ArrayList<String>> p2ParamMap = new HashMap<>();
        p2ParamMap.put("type1", new ArrayList<>(List.of("paramA", "paramB")));
        p2ParamMap.put("type2", new ArrayList<>(List.of("paramC", "paramD")));

        List<List<Pair<Pair<String, String>, String>>> mappings = Parameter.formMapping(p1ParamMap, p2ParamMap);

        assertEquals(4, mappings.size());

        // Test each possible mapping
        assertTrue(mappings.contains(
            List.of(
                new Pair<>(new Pair<>("param3", "paramC"), "type2"),
                new Pair<>(new Pair<>("param4", "paramD"), "type2"),
                new Pair<>(new Pair<>("param1", "paramA"), "type1"),
                new Pair<>(new Pair<>("param2", "paramB"), "type1")
            )
        ));
        assertTrue(mappings.contains(
            List.of(
                new Pair<>(new Pair<>("param3", "paramC"), "type2"),
                new Pair<>(new Pair<>("param4", "paramD"), "type2"),
                new Pair<>(new Pair<>("param1", "paramB"), "type1"),
                new Pair<>(new Pair<>("param2", "paramA"), "type1")
            )
        ));
        assertTrue(mappings.contains(
            List.of(
                new Pair<>(new Pair<>("param3", "paramD"), "type2"),
                new Pair<>(new Pair<>("param4", "paramC"), "type2"),
                new Pair<>(new Pair<>("param1", "paramA"), "type1"),
                new Pair<>(new Pair<>("param2", "paramB"), "type1")
            )
        ));
        assertTrue(mappings.contains(
            List.of(
                new Pair<>(new Pair<>("param3", "paramD"), "type2"),
                new Pair<>(new Pair<>("param4", "paramC"), "type2"),
                new Pair<>(new Pair<>("param1", "paramB"), "type1"),
                new Pair<>(new Pair<>("param2", "paramA"), "type1")
            )
        ));
    }

    @Test
    public void testBijectiveParameterNotNull() {
        assertNotNull(new Parameter());
    }
}
