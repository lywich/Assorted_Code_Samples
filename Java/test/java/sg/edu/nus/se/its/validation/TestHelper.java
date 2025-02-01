package sg.edu.nus.se.its.validation;

import java.io.File;
import java.io.IOException;

import org.javatuples.Pair;

import sg.edu.nus.se.its.model.Program;
import sg.edu.nus.se.its.parser.ParserServiceImpl;


public class TestHelper {
    private static final ParserServiceImpl parserService = new ParserServiceImpl();
    private static final String FILE_PATHS = System.getProperty("user.dir") + "/../common-tests/basic_test/programs/";

    /**
     * Parses a program from a local file.
     *
     * @param fileName the name of the file
     * @return the parsed program
     */
    public static Program parseLocalProgramFile(String fileName) {
        File file = new File(FILE_PATHS + fileName);
        Program program = null;
        try {
            program = parserService.parse(file);
        } catch (IOException e) {
            System.err.println("Unexpected exception during service call." + e);
        }

        return program;
    }

    /**
     * Runs a test with the given file names.
     *
     * @param fileName0 the name of the first file
     * @param fileName1 the name of the second file
     * @return true if the test passes, false otherwise
     */
    public static boolean runTest(String fileName0, String fileName1) {
        PatchValidator validator = new PatchValidator();
        Program referenceSolution = TestHelper.parseLocalProgramFile(fileName0);
        Program submittedProgram = TestHelper.parseLocalProgramFile(fileName1);
        return validator.patchValidation(referenceSolution, submittedProgram);
    }

    /**
     * Runs a test with the given file names, checking both ways.
     *
     * @param fileName0 the name of the first file
     * @param fileName1 the name of the second file
     * @return true if the test passes, false otherwise
     */
    public static boolean runTestReflexive(String fileName0, String fileName1) {
        PatchValidator validator = new PatchValidator();
        Program referenceSolution = TestHelper.parseLocalProgramFile(fileName0);
        Program submittedProgram = TestHelper.parseLocalProgramFile(fileName1);
        return (validator.patchValidation(referenceSolution, submittedProgram) &&
            validator.patchValidation(submittedProgram, referenceSolution));
    }

    /**
     * Runs a test with the given file names and return the result and counter example.
     *
     * @param fileName0 the name of the first file
     * @param fileName1 the name of the second file
     * @return a pair containing the result and counter example
     */
    public static Pair<Boolean, String> runTestWithCounterExample(String fileName0, String fileName1) {
        PatchValidator validator = new PatchValidator();
        Program referenceSolution = TestHelper.parseLocalProgramFile(fileName0);
        Program submittedProgram = TestHelper.parseLocalProgramFile(fileName1);
        Boolean result = validator.patchValidation(referenceSolution, submittedProgram);
        String counterExample = validator.getCounterExample();
        return new Pair<>(result, counterExample);
    }
}
