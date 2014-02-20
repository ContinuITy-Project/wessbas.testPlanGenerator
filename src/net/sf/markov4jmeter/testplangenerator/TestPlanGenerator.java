package net.sf.markov4jmeter.testplangenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import m4jdsl.WorkloadModel;
import m4jdsl.impl.M4jdslPackageImpl;
import net.sf.markov4jmeter.testplangenerator.transformation.AbstractTestPlanTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.HTTPRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.SimpleTestPlanTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.AbstractFilter;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.ConstantWorkloadIntensityFilter;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.HeaderDefaultsFilter;
import net.sf.markov4jmeter.testplangenerator.util.Configuration;
import net.sf.markov4jmeter.testplangenerator.util.EcoreObjectValidator;
import net.sf.markov4jmeter.testplangenerator.util.XmiEcoreHandler;

import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Generator class for building Test Plans which result from M4J-DSL models.
 *
 * <p>The generator must be initialized before it can be used properly;
 * hence, the {@link #init(String)} method must be called once for initializing
 * the default configuration of Test Plan elements. The name of the
 * configuration properties file must be passed to the initialization method
 * therefore. The {@link #isInitialized()} method might be used for requesting
 * the initialization status of the generator.
 *
 * <p>An M4J-DSL model for which a Test Plan shall be generated, might be passed
 * to the regarding <code>generate()</code> method; the model might be even
 * loaded from an XMI file alternatively, requiring the related filename to be
 * passed to the regarding <code>generate()</code> method.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 * @since    1.7
 */
public class TestPlanGenerator {

    /** Property key for the JMeter home directory. */
    private final static String PKEY_JMETER__HOME = "jmeter_home";

    /** Property key for the JMeter default properties. */
    private final static String PKEY_JMETER__PROPERTIES = "jmeter_properties";

    /** Property key for the language tag which indicates the locality. */
    private final static String PKEY_JMETER__LANGUAGE_TAG = "jmeter_languageTag";

    /** Property key for the file with Test Plan default properties. */
    private final static String PKEY_TEST_PLAN__PROPERTIES =
            "testPlan_properties";

    /** Separator to be used by JMeter utilities for handling file paths
     *  properly. */
    // the JMeter properties loader, which refers to this separator, uses "/"
    // by default, so File.separatorChar should not be used here; otherwise
    // inconsistent file paths might occur, for example, on Windows systems;
    private final static String JMETER_PROPERTIES_FILE_SEPARATOR = "/";

    // info-, warn- and error-messages (should be self-explaining);

    private final static String ERROR_CONFIGURATION_UNDEFINED =
            "Configuration file is null.";

    private final static String ERROR_CONFIGURATION_NOT_FOUND =
            "Could not find configuration file \"%s\".";

    private final static String ERROR_CONFIGURATION_READING_FAILED =
            "Could not read configuration file \"%s\".";

    private final static String ERROR_JMETER_PROPERTIES_READING_FAILED =
            "Could not read JMeter properties file \"%s\": %s";

    private final static String ERROR_TEST_PLAN_PROPERTIES_UNDEFINED =
            "Test Plan properties file is null.";

    private final static String ERROR_TEST_PLAN_PROPERTIES_NOT_FOUND =
            "Could not find Test Plan properties file \"%s\".";

    private final static String ERROR_TEST_PLAN_PROPERTIES_READING_FAILED =
            "Could not read Test Plan properties file \"%s\".";

    private final static String ERROR_INITIALIZATION_FAILED =
            "Initialization of Test Plan Generator failed.";

    private final static String INFO_TEST_PLAN_GENERATION_STARTED =
            "Generating Test  Plan ...";

    private final static String INFO_TEST_PLAN_GENERATION_FINISHED =
            "Finished.";

    private final static String INFO_INITIALIZATION_SUCCESSFUL =
            "Test Plan Generator has been successfully initialized.";

    private final static String ERROR_INPUT_FILE_NOT_FOUND =
            "Could not open file \"%s\" for reading input data: %s";

    private final static String ERROR_OUTPUT_FILE_NOT_FOUND =
            "Could not open file \"%s\" for writing output data: %s";

    private final static String ERROR_OUTPUT_FILE_ACCESS_FAILED =
            "Could not access file \"%s\" for writing output data: %s";

    private final static String ERROR_TREE_SAVING_FAILED =
            "Could not save Test Plan tree \"%s\" via SaveService: %s";

    private final static String WARNING_OUTPUT_FILE_CLOSING_FAILED =
            "Could not close file-output stream for file \"%s\": %s";

    /** Handler for loading Ecore-models from XMI files. */
    private final XmiEcoreHandler xmiEcoreHandler;

    /** Log-factory for any warnings or error messages. */
    private final static Log LOG = LogFactory.getLog(TestPlanGenerator.class);


    /* *********************  global (non-final) fields  ******************** */


    /** Factory to be used for creating Test Plan elements. */
    private TestPlanElementFactory testPlanElementFactory;


    /* ***************************  constructors  *************************** */


    /**
     * Standard constructor of a Test Plan Generator, without parameters.
     */
    public TestPlanGenerator () {

        this.xmiEcoreHandler = new XmiEcoreHandler();
    }


    /* **************************  public methods  ************************** */


    /**
     * Returns the Test Plan Factory associated with the Test Plan Generator.
     *
     * @return
     *     A valid Test Plan Factory, if the Test Plan Generator has been
     *     initialized successfully; otherwise <code>null</code> will be
     *     returned.
     */
    public TestPlanElementFactory getTestPlanElementFactory () {

        return this.testPlanElementFactory;
    }

    /**
     * Returns the information whether the Test Plan Generator is initialized,
     * meaning that the {@link #init(String)} method has been called
     * successfully.
     *
     * @return
     *     <code>true</code> if and only if the Test Plan Generator is
     *     initialized.
     */
    public boolean isInitialized () {

        return this.testPlanElementFactory != null;
    }

    /**
     * Initializes the Test Plan Generator by loading the specified
     * configuration file and setting its properties accordingly.
     *
     * @param configurationFile
     *     Properties file which provides required or optional properties.
     */
    public void init (final String configurationFile) {

        // read the configuration and give an error message, if reading fails;
        // in that case, null will be returned;
        final Configuration configuration =
                this.readConfiguration(configurationFile);

        if (configuration != null) {  // could configuration be read?

            final String jMeterHome = configuration.getString(
                    TestPlanGenerator.PKEY_JMETER__HOME);

            final String jMeterProperties = configuration.getString(
                    TestPlanGenerator.PKEY_JMETER__PROPERTIES);

            final String jMeterLanguageTag = configuration.getString(
                    TestPlanGenerator.PKEY_JMETER__LANGUAGE_TAG);

            final Locale jMeterLocale =
                    Locale.forLanguageTag(jMeterLanguageTag);

            final String testPlanProperties = configuration.getString(
                    TestPlanGenerator.PKEY_TEST_PLAN__PROPERTIES);

            final boolean success =
                    this.initJMeter(jMeterHome, jMeterProperties, jMeterLocale);

            if (success) {

                // create a factory which builds Test Plan elements according
                // to the specified properties.
                this.testPlanElementFactory =
                        this.createTestPlanFactory(testPlanProperties);

                if (this.testPlanElementFactory != null) {

                    this.logInfo(
                            TestPlanGenerator.INFO_INITIALIZATION_SUCCESSFUL);

                    return;
                }
            }

        }

        TestPlanGenerator.LOG.error(
                TestPlanGenerator.ERROR_INITIALIZATION_FAILED);
    }

    /**
     * Generates a Test Plan for the given workload model and writes the result
     * into the specified file.
     *
     * @param workloadModel
     *     Workload model which provides the values for the Test Plan to be
     *     generated.
     * @param testPlanBuilder
     *     Builder to be used for building a Test Plan of certain structure.
     * @param filters
     *     (Optional) modification filters to be finally applied on the newly
     *     generated Test Plan.
     * @param outputFilename
     *     Name of the file where the Test Plan shall be stored in.
     */
    public void generate (
            final WorkloadModel workloadModel,
            final AbstractTestPlanTransformer testPlanBuilder,
            final AbstractFilter[] filters,
            final String outputFilename) {

        this.logInfo(TestPlanGenerator.INFO_TEST_PLAN_GENERATION_STARTED);

        // TODO: log-messages for validation
        final EcoreObjectValidator validator = new EcoreObjectValidator();
        final boolean success = validator.validateAndPrintResult(workloadModel);

        if (!success) {

            System.out.println("Generation process failed through model errors.");
            System.exit(0);
        }

        final ListedHashTree testPlanTree = testPlanBuilder.transform(
                workloadModel,
                this.testPlanElementFactory,
                filters);

        this.writeOutput(testPlanTree, outputFilename);

        this.logInfo(TestPlanGenerator.INFO_TEST_PLAN_GENERATION_FINISHED);
    }

    /**
     * Generates a Test Plan for the (Ecore) workload model which is stored in
     * the given XMI-file; the result will be written into the specified output
     * file.
     *
     * @param inputFile
     *     XMI file containing the (Ecore) workload model which provides the
     *     values for the Test Plan to be generated.
     * @param outputFile
     *     Name of the file where the Test Plan shall be stored in.
     * @param testPlanBuilder
     *     Builder to be used for building a Test Plan of certain structure.
     * @param filters
     *     (Optional) modification filters to be finally applied on the newly
     *     generated Test Plan.
     *
     * @throws IOException
     *     in case any file reading or writing operation failed.
     */
    public void generate (
            final String inputFile,
            final String outputFile,
            final AbstractTestPlanTransformer testPlanBuilder,
            final AbstractFilter[] filters) throws IOException {

        // initialize the model package;
        M4jdslPackageImpl.init();

        // might throw an IOException;
        final WorkloadModel workloadModel =
                (WorkloadModel) this.xmiEcoreHandler.xmiToEcore(inputFile, "xmi");

        this.generate(workloadModel, testPlanBuilder, filters, outputFile);
    }


    /* *************************  protected methods  ************************ */


    /**
     * Writes a given Test Plan to a specified output file.
     *
     * @param testPlanTree    Test Plan to be written into the output file.
     * @param outputFilename  Name of the output file.
     */
    protected void writeOutput (
            final ListedHashTree testPlanTree, final String outputFilename) {

        FileOutputStream fileOutputStream = null;

        try {

            // might throw a FileNotFoundException or SecurityException;
            fileOutputStream = new FileOutputStream(outputFilename);

            // might throw an IOException;
            SaveService.saveTree(testPlanTree, fileOutputStream);

        } catch (final FileNotFoundException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_OUTPUT_FILE_NOT_FOUND,
                    outputFilename,
                    ex.getMessage());

            TestPlanGenerator.LOG.error(message);

        } catch (final SecurityException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_OUTPUT_FILE_ACCESS_FAILED,
                    outputFilename,
                    ex.getMessage());

            TestPlanGenerator.LOG.error(message);

        } catch (final IOException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_TREE_SAVING_FAILED,
                    outputFilename,
                    ex.getMessage());

            TestPlanGenerator.LOG.error(message);

        } finally {

            if (fileOutputStream != null) {

                try {

                    fileOutputStream.close();

                } catch (final IOException ex) {

                    final String message = String.format(
                            TestPlanGenerator.WARNING_OUTPUT_FILE_CLOSING_FAILED,
                            outputFilename,
                            ex.getMessage());

                    TestPlanGenerator.LOG.warn(message);
                }
            }
        }
    }

    /**
     * Starts the JMeter engine for a given Test Plan.
     *
     * @param testPlan  Test Plan to be processed by the JMeter engine.
     */
    protected void startJMeterEngine (final HashTree testPlan) {

        // JMeter engine;
        final StandardJMeterEngine jmeterEngine = new StandardJMeterEngine();

        jmeterEngine.configure(testPlan);
        jmeterEngine.run();

        // TODO: capture results and return information;
    }


    /* **************************  private methods  ************************* */


    /**
     * Reads all configuration properties from the specified file and gives an
     * error message, if reading fails.
     *
     * @param propertiesFile  Properties file to be read.
     *
     * @return
     *     A valid configuration, if the specified properties file could be
     *     read successfully; otherwise <code>null</code> will be returned.
     */
    private Configuration readConfiguration (final String propertiesFile) {

        Configuration configuration = new Configuration();

        try {
            // might throw FileNotFound-, IO-, or NullPointerException;
            configuration.load(propertiesFile);

        } catch (final FileNotFoundException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_CONFIGURATION_NOT_FOUND,
                    propertiesFile);

            TestPlanGenerator.LOG.error(message);
            configuration = null;  // indicates an error;

        } catch (final IOException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_CONFIGURATION_READING_FAILED,
                    propertiesFile);

            TestPlanGenerator.LOG.error(message);
            configuration = null;  // indicates an error;

        } catch (final NullPointerException ex) {

            TestPlanGenerator.LOG.error(
                    TestPlanGenerator.ERROR_CONFIGURATION_UNDEFINED);

            configuration = null;  // indicates an error;
        }

        return configuration;
    }

    /**
     * Initializes the JMeter utilities by setting the required properties
     * in the {@link JMeterUtils} class.
     *
     * @param jMeterHome
     *     Home directory of JMeter which is the (local) installation directory
     *     of the tool.
     * @param jMeterPropertiesFile
     *     File which contains additional JMeter-specific properties.
     * @param locale
     *     The locality to be used.
     *
     * @return
     *     <code>true</code> if and only if the specified properties file could
     *     be read successfully.
     */
    private boolean initJMeter (
            final String jMeterHome,
            final String jMeterPropertiesFile,
            final Locale locale) {

        boolean success = true;  // to be returned;

        final String jMeterPropertiesFilePath =
                jMeterHome +
                TestPlanGenerator.JMETER_PROPERTIES_FILE_SEPARATOR +
                jMeterPropertiesFile;

        JMeterUtils.setJMeterHome(jMeterHome);
        JMeterUtils.setLocale(locale);

        try {

            // might throw (at least) a RunTimeException;
            JMeterUtils.loadJMeterProperties(jMeterPropertiesFilePath);

        } catch (final Exception ex) {

            final String message = String.format(

                    TestPlanGenerator.ERROR_JMETER_PROPERTIES_READING_FAILED,
                    jMeterPropertiesFilePath,
                    ex.getMessage());

            TestPlanGenerator.LOG.error(message);
            success = false;
        }

        return success;
    }

    /**
     * Creates a Factory which builds Test Plan according to the default
     * properties defined in the specified file.
     *
     * @param propertiesFile
     *     Properties file with default properties of Test Plan elements.
     *
     * @return
     *     A valid Test Plan Factory if the properties file could be read
     *     successfully; otherwise <code>null</code> will be returned.
     */
    private TestPlanElementFactory createTestPlanFactory (
            final String propertiesFile) {

        // to be returned;
        TestPlanElementFactory testPlanElementFactory = null;

        final Configuration testPlanProperties = new Configuration();

        try {

            // might throw FileNotFound-, IO-, or NullPointerException;
            testPlanProperties.load(propertiesFile);

            // store factory globally for regular and simplified access;
            testPlanElementFactory =
                    new TestPlanElementFactory(testPlanProperties, false);

        } catch (final FileNotFoundException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_TEST_PLAN_PROPERTIES_NOT_FOUND,
                    propertiesFile);

            TestPlanGenerator.LOG.error(message);

        } catch (final IOException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_TEST_PLAN_PROPERTIES_READING_FAILED,
                    propertiesFile);

            TestPlanGenerator.LOG.error(message);

        } catch (final NullPointerException ex) {

            TestPlanGenerator.LOG.error(
                    TestPlanGenerator.ERROR_TEST_PLAN_PROPERTIES_UNDEFINED);
        }

        return testPlanElementFactory;
    }

    /**
     * Logs an information to standard output.
     *
     * @param message  Information to be logged.
     */
    private void logInfo (final String message) {

        // TODO: remove print-command, solve the issue below;
        System.out.println(message);

        // this command would print in red color, indicating an error:
        //
        //   TestPlanGenerator.LOG.info(message);
        //
        // --> adjust "commons-logging.properties" accordingly;
    }



    /**
     * Main method which parses the command line parameters and
     *
     * @param argv Argument vector.
     */
    public static void main (final String[] argv) {

        try {

            // initialize arguments handler for requesting the command line
            // values afterwards via get() methods; might throw a
            // NullPointer-, IllegalArgument- or ParseException;
            CommandLineArgumentsHandler.init(argv);

            TestPlanGenerator.generate();

        } catch (final NullPointerException
                | IllegalArgumentException
                | ParseException ex) {

            // TODO: this is just for testing purposes!
//            TestPlanGenerator.LOG.error(ex.getMessage());
//            ex.printStackTrace();

            CommandLineArgumentsHandler.printUsage();
        }
    }

    /**
     * Starts the generation process with the arguments which have been passed
     * to command line.
     */
    private static void generate () {

        final TestPlanGenerator testPlanGenerator = new TestPlanGenerator();

        String inputFile =
                CommandLineArgumentsHandler.getInputFile();

        String outputFile =
                CommandLineArgumentsHandler.getOutputFile();

        String generatorPropertiesFile =
                CommandLineArgumentsHandler.getGeneratorPropertiesFile();

        String testPlanPropertiesFile =
                CommandLineArgumentsHandler.getTestPlanPropertiesFile();

        String filters =
                CommandLineArgumentsHandler.getFilters();

        testPlanGenerator.generate(
                inputFile,
                outputFile,
                generatorPropertiesFile,
                testPlanPropertiesFile,
                filters);
    }

    /**
     * Starts the ...
     * TODO: optional generatorPropertiesFile and filters are not supported yet;
     *
     * @param inputFile
     * @param outputFile
     * @param generatorPropertiesFile
     * @param testPlanPropertiesFile
     * @param filters
     */
    public void generate (
            final String inputFile,
            final String outputFile,
            String generatorPropertiesFile,  // TODO: should be final, after open issues have been fixed;
            final String testPlanPropertiesFile,
            final String filterFlags) {

        final TestPlanGenerator testPlanGenerator = new TestPlanGenerator();

        // TODO: collect these filters according to the command line flags;
        generatorPropertiesFile = "configuration/generator.default.properties";
        final AbstractFilter[] filters = new AbstractFilter[]{

                new ConstantWorkloadIntensityFilter(),
/*
                new GaussianThinkTimeDistributionFilter(  // TODO: just for testing, these values must be taken from workload model;
                        "Think Time",
                        "",
                        true,
                        300.0d,
                        100.0d),
*/
                new HeaderDefaultsFilter()
        };

        testPlanGenerator.init(generatorPropertiesFile);

        if (testPlanGenerator.isInitialized()) {

            try {

                testPlanGenerator.generate(
                        inputFile,
                        outputFile,
                        new SimpleTestPlanTransformer(new HTTPRequestTransformer()),
                        filters);

            } catch (final IOException ex) {
ex.printStackTrace();
                final String message = String.format(
                        TestPlanGenerator.ERROR_INPUT_FILE_NOT_FOUND,
                        inputFile,
                        ex.getMessage());

                TestPlanGenerator.LOG.error(message);
            }

            // TODO: invoke JMeter engine optionally;
        }
    }
    /*
    // TODO: include these elements!
    final Arguments arguments = testPlanElementFactory.createArguments();
    final LoopController loopController = testPlanElementFactory.createLoopController();
    final WhileController whileController = testPlanElementFactory.createWhileController();
    final CounterConfig counterConfig = testPlanElementFactory.createCounterConfig();
     */

}
