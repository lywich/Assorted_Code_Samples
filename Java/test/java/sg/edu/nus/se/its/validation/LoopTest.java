package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for loop behavior validation.
 */
public class LoopTest {

  // c language tests
  @Test
  void testCSimpleLoop() {
    // Assuming testFile and testFile1 have similar loop structures that are meant to be equivalent
    String testFile = "c/loop/simple_loop.c";
    String testFile1 = "c/loop/simple_loop_variant.c";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCNonEquivalentLoops() {
    // Assuming testFile and testFile1 have different loop conditions that are meant to be non-equivalent
    String testFile = "c/loop/Condition1.c";
    String testFile1 = "c/loop/Condition2.c";
    assertFalse(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCNonEquivalentLoops1() {
    // Assuming testFile and testFile1 have different loop body that are meant to be non-equivalent
    String testFile = "c/loop-c.c";
    String testFile1 = "c/loop-i.c";
    assertFalse(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCInfiniteLoop() {
    // Test to ensure that the method can identify infinite loops; assumes both files contain infinite loops
    String testFile0 = "c/loop/infinite_loop.c";
    String testFile1 = "c/loop/infinite_loop_variant.c";
    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testCOneInfiniteLoops() {
    // Tests two loops that are not equivalent; one is infinite, the other finite
    String testFile0 = "c/loop/finite_loop.c";
    String testFile1 = "c/loop/infinite_loop.c";
    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testCLoopOptimization() {
    // Tests an original loop against an optimized version of the same loop, expecting them to be equivalent
    String testFile = "c/loop/loop_unoptimized.c";
    String testFile1 = "c/loop/loop_optimized.c";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCLoopWithBreak() {
    // Tests loops with break conditions, ensuring they are equivalent if they have the same behavior
    String testFile0 = "c/loop/loop_with_break.c";
    String testFile1 = "c/loop/loop_with_break_variant.c";
    assertTrue(TestHelper.runTest(testFile0, testFile1));
  }

  // python language tests
  @Test
  void test_py_simple_loop() {
    // Assuming testFile and testFile1 have similar loop structures that are meant to be equivalent
    String testFile = "python/loop/simple_loop.py";
    String testFile1 = "python/loop/simple_loop_variant.py";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }
  @Test
  void testPyRangeEquivlance(){
    // Assuming testFile and testFile1 have similar loop structures that are meant to be equivalent
    String testFile = "python/loop/range_3_paras.py";
    String testFile1 = "python/loop/range_2_paras.py";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testPyRangeNonEquivlance(){
    // Assuming testFile and testFile1 have similar loop structures that are meant to be equivalent
    String testFile = "python/loop/range_3_paras.py";
    String testFile1 = "python/loop/range_3_paras_step_2.py";
    assertFalse(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testPyNonEquivalentLoops() {
    // Assuming testFile and testFile1 have different loop conditions that are meant to be non-equivalent
    String testFile = "python/loop/Condition1.py";
    String testFile1 = "python/loop/Condition2.py";
    assertFalse(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testPyInfiniteLoop() {
    // Test to ensure that the method can identify infinite loops; assumes both files contain infinite loops
    String testFile0 = "python/loop/infinite_loop.py";
    String testFile1 = "python/loop/infinite_loop_variant.py";
    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testPyOneInfiniteLoops() {
    // Tests two loops that are not equivalent; one is infinite, the other finite
    String testFile0 = "python/loop/finite_loop.py";
    String testFile1 = "python/loop/infinite_loop.py";
    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void test_py_loop_optimization() {
    // Tests an original loop against an optimized version of the same loop, expecting them to be equivalent
    String testFile = "python/loop/loop_unoptimized.py";
    String testFile1 = "python/loop/loop_optimized.py";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void test_py_loop_with_break() {
    // Tests loops with break conditions, ensuring they are equivalent if they have the same behavior
    String testFile0 = "python/loop/loop_with_break.py";
    String testFile1 = "python/loop/loop_with_break_variant.py";
    assertTrue(TestHelper.runTest(testFile0, testFile1));
  }

  @Test
  void FloorDivTest(){
    String testFile = "python/FloorDiv.py";
    String testFile1 = "python/FloorDiv2.py";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void FloorDivTestNotEq(){
    String testFile = "python/FloorDiv2.py";
    String testFile1 = "python/FloorDiv3.py";
    assertFalse(TestHelper.runTest(testFile, testFile1));
  }

  // python and c language tests
  @Test
  void testCPySimpleLoop() {
    // Assuming testFile and testFile1 have similar loop structures that are meant to be equivalent
    String testFile = "c/loop/simple_loop.c";
    String testFile1 = "python/loop/simple_loop_variant.py";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCPyNonEquivalentLoops() {
    // Assuming testFile and testFile1 have different loop conditions that are meant to be non-equivalent
    String testFile = "c/loop/Condition1.c";
    String testFile1 = "python/loop/Condition2.py";
    assertFalse(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCPyInfiniteLoop() {
    // Test to ensure that the method can identify infinite loops; assumes both files contain infinite loops
    String testFile0 = "c/loop/infinite_loop.c";
    String testFile1 = "python/loop/infinite_loop_variant.py";
    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  @Test
  void testCPyOneInfiniteLoops() {
    // Tests two loops that are not equivalent; one is infinite, the other finite
    String testFile0 = "c/loop/finite_loop.c";
    String testFile1 = "python/loop/infinite_loop.py";
    assertFalse(TestHelper.runTestReflexive(testFile0, testFile1));
  }

  void test_c_py_loop_optimization() {
    // Tests an original loop against an optimized version of the same loop, expecting them to be equivalent
    String testFile = "c/loop/loop_unoptimized.c";
    String testFile1 = "python/loop/loop_optimized.py";
    assertTrue(TestHelper.runTest(testFile, testFile1));
  }

  @Test
  void testCPyLoopWithBreak() {
    // Tests loops with break conditions, ensuring they are equivalent if they have the same behavior
    String testFile0 = "c/loop/loop_with_break.c";
    String testFile1 = "python/loop/loop_with_break_variant.py";
    assertTrue(TestHelper.runTest(testFile0, testFile1));
  }
}
