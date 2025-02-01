package sg.edu.nus.se.its.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputOutputTest {
    @Test
    public void testTrivialStringPrintfPrograms() {
        String baseCFile = "c/hello_world.c";
        String basePyFile = "python/hello_world.py";
        assertTrue(TestHelper.runTest(baseCFile, baseCFile));
        assertTrue(TestHelper.runTest(basePyFile, basePyFile));
    }

    @Test
    public void testEquivalentStringPrintfPrograms() {
        String baseCFile = "c/hello_world.c";
        String equivalentCFile = "c/hello_world_1.c";
        assertTrue(TestHelper.runTest(baseCFile, equivalentCFile));

        String basePyFile = "python/hello_world.py";
        String equivalentPyFile = "python/hello_world_1.py";
        assertTrue(TestHelper.runTest(basePyFile, equivalentPyFile));
    }

    @Test
    public void testNonEquivalentStringPrintfPrograms() {
        String baseCFile = "c/hello_world.c";
        String wrongCFile = "c/hi.c";
        String wrongOrderCFile = "c/world_hello.c";
        assertFalse(TestHelper.runTest(baseCFile, wrongCFile));
        assertFalse(TestHelper.runTest(baseCFile, wrongOrderCFile));

        String basePyFile = "python/hello_world.py";
        String wrongPyFile = "python/hello_world_wrong.py";
        String wrongVariablePyFile = "python/wrongVariablePyFile.py";
        assertFalse(TestHelper.runTest(basePyFile, wrongPyFile));
        assertFalse(TestHelper.runTest(basePyFile, wrongVariablePyFile));
    }

    @Test
    public void testEmptyOutputPrograms() {
        String skeletonCFile = "c/blank.c";
        String printEmptyArgumentCFile = "c/print_nothing.c";
        assertTrue(TestHelper.runTest(skeletonCFile, printEmptyArgumentCFile));

        String skeletonPyFile = "python/blank.py";
        String printEmptyArgumentPyFile = "python/print_nothing.py";
        assertTrue(TestHelper.runTest(skeletonPyFile, printEmptyArgumentPyFile));
    }
}
