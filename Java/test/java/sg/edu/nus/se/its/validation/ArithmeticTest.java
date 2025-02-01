package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

/**
 * Basic unit tests for Patch Validator.
 */
public class ArithmeticTest {
  @Test
  void testTrivialArithmetic() {
    String testFile = "c/arithmetic.c";
    assertTrue(TestHelper.runTest(testFile, testFile));
  }

  @Test
  void testEquivalentArithmetic() {
    String testFile0 = "c/arithmetic.c";
    String testFile1 = "c/arithmetic_1.c";
    assertTrue(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testNotEquivalentArithmetic() {
    String testFile0 = "c/arithmetic.c";
    String testFile1 = "c/arithmetic_wrong.c";

    Pair<Boolean, String> results = TestHelper.runTestWithCounterExample(testFile0, testFile1);
    assertFalse(results.getValue0());
    assertNotEquals("", results.getValue1());
  }
}
