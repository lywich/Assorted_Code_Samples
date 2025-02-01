package sg.edu.nus.se.its.validation;

import java.util.Collection;
import java.util.List;

import org.javatuples.Pair;

import sg.edu.nus.se.its.model.Expression;
import sg.edu.nus.se.its.model.Function;
import sg.edu.nus.se.its.model.Program;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Verification module based on program equivalence checking.
 */
public class PatchValidator{
  /**
   * Wrapper for the solver.
   */
  private final SolverWrapper solverWrapper;

  /**
   * Executor service for timeout checking.
   */
  private final ExecutorService executor = Executors.newCachedThreadPool();

  /**
   * Timeout for the verification process(in seconds).
   */
  private final long TIMEOUT = 60;

  /**
   * Debug flag.
   */
  public static final boolean DEBUG = true;

  /**
   * Constructs a new PatchValidator.
   */
  public PatchValidator() {
    this.solverWrapper = new SolverWrapper();
  }

  /**
   * Gets the counter example from the solver.
   *
   * @return Counter example.
   */
  public String getCounterExample() {
    return solverWrapper.getCounterExample();
  }

  /**
   * Checks if two programs are equivalent.
   *
   * @param referenceProgram Reference program.
   * @param fixedProgram Fixed program.
   * @return True if the programs are equivalent, false otherwise.
   */
  public boolean patchValidation(Program referenceProgram, Program fixedProgram) {
    Future<Boolean> future = null;

    try {
      Pair<Function, Function> functions = extractFunctions(
          new Pair<>(referenceProgram, fixedProgram));

      Function f0 = functions.getValue0();
      Function f1 = functions.getValue1();

      if (DEBUG) {
        PatchValidator.printExpressions("Reference", f0);
        PatchValidator.printExpressions("Fixed", f1);
      }

      // Define a callable task for checking function equivalence
      Callable<Boolean> task = () -> solverWrapper.areFunctionsEquivalent(f0, f1);

      // Submit the task to the executor service and get a Future object
      future = executor.submit(task);

      return future.get(TIMEOUT, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      System.err.println("Timeout occurred while checking function equivalence.");
      // Attempt to cancel the execution
      future.cancel(true);
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Extracts functions from the programs.
   *
   * @param programs Programs.
   * @return Pair of functions.
   */
  private static Pair<Function, Function> extractFunctions(Pair<Program, Program> programs) {
    Program p0 = programs.getValue0();
    Program p1 = programs.getValue1();

    if (p0 == null || p1 == null) {
      throw new IllegalArgumentException("Null program provided. Failed to extract functions.");
    }

    Collection<Function> p0Funcs = p0.getFncs().values();
    Collection<Function> p1Funcs = p1.getFncs().values();

    if (p0Funcs.size() > 1 || p1Funcs.size() > 1) {
      throw new IllegalArgumentException("We do not support programs with multiple functions.");
    }

    Function f0 = null, f1 = null;

    for (Function x : p0Funcs) {
      f0 = x;
      break;
    }

    for (Function x : p1Funcs) {
      f1 = x;
      break;
    }

    if (f0 == null || f1 == null) {
      throw new IllegalArgumentException("Programs should have 1 function defined.");
    }

    return new Pair<>(f0, f1);
  }

  /**
   * Prints the expressions in a function.
   *
   * @param name Name of the program.
   * @param f Function.
   */
  private static void printExpressions(String name, Function f) {
    System.out.println("Program " + name);

    int exprLoc = 1;
    for (int loc : f.getLocexprs().keySet()) {
      System.out.println("Loc " + loc + " : " + f.getLocdescAt(loc));
      List<Pair<String, Expression>> expressions = f.getLocexprs().get(loc);
      for (Pair<String, Expression> expression : expressions) {
        System.out.println("  " + exprLoc + ") " + expression.getValue0() + " : " + expression.getValue1());
        exprLoc++;
      }
      System.out.println();
    }
  }
}
