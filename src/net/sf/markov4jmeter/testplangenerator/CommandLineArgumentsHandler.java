/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/


package net.sf.markov4jmeter.testplangenerator;


import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class defines the command-line options accepted by the Test Plan
 * Generator. Each option is initiated by a leading hyphen; an overview is
 * given below, followed by several examples.
 *
 * <p>Available command-line options:
 * <table border="1">
 *   <tr><th>Long         </th>
 *       <th> Short       </th>
 *       <th> Description </th>
 *
 *   <tr><td><code> input </code></td>
 *       <td><code> i     </code></td>
 *       <td> XMI input file which provides the M4J-DSL workload model to be
 *       transformed into a JMeter Test Plan, e.g., "WorkloadModel.xmi". </td>
 *
 *   <tr><td><code> output </code></td>
 *       <td><code> o      </code></td>
 *       <td> Output file of the JMeter Test Plan; the suffix ".jmx" indicates
 *       a JMeter Test Plan file, e.g., "testplan.jmx".
 *       </td>
 *
 *   <tr><td><code> testplanproperties </code></td>
 *       <td><code> t                  </code></td>
 *       <td> Properties file which provides the default values of the
 *       Test Plan elements. </td>
 *
 *   <tr><td colspan="3" align="center">
 *       <i>Optional Arguments</i>
 *       </td></tr>
 *
 *   <tr><td><code> linebreak </code></td>
 *       <td><code> l         </code></td>
 *       <td> (Optional) OS-specific line-break for being used in the CSV files
 *       of the Behavior Models (0 = Windows, 1 = Unix, 2 = MacOS); the default
 *       value is 0 (Windows).
 *       </td>
 *
 *   <tr><td><code> path </code></td>
 *       <td><code> p    </code></td>
 *       <td> (Optional) path to an existing destination directory for the
 *       Behavior Model files to be written into; the default value is "./"
 *       (current directory).
 *       </td>
 *
 *   <tr><td><code> generatorproperties </code></td>
 *       <td><code> g                   </code></td>
 *       <td> (Optional) properties file which provides the configuration values
 *       of the Test Plan Generator, in particular the locality and further
 *       settings for the JMeter engine; the default settings should be
 *       sufficient, so that this option does not need to be used in general.
 *       </td>
 *
 *   <tr><td><code> filters    </code></td>
 *       <td><code> f          </code></td>
 *       <td> (Optional) filters for being applied to the resulting Test Plan
 *            after the transformation process; filters must be passed as a
 *            sequence of their short names, in an arbitrary combination and
 *            order. </td>
 *
 *   <tr><td><code> runtest </code></td>
 *       <td><code> r       </code></td>
 *       <td> (Optional) immediate start of the JMeter engine for running a
 *       test with the resulting Test Plan.
 *       </td>
 * </table>
 *
 * <p>Examples:
 * <ul>
 *   <li>The options sequence
 *   <blockquote>
 *     <code>-i WorkloadModel.xmi -o testplan.jmx -t testplan.properties</code>
 *   </blockquote>
 *   denotes a minimum start configuration for the Test Plan Generator, since
 *   it defines the files "WorkloadModel.xmi" and "testplan.jmx" to be used as
 *   input file and output file respectively, and it directs the generator to
 *   use the default values provided by file "testplan.properties" for Test Plan
 *   elements.
 *   </li>
 *
 *   <li>The options sequence
 *   <blockquote>
 *     <code>-i WorkloadModel.xmi -o testplan.jmx -t testplan.properties
 *     -l 2</code>
 *   </blockquote>
 *   has the same effect as the first one, but it additionally defines a
 *   MacOS-specific line-break type to be used for the CSV files of the
 *   Behavior Models.
 *
 *   <li>The options sequence
 *   <blockquote>
 *     <code>-i WorkloadModel.xmi -o testplan.jmx -t testplan.properties
 *     -l 2 -g generator.properties -r</code>
 *   </blockquote>
 *   has the same effect as the second one, but it additionally passes a custom
 *   configuration file for the generator and directs the generator to start
 *   the JMeter engine finally, for running a test with the resulting Test Plan.
 *   </li>
 * </ul>
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class CommandLineArgumentsHandler {

    /** Name of the XMI input file which provides the M4J-DSL workload model to
     *  be transformed into a JMeter Test Plan. */
    private final static Option INPUT_FILE =
            CmdlOptionFactory.createOption(
                    "i",                                   // opt;
                    "input",                               // longOpt;
                    "XMI input file which provides the "   // description;
                    + "M4J-DSL workload model to be transformed into a JMeter Test Plan.",
                    true,                                  // isRequired;
                    "WorkloadModel.xmi",                   // argName;
                    false);                                // !hasOptionalArg;

    /** Name of the JMeter Test Plan output file. */
    private final static Option OUTPUT_FILE =
            CmdlOptionFactory.createOption(
                    "o",                                   // opt;
                    "output",                              // longOpt;
                    "Output file of the JMeter Test "      // description;
                    + "Plan; the suffix \".jmx\" indicates a JMeter Test Plan file.",
                    true,                                  // isRequired;
                    "testplan.jmx",                        // argName;
                    false);                                // !hasOptionalArg;

    /** Name of the properties file which provides the default values of the
     *  Test Plan elements. */
    private final static Option TEST_PLAN_PROPERTIES_FILE =
            CmdlOptionFactory.createOption(
                    "t",                                    // opt;
                    "testplanproperties",                   // longOpt;
                    "Properties file which provides the "   // description;
                    + "default values of the Test Plan elements.",
                    true,                                   // isRequired;
                    "testplan.properties",                  // argName;
                    false);                                 // !hasOptionalArg;

    /** (Optional) OS-specific line-break for being used in the CSV files for
     *  Behavior Models (0 = Windows, 1 = Unix, 2 = MacOS); the default value
     *  is 0 (Windows). */
    private final static Option LINE_BREAK_TYPE =
            CmdlOptionFactory.createOption(
                    "l",                                    // opt;
                    "linebreak",                            // longOpt;
                    "(Optional) OS-specific line-break "    // description;
                    + "for being used in the CSV files of the Behavior Models (0 = Windows, 1 = Unix, 2 = MacOS); the default value is 0 (Windows).",
                    false,                                  // !isRequired;
                    "0",                                    // argName;
                    false);                                 // !isRequired;

    /** (Optional) path to an existing destination directory for the Behavior
     *  Model files to be written into; the default value is "./" (current
     *  directory). */
    private final static Option OUTPUT_PATH =
            CmdlOptionFactory.createOption(
                    "p",                                    // opt;
                    "path",                                 // longOpt;
                    "(Optional) path to an existing "       // description;
                    + "destination directory for the Behavior Model files to be written into; the default value is \"./\" (current directory).",
                    false,                                  // !isRequired;
                    "./",                                   // argName;
                    false);                                 // !hasOptionalArg;

    /** (Optional) properties file which provides the configuration values of
     *  the Test Plan Generator, in particular the locality and further settings
     *  for the JMeter engine. */
    private final static Option GENERATOR_PROPERTIES_FILE =
            CmdlOptionFactory.createOption(
                    "g",                                    // opt;
                    "generatorproperties",                  // longOpt;
                    "(Optional) properties file which "     // description;
                    + "provides the configuration values of the Test Plan generator, in particular the locality or further settings for the JMeter engine.",
                    false,                                  // !isRequired;
                    "generator.properties",                 // argName;
                    false);                                 // !hasOptionalArg;

    /** Filters to be used; this is a sequence of flags in arbitrary
     *  combination and order. */
    // TODO: implementation and description of filters;
    private final static Option FILTERS =
            CmdlOptionFactory.createOption(
                    "f",                                    // opt;
                    "filters",                              // longOpt;
                    "(Optional) filters for being "         // description;
                    + "applied to the resulting Test Plan after the transformation process; filters must be passed as a sequence of their short names, in an arbitrary combination and order.",
                    false,                                  // !isRequired;
                    "H",                                    // argName;
                    false);                                 // !hasOptionalArg;

    private final static Option START_TEST =
            CmdlOptionFactory.createOption(
                    "r",                                     // opt;
                    "runtest",                               // longOpt;
                    "(Optional) immediate start of the "     // description;
                    + "JMeter engine for running a test with the resulting Test Plan.",
                    false);                                  // !isRequired;

    /** Formatter for printing the usage instructions. */
    private final static HelpFormatter HELP_FORMATTER = new HelpFormatter();

    /** Basic parser for extracting values from command-line input. */
    private final static CommandLineParser PARSER = new BasicParser();


    /* *********************  global (non-final) fields  ******************** */


    /** Input file path which has been read from command-line. */
    private static String inputFile;

    /** Output file path which has been read from command-line. */
    private static String outputFile;

    /** Test Plan properties file path which has been read from command-line. */
    private static String testPlanPropertiesFile;

    /** (Optional) line-break type which has been read from command-line. */
    private static int lineBreakType;

    /** (Optional) output path which has been read from command-line. */
    private static String outputPath;

    /** (Optional) generator properties file path which has been read from
     *  command-line. */
    private static String generatorPropertiesFile;

    /** Filter flags which have been read from command-line. */
    private static String filters;

    /** Flag indicating whether a test run shall be started immediately. */
    private static boolean runTest;

    /** Command-line options to be parsed. */
    private static Options options;


    /* ***************************  static blocks  ************************** */


    static {

        // fill the options container;
        CommandLineArgumentsHandler.options = new Options();

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.INPUT_FILE);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.OUTPUT_FILE);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.TEST_PLAN_PROPERTIES_FILE);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.LINE_BREAK_TYPE);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.OUTPUT_PATH);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.GENERATOR_PROPERTIES_FILE);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.FILTERS);

        CommandLineArgumentsHandler.options.addOption(
                CommandLineArgumentsHandler.START_TEST);
    }


    /* **************************  public methods  ************************** */


    /**
     * Returns the input file path which has been read from command-line.
     *
     * @return  a valid <code>String</code> which represents a file path.
     */
    public static String getInputFile () {

        return CommandLineArgumentsHandler.inputFile;
    }

    /**
     * Returns the output file path which has been read from command-line.
     *
     * @return  a valid <code>String</code> which represents a file path.
     */
    public static String getOutputFile () {

        return CommandLineArgumentsHandler.outputFile;
    }

    /**
     * Returns the Test Plan properties file path which has been read from
     * command-line.
     *
     * @return
     *     a valid <code>String</code> which represents a file path, or
     *     <code>null</code> if no file path has been read.
     */
    public static String getTestPlanPropertiesFile () {

        return CommandLineArgumentsHandler.testPlanPropertiesFile;
    }

    /**
     * Returns the (optional) line-break value which has been read from
     * command-line.
     *
     * @return  an integer value which represents the line-break type.
     */
    public static int getLineBreakType () {

        return CommandLineArgumentsHandler.lineBreakType;
    }

    /**
     * Returns the (optional) output path which has been read from command-line.
     *
     * @return
     *     a <code>String</code> which denotes the location of an (existing)
     *     directory.
     */
    public static String getPath () {

        return CommandLineArgumentsHandler.outputPath;
    }

    /**
     * Returns the (optional) generator properties file path which has been read
     * from command-line.
     *
     * @return
     *     a valid <code>String</code> which represents a file path, or
     *     <code>null</code> if no file path has been read.
     */
    public static String getGeneratorPropertiesFile () {

        return CommandLineArgumentsHandler.generatorPropertiesFile;
    }

    /**
     * Returns the filter flags which have been read from command-line.
     *
     * @return
     *     a valid <code>String</code>, or <code>null</code> if no filter flags
     *     have been read.
     */
    public static String getFilters () {

        return CommandLineArgumentsHandler.filters;
    }

    /**
     * Returns the information whether a test run shall be started immediately.
     *
     * @return
     *     <code>true</code> if and only if a test run shall be started
     *     immediately after Test Plan generation.
     */
    public static boolean getRunTest () {

        return CommandLineArgumentsHandler.runTest;
    }

    /**
     * Prints the usage instructions to standard output.
     */
    public static void printUsage () {

        CommandLineArgumentsHandler.HELP_FORMATTER.printHelp(
            TestPlanGenerator.class.getSimpleName(),
            CommandLineArgumentsHandler.options);
    }

    /**
     * Initializes the handler by parsing the given array of arguments; the
     * parsed values might be requested through the <code>get()</code> methods
     * of this class.
     *
     * @param args
     *     sequence of <code>String</code>s to be parsed; might comply with
     *     the arguments which have been passed to the <code>main()</code>
     *     method of the application.
     *
     * @throws ParseException
     *     if the given arguments do not match the set of options which
     *     is predefined by this class.
     * @throws NullPointerException
     *     if <code>null</code> has been passed as a parameter, or if the value
     *     of any required option is undefined (<code>null</code>).
     * @throws IllegalArgumentException
     *     if an option flag denotes an empty <code>String</code>
     *     (<code>""</code>).
     */
    public static void init (final String[] args)
        throws ParseException, NullPointerException, IllegalArgumentException {

        // might throw a ParseException;
        final CommandLine commandLine =
                CommandLineArgumentsHandler.parseCommands(args);

        CommandLineArgumentsHandler.inputFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        commandLine,
                        CommandLineArgumentsHandler.INPUT_FILE);

        CommandLineArgumentsHandler.outputFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        commandLine,
                        CommandLineArgumentsHandler.OUTPUT_FILE);

        CommandLineArgumentsHandler.testPlanPropertiesFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        commandLine,
                        CommandLineArgumentsHandler.TEST_PLAN_PROPERTIES_FILE);

        CommandLineArgumentsHandler.lineBreakType =
                CommandLineArgumentsHandler.readOptionValueAsInt(
                        commandLine,
                        CommandLineArgumentsHandler.LINE_BREAK_TYPE);

        CommandLineArgumentsHandler.outputPath =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        commandLine,
                        CommandLineArgumentsHandler.OUTPUT_PATH);

        CommandLineArgumentsHandler.generatorPropertiesFile =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        commandLine,
                        CommandLineArgumentsHandler.GENERATOR_PROPERTIES_FILE);

        CommandLineArgumentsHandler.filters =
                CommandLineArgumentsHandler.readOptionValueAsString(
                        commandLine,
                        CommandLineArgumentsHandler.FILTERS);

        CommandLineArgumentsHandler.runTest = commandLine.hasOption(
                CommandLineArgumentsHandler.START_TEST.getOpt());
    }


    /* **************************  private methods  ************************* */


    /**
     * Reads the value for a given option from the specified command-line as
     * <code>String</code>.
     *
     * @param commandLine  command-line which provides the values.
     * @param option       option whose value shall be read from command-line.
     *
     * @return
     *     a valid <code>String</code>, or <code>null</code> if the option's
     *     value is optional and undefined.
     *
     * @throws NullPointerException
     *     in case the value is required, but could not be read as
     *     <code>String</code>.
     * @throws IllegalArgumentException
     *     if an option flag denotes an empty <code>String</code>
     *     (<code>""</code>).
     */
    private static String readOptionValueAsString (
            final CommandLine commandLine,
            final Option option)
                    throws NullPointerException, IllegalArgumentException {

        String value;  // to be returned;

        final String opt = option.getOpt();

        // build an instance for reading "typed" options from command-line;
        final CmdlOptionsReader cmdlOptionsReader =
                new CmdlOptionsReader(commandLine);

        try {

            // might throw a NullPointer- or IllegalArgumentException;
            value = cmdlOptionsReader.readOptionValueAsString(opt);

        } catch (final Exception ex) {

            if ( option.isRequired() ) {

                throw ex;

            } else {

                value = null;  // accept undefined value for optional option;
            }
        }

        return value;
    }

    /**
     * Reads the value for a given option from the specified command-line as
     * <code>int</code>.
     *
     * @param commandLine  command-line which provides the values.
     * @param option       option whose value shall be read from command-line.
     *
     * @return
     *     an <code>int</code> value which is 0, if the option's value is
     *     optional and undefined.
     *
     * @throws NullPointerException
     *     in case the value is required, but could not be read as
     *     <code>int</code>.
     * @throws NumberFormatException
     *     if the parsed value does not denote an <code>int</code> value.
     */
    private static int readOptionValueAsInt (
            final CommandLine commandLine,
            final Option option)
                    throws NullPointerException, NumberFormatException {

        int value;  // to be returned;

        final String opt = option.getOpt();

        // build an instance for reading "typed" options from command-line;
        final CmdlOptionsReader cmdlOptionsReader =
                new CmdlOptionsReader(commandLine);

        try {

            // might throw a NullPointer- or NumberFormatException;
            value = cmdlOptionsReader.readOptionValueAsInt(opt);

        } catch (final Exception ex) {

            if ( option.isRequired() ) {

                throw ex;

            } else {

                value = 0;  // accept undefined value for optional option;
            }
        }

        return value;
    }

    /**
     * Parses the given user input and builds an instance of
     * {@link CommandLine}.
     *
     * @param args
     *     user input as it might have been passed to the <code>main()</code>
     *     method of the application before.
     *
     * @return
     *     an instance of {@link CommandLine} to be used for requesting any
     *     input values.
     *
     * @throws ParseException
     *     in case the given arguments do not match the predefined set of
     *     options.
     */
    private static CommandLine parseCommands (final String[] args)
            throws ParseException {

        // might throw a ParseException; returns a CommandLine, if successful;
        return CommandLineArgumentsHandler.PARSER.parse(
                CommandLineArgumentsHandler.options, args);
    }
}
