package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sg.edu.nus.se.its.model.Program;

/**
 * Unit tests for invalid programs in Patch Validator.
 */
public class InvalidTest {
  private static final Program validProgram = TestHelper.parseLocalProgramFile("python/arithmetic.py");
  private static final Program emptyProgram = TestHelper.parseLocalProgramFile("python/empty.py");

  @Test
  void testNullPrograms() {
    PatchValidator validator = new PatchValidator();
    assertFalse(validator.patchValidation(null, validProgram));
    assertFalse(validator.patchValidation(validProgram, null));
    assertFalse(validator.patchValidation(null, null));
  }

  @Test
  void testEmptyPrograms() {
    PatchValidator validator = new PatchValidator();

    assertFalse(validator.patchValidation(emptyProgram, validProgram));
    assertFalse(validator.patchValidation(validProgram, emptyProgram));
    assertFalse(validator.patchValidation(emptyProgram, null));
    assertFalse(validator.patchValidation(null, emptyProgram));
    assertFalse(validator.patchValidation(emptyProgram, emptyProgram));
  }

  @Test
  void testInvalidParams() {
    String testFile0 = "c/and_or_operators.c";
    String testFile1 = "c/arithmetic.c";

    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testInvalidComparison() {
    String testFile0 = "c/diffReturn.c";
    String testFile1 = "c/diffReturn_wrong.c";

    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testUnsupportedFeatures() {
    // printf with int in C
    String testFile0 = "c/printf.c";
    assertFalse(TestHelper.runTest(testFile0, testFile0));

    // char arrays in C
    String testFile1 = "c/char_arrays.py";
    assertFalse(TestHelper.runTest(testFile1, testFile1));

    // multiple functions
    String testFile2 = "c/multiple_functions.c";
    assertFalse(TestHelper.runTest(testFile2, testFile2));
    assertFalse(TestHelper.runTest(testFile0, testFile2));

    // dynamic typing in Python
    String testFile3 = "python/arithmetic.py";
    assertTrue(TestHelper.runTest(testFile3, testFile3));
  }
}
