package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ArrayTest {
    @Test
    void testTrivialArray() {
        String testFile = "c/array.c";
        assertTrue(TestHelper.runTest(testFile, testFile));
    }

    @Test
    void testEquivalentArray() {
        String testFile0 = "c/array.c";
        String testFile1 = "c/array_1.c";
        assertTrue(TestHelper.runTestReflexive(testFile0, testFile1));
    }

    @Test
    void testNotEquivalentArray() {
        String testFile0 = "c/array.c";
        String testFile1 = "c/array_wrong.c";

        assertFalse(TestHelper.runTest(testFile0, testFile1));
        assertFalse(TestHelper.runTest(testFile1, testFile0));
    }

    @Test
    void testTypedArrays() {
        assertTrue(TestHelper.runTestReflexive("c/typed_arrays.c", "c/typed_arrays_1.c"));
        assertFalse(TestHelper.runTestReflexive("c/typed_arrays.c", "c/typed_arrays_wrong.c"));
    }
}
