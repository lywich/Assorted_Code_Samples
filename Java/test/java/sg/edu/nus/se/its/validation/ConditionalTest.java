package sg.edu.nus.se.its.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ConditionalTest {
  @Test
  void testComparisonOperatorsPrograms() {
    // test larger than
    String highestInput = "c/highest_input.c";
    String equivalentHighestInput = "c/highest_input_1.c";
    String lowestInput = "c/lowest_input.c";
    String equivalentLowestInput = "c/lowest_input_1.c";
    assertTrue(TestHelper.runTestReflexive(highestInput, equivalentHighestInput));
    assertFalse(TestHelper.runTestReflexive(highestInput, lowestInput));
    // test lesser than
    assertTrue(TestHelper.runTestReflexive(lowestInput, equivalentLowestInput));
    assertFalse(TestHelper.runTestReflexive(lowestInput, highestInput));
  }

  @Test
  void testLargerEqualPrograms() {
    // test larger or equal than
    String largerEqualComparison = "c/larger_equal.c";
    String equivalentLargerEqualComparison = "c/larger_equal_1.c";
    String nonEquivalentLargerEqualComparison = "c/larger_equal_wrong.c";
    assertTrue(TestHelper.runTestReflexive(largerEqualComparison, equivalentLargerEqualComparison));
    assertFalse(TestHelper.runTestReflexive(largerEqualComparison, nonEquivalentLargerEqualComparison));
  }

  @Test
  void testLesserEqualPrograms() {
    // test larger or equal than
    String lesserEqualComparison = "c/lesser_equal.c";
    String equivalentLesserEqualComparison = "c/lesser_equal_1.c";
    String nonEquivalentLesserEqualComparison = "c/lesser_equal_wrong.c";
    assertTrue(TestHelper.runTestReflexive(lesserEqualComparison, equivalentLesserEqualComparison));
    assertFalse(TestHelper.runTestReflexive(lesserEqualComparison, nonEquivalentLesserEqualComparison));
  }

  @Test
  void testMultipleComparisonPrograms() {
    // test multiple comparison
    String findHighest = "c/find_highest.c";
    String equivalentFindHighest = "c/find_highest_1.c";
    String nonEquivalentFindHighest = "c/find_highest_wrong.c";
    assertTrue(TestHelper.runTestReflexive(findHighest, equivalentFindHighest));
    assertFalse(TestHelper.runTestReflexive(findHighest, nonEquivalentFindHighest));
  }

  @Test
  void testLogicalEqualOperatorPrograms() {
    // test equal
    String equalCondional = "c/equivalent.c";
    String equivalentEqualConditional = "c/equivalent_1.c";
    String nonEquivalentEqualConditional = "c/equivalent_wrong.c";
    assertTrue(TestHelper.runTestReflexive(equalCondional, equivalentEqualConditional));
    assertFalse(TestHelper.runTestReflexive(equalCondional, nonEquivalentEqualConditional));
  }

  @Test
  void testLogicalNotOperatorPrograms() {
    // test not operator
    String nonOperator = "c/not_operator.c";
    String equivalentNonOperator = "c/not_operator_1.c";
    String nonEquivalentNonOperator = "c/not_operator_wrong.c";
    assertTrue(TestHelper.runTestReflexive(nonOperator, equivalentNonOperator));
    assertFalse(TestHelper.runTestReflexive(nonOperator, nonEquivalentNonOperator));
  }

  @Test
  void testLogicalAndOrOperatorsProgram() {
    // test and, or
    String andOrOperator = "c/and_or_operators.c";
    String equivalentAndOrOperator = "c/and_or_operators_1.c";
    String nonEquivalentAndOrOperator = "c/and_or_operators_wrong.c";
    assertTrue(TestHelper.runTestReflexive(andOrOperator, equivalentAndOrOperator));
    assertFalse(TestHelper.runTestReflexive(andOrOperator, nonEquivalentAndOrOperator));
  }
}
