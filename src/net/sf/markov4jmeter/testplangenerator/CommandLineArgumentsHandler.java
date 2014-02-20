package net.sf.markov4jmeter.testplangenerator;


import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dynamod.aspectlegacy.util.CmdlOptionFactory;
import dynamod.aspectlegacy.util.CmdlOptionsReader;

/**
 * This class defines the command line options accepted by the Test Plan
 * Generator. Each option is initiated by a leading hyphen; an overview is
 * given below, followed by several examples.
 *
 * <p>Available command line options:
 * <table border="1">
 *   <tr><th>Long                      </th>
 *       <th> Short                    </th>
 *       <th> Description              </th>
 *
 *   <tr><td><code> input    </code></td>
 *       <td><code> i          </code></td>
 *       <td> XMI input file containing a workload model in M4J-DSL, e.g.
 *       "WorkloadModel.xmi" </td>
 *
 *   <tr><td><code> output     </code></td>
 *       <td><code> o          </code></td>
 *       <td> Output file which will contain the result Test Plan; note that
 *            the suffix ".jmx" indicates a JMeter Test Plan file."
 *       </td>
 *
 *   <tr><td><code> properties </code></td>
 *       <td><code> p          </code></td>
 *       <td> Properties file containing customized Test Plan properties, in
 *            particular the initial values of the Test Plan elements to be
 *            created. </td>
 *
 *   <tr><td><code> genProperties </code></td>
 *       <td><code> g          </code></td>
 *       <td> (Optional) properties file containing customized generator
 *            properties, in particular the locality or other settings for the
 *            JMeter engine; the default settings should be sufficient, so that
 *            this option does not need to be used in general. </td>
 *
 *   <tr><td><code> filters    </code></td>
 *       <td><code> f          </code></td>
 *       <td> (Optional) filters to be used; filters are passed as a sequence of
 *            their short names, in an arbitrary combination and order. </td>
 * </table>
 *
 * <p>Examples:
 * <ul>
 *   <li>The options sequence
 *   <blockquote>
 *     <code>-i WorkloadModel.xmi -o testplan.jmx -p testplan.properties</code>
 *   </blockquote>
 *   defines the files "WorkloadModel.xmi" and "testplan.jmx" to be used as
 *   input file and output file respectively, and it directs the generator to
 *   include the properties which are provided by the file
 *   "generator.properties"; no filters will be used.
 *   </li>
 *
 *   <li>The options sequence
 *   <blockquote>
 *     <code>-i WorkloadModel.xmi -o testplan.jmx -p testplan.properties
 *     -f CG</code>
 *   </blockquote>
 *   has the same effect as the first one, but it additionally defines a
 *   sequence of two filters, associated with the arguments "C" and "G"
 *   respectively, to be applied on the Test Plan finally.
 *   </li>
 * </ul>
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class CommandLineArgumentsHandler {

    /** Command-line options to be parsed. */
    private static Options OPTIONS;

    /** Name of the input File which provides a workload model in M4J-DSL to be
     *  transformed. */
    private final static Option INPUT_FILE =
            CmdlOptionFactory.createOption(
                    "i",                                   // opt;
                    "input",                               // longOpt;
                    "XMI input file containing a "         // description;
                    + "workload model defined in M4J-DSL.",
                    true,                                  // !isRequired;
                    "WorkloadModel.xmi",                   // argName;
                    false);                                // !hasOptionalArg;

    /** Name of the output file which will contain the result Test Plan. */
    private final static Option OUTPUT_FILE =
            CmdlOptionFactory.createOption(
                    "o",                                   // opt;
                    "output",                              // longOpt;
                    "Output file which will contain the "  // description;
                    + "result Test Plan.",
                    true,                                  // !isRequired;
                    "testplan.jmx",                        // argName;
                    false);                                // !hasOptionalArg;

    /** Name of the (optional) configuration file which provides default
     *  properties for the Test Plan factory. */
    private final static Option TEST_PLAN_PROPERTIES_FILE =
            CmdlOptionFactory.createOption(
                    "p",                                    // opt;
                    "properties",                           // longOpt;
                    "(Optional) configuration file for "    // description;
                    + "the Test Plan factory, providing default properties.",
                    true,                                   // !isRequired;
                    "testplan.properties",                  // argName;
                    false);                                 // !hasOptionalArg;

    /** Name of the (optional) configuration file which provides default
     *  properties for the generator. */
    private final static Option GENERATOR_PROPERTIES_FILE =
            CmdlOptionFactory.createOption(
                    "g",                                    // opt;
                    "genProperties",                        // longOpt;
                    "Configuration file for the "           // description;
                    + "generator, providing default properties.",
                    false,                                   // !isRequired;
                    "generator.properties",                 // argName;
                    false);                                 // !hasOptionalArg;

    /** Filters to be used; this is a sequence of flags in arbitrary
     *  combination and order. */
    // TODO: Description of filters;
    // TODO: Filter for workload intensity must be passed exclusively.
    private final static Option FILTERS =
            CmdlOptionFactory.createOption(
                    "f",                                    // opt;
                    "filters",                              // longOpt;
                    "(Optional) filters.",                  // description;
                    false,                                   // !isRequired;
                    "(C|G)*",                               // argName;
                    false);                                 // !hasOptionalArg;

    /** Formatter for printing the usage instructions. */
    private final static HelpFormatter HELP_FORMATTER = new HelpFormatter();

    /** Basic parser for extracting values from command-line input. */
    private final static CommandLineParser PARSER = new BasicParser();

    static {

        // fill the options container;
        CommandLineArgumentsHandler.OPTIONS = new Options();

        CommandLineArgumentsHandler.OPTIONS.addOption(
                CommandLineArgumentsHandler.INPUT_FILE);

        CommandLineArgumentsHandler.OPTIONS.addOption(
                CommandLineArgumentsHandler.OUTPUT_FILE);

        CommandLineArgumentsHandler.OPTIONS.addOption(
                CommandLineArgumentsHandler.GENERATOR_PROPERTIES_FILE);

        CommandLineArgumentsHandler.OPTIONS.addOption(
                CommandLineArgumentsHandler.TEST_PLAN_PROPERTIES_FILE);

        CommandLineArgumentsHandler.OPTIONS.addOption(
                CommandLineArgumentsHandler.FILTERS);
    }

    /** Input file path which has been read from command line. */
    private static String inputFile;

    /** Output file path which has been read from command line. */
    private static String outputFile;

    /** (Optional) generator properties file path which has been read from
     *  command line. */
    private static String generatorPropertiesFile;

    /** (Optional) Test Plan properties file path which has been read from
     *  command line. */
    private static String testPlanPropertiesFile;

    /** Filter flags which have been read from command line. */
    private static String filters;


    /**
     * Returns the input file path which has been read from command line.
     *
     * @return  A valid <code>String</code> which represents a file path.
     */
    public static String getInputFile() {

        return CommandLineArgumentsHandler.inputFile;
    }

    /**
     * Returns the output file path which has been read from command line.
     *
     * @return  A valid <code>String</code> which represents a file path.
     */
    public static String getOutputFile() {

        return CommandLineArgumentsHandler.outputFile;
    }

    /**
     * Returns the (optional) generator properties file path which has been read
     * from command line.
     *
     * @return
     *     A valid <code>String</code> which represents a file path, or
     *     <code>null</code> if no file path has been read.
     */
    public static String getGeneratorPropertiesFile() {

        return CommandLineArgumentsHandler.generatorPropertiesFile;
    }

    /**
     * Returns the (optional) Test Plan properties file path which has been read
     * from command line
     *
     * @return
     *     A valid <code>String</code> which represents a file path, or
     *     <code>null</code> if no file path has been read.
     */
    public static String getTestPlanPropertiesFile() {

        return CommandLineArgumentsHandler.testPlanPropertiesFile;
    }

    /**
     * Returns the filter flags which have been read from command line.
     *
     * @return
     *     A valid <code>String</code>, or <code>null</code> if no filter flags
     *     have been read.
     */
    public static String getFilters() {

        return CommandLineArgumentsHandler.filters;
    }

    /**
     * Prints the usage instructions to standard output.
     */
    public static void printUsage () {

        CommandLineArgumentsHandler.HELP_FORMATTER.printHelp(
            TestPlanGenerator.class.getSimpleName(),
            CommandLineArgumentsHandler.OPTIONS);
    }

    // TODO: revise comments!
    /**
     * Initializes the handler by parsing the given array of arguments; the
     * parsed values might be requested through the <code>get()</code> methods
     * of this class.
     *
     * @param args
     *     Sequence of <code>String</code>s to be parsed; might comply with
     *     the arguments which have been passed to the <code>main()</code>
     *     method of the application.
     *
     * @throws ParseException
     *     if the given arguments do not match the set of options which
     *     is predefined by this class.
     * @throws NullPointerException
     *     if <code>null</code> has been passed as a parameter, or if the value
     *     of any specified option is undefined (<code>null</code>).
     * @throws IllegalArgumentException
     *     if the value of any option is an empty <code>String</code>
     *     (<code>""</code>).
     */
    public static void init (final String[] args)
        throws ParseException, NullPointerException, IllegalArgumentException {

        // might throw a ParseException;
        final CommandLine commandLine =
                CommandLineArgumentsHandler.parseCommands(args);

        // build an instance for reading "typed" options from command-line;
        final CmdlOptionsReader cmdlOptionsReader =
                new CmdlOptionsReader(commandLine);

        CommandLineArgumentsHandler.inputFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        cmdlOptionsReader,
                        CommandLineArgumentsHandler.INPUT_FILE);

        CommandLineArgumentsHandler.outputFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        cmdlOptionsReader,
                        CommandLineArgumentsHandler.OUTPUT_FILE);

        CommandLineArgumentsHandler.generatorPropertiesFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        cmdlOptionsReader,
                        CommandLineArgumentsHandler.GENERATOR_PROPERTIES_FILE);

        CommandLineArgumentsHandler.testPlanPropertiesFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        cmdlOptionsReader,
                        CommandLineArgumentsHandler.TEST_PLAN_PROPERTIES_FILE);

        CommandLineArgumentsHandler.filters =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        cmdlOptionsReader,
                        CommandLineArgumentsHandler.FILTERS);
    }

    private static String readOptionValueAsString (
            final CmdlOptionsReader cmdlOptionsReader,
            final Option option)
                    throws NullPointerException, IllegalArgumentException {

        String value;  // to be returned;

        final String opt = option.getOpt();

        if ( option.isRequired() ) {

            // might throw a NullPointer- or IllegalArgumentException;
            value = cmdlOptionsReader.readOptionValueAsString(opt);

        } else {

            try {

                // might throw a NullPointer- or IllegalArgumentException;
                value = cmdlOptionsReader.readOptionValueAsString(opt);

            } catch (final Exception ex) {

                value = null;  // accept undefined value for optional option;
            }
        }

        return value;
    }

    /**
     * Parses the given user-input and build an instance of {@link CommandLine}.
     *
     * @param args
     *     User-input as it might have been passed to a <code>main()</code>
     *     method before.
     *
     * @return
     *     An instance of {@link CommandLine} to be used for requesting any
     *     input values.
     *
     * @throws Exception
     *     In case the given arguments do not match the set of options which
     *     is predefined by this class.
     */
    private static CommandLine parseCommands (final String[] args)
            throws ParseException {

        // might throw a ParseException; returns a CommandLine, if successful;
        return CommandLineArgumentsHandler.PARSER.parse(
                CommandLineArgumentsHandler.OPTIONS, args);
    }
}