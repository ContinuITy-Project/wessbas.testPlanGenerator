package net.sf.markov4jmeter.testplangenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import m4jdsl.WorkloadModel;
import m4jdsl.impl.M4jdslPackageImpl;
import net.sf.markov4jmeter.testplangenerator.transformation.AbstractTestPlanTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.SimpleTestPlanTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.AbstractFilter;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.HeaderDefaultsFilter;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.AbstractRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.BeanShellRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.HTTPRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.JUnitRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.JavaRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.SOAPRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.util.Configuration;
import net.sf.markov4jmeter.testplangenerator.util.EcoreObjectValidator;
import net.sf.markov4jmeter.testplangenerator.util.XmiEcoreHandler;

import org.apache.commons.cli.ParseException;
import org.apache.jmeter.save.SaveService;
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

    /* IMPLEMENTATION NOTE:
     * --------------------
     * The following elements of the Test Plan Factory have not been used for
     * creating Markov4JMeter Test Plans, but they are already supported by the
     * framework:
     *
     *   WhileController whileController = testPlanElementFactory.createWhileController();
     *   IfController ifController = testPlanElementFactory.createIfController();
     *   CounterConfig counterConfig = testPlanElementFactory.createCounterConfig();
     *
     * The following elements are just required as nested parts for other types
     * of Test Plan elements, but they can be even created independently:
     *
     *   Arguments arguments = testPlanElementFactory.createArguments();
     *   LoopController loopController = testPlanElementFactory.createLoopController();
     */

    /** Type constant for requests of type <i>HTTP</i>. */
    public final static String REQUEST_TYPE_HTTP = "http";

    /** Type constant for requests of type <i>Java</i>. */
    public final static String REQUEST_TYPE_JAVA = "java";

    /** Type constant for requests of type <i>BeanShell</i>. */
    public final static String REQUEST_TYPE_BEANSHELL = "beanshell";

    /** Type constant for requests of type <i>JUnit</i>. */
    public final static String REQUEST_TYPE_JUNIT = "junit";

    /** Type constant for requests of type <i>SOAP</i>. */
    public final static String REQUEST_TYPE_SOAP = "soap";

    /** Default properties file for the Test Plan Generator, to be used in case
     *  no user-defined properties file can be read from command line. */
    private final static String GENERATOR_DEFAULT_PROPERTIES =
            "configuration/generator.default.properties";

    /** Property key for the JMeter home directory. */
    private final static String PKEY_JMETER__HOME = "jmeter_home";

    /** Property key for the JMeter default properties. */
    private final static String PKEY_JMETER__PROPERTIES = "jmeter_properties";

    /** Property key for the language tag which indicates the locality. */
    private final static String PKEY_JMETER__LANGUAGE_TAG = "jmeter_languageTag";


    // info-, warn- and error-messages (names should be self-explaining);

    private final static String ERROR_CONFIGURATION_UNDEFINED =
            "Configuration file is null.";

    private final static String ERROR_CONFIGURATION_NOT_FOUND =
            "Could not find configuration file \"%s\".";

    private final static String ERROR_CONFIGURATION_READING_FAILED =
            "Could not read configuration file \"%s\".";

    private final static String ERROR_TEST_PLAN_PROPERTIES_UNDEFINED =
            "Test Plan properties file is null.";

    private final static String ERROR_TEST_PLAN_PROPERTIES_NOT_FOUND =
            "Could not find Test Plan properties file \"%s\".";

    private final static String ERROR_TEST_PLAN_PROPERTIES_READING_FAILED =
            "Could not read Test Plan properties file \"%s\".";

    private final static String ERROR_INITIALIZATION_FAILED =
            "Initialization of Test Plan Generator failed.";

    private final static String ERROR_UNSUPPORTED_REQUEST_TYPE =
            "Request type \"%s\" is not supported.";

    private final static String ERROR_INPUT_FILE_COULD_NOT_BE_READ =
            "Input file \"%s\" could not be read: %s";

    private final static String ERROR_OUTPUT_FILE_COULD_NOT_BE_WRITTEN =
            "Output file \"%s\" could not be written: %s";

    private final static String ERROR_OUTPUT_FILE_ACCESS_FAILED =
            "Could not access file \"%s\" for writing output data: %s";

    private final static String ERROR_TREE_SAVING_FAILED =
            "Could not save Test Plan tree \"%s\" via SaveService: %s";

    private final static String INFO_INITIALIZATION_SUCCESSFUL =
            "Test Plan Generator has been successfully initialized.";

    private final static String INFO_TEST_PLAN_GENERATION_STARTED =
            "Generating Test  Plan ...";

    private final static String INFO_TEST_PLAN_GENERATION_SUCCESSFUL =
            "Test Plan generation successful.";

    private final static String ERROR_TEST_PLAN_GENERATION_FAILED =
            "Test Plan generation failed.";

    private final static String INFO_MODEL_VALIDATION_STARTED =
            "Validating M4J-DSL model ...";

    private final static String INFO_MODEL_VALIDATION_SUCCESSFUL =
            "Validation of M4J-DSL model successful.";

    private final static String ERROR_MODEL_VALIDATION_FAILED =
            "Validation of M4J-DSL model failed.";

    private final static String WARNING_OUTPUT_FILE_CLOSING_FAILED =
            "Could not close file-output stream for file \"%s\": %s";

    private final static String ERROR_TEST_PLAN_RUN_FAILED =
            "Could not run Test Plan \"%s\": %s";


    /* *********************  global (non-final) fields  ******************** */


    /** Factory to be used for creating Test Plan elements. */
    private TestPlanElementFactory testPlanElementFactory;


    /* ***************************  constructors  *************************** */


    /**
     * Standard constructor of a Test Plan Generator, without parameters.
     */
    // TODO: this could be even implemented as singleton pattern;
    public TestPlanGenerator () { }


    /* **************************  public methods  ************************** */


    /**
     * Returns the Test Plan Factory associated with the Test Plan Generator.
     *
     * @return
     *     a valid Test Plan Factory, if the Test Plan Generator has been
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
     *     properties file which provides required or optional properties.
     * @param testPlanProperties
     *     file with Test Plan default properties.
     */
    public void init (
            final String configurationFile,
            final String testPlanProperties) {

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

            final boolean success =
                    JMeterEngineGateway.getInstance().initJMeter(
                            jMeterHome,
                            jMeterProperties,
                            jMeterLocale);

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

        this.logError(TestPlanGenerator.ERROR_INITIALIZATION_FAILED);
    }

    /**
     * Generates a Test Plan for the given workload model and writes the result
     * into the specified file.
     *
     * @param workloadModel
     *     workload model which provides the values for the Test Plan to be
     *     generated.
     * @param testPlanTransformer
     *     builder to be used for building a Test Plan of certain structure.
     * @param filters
     *     (optional) modification filters to be finally applied on the newly
     *     generated Test Plan.
     * @param outputFilename
     *     name of the file where the Test Plan shall be stored in.
     *
     * @return
     *     the generated Test Plan, or <code>null</code> if any error occurs.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public ListedHashTree generate (
            final WorkloadModel workloadModel,
            final AbstractTestPlanTransformer testPlanTransformer,
            final AbstractFilter[] filters,
            final String outputFilename) throws TransformationException {

        ListedHashTree testPlanTree = null;  // to be returned;

        final EcoreObjectValidator validator;
        final boolean validationSuccessful;

        this.logInfo(TestPlanGenerator.INFO_TEST_PLAN_GENERATION_STARTED);
        this.logInfo(TestPlanGenerator.INFO_MODEL_VALIDATION_STARTED);

        validator = new EcoreObjectValidator();
        validationSuccessful = validator.validateAndPrintResult(workloadModel);

        this.logInfo(TestPlanGenerator.INFO_MODEL_VALIDATION_SUCCESSFUL);

        if (!validationSuccessful) {

            this.logError(TestPlanGenerator.ERROR_MODEL_VALIDATION_FAILED);
            this.logError(TestPlanGenerator.ERROR_TEST_PLAN_GENERATION_FAILED);

        } else {  // validation successful -> generate Test Plan output file;

            testPlanTree = testPlanTransformer.transform(
                    workloadModel,
                    this.testPlanElementFactory,
                    filters);

            final boolean success =
                    this.writeOutput(testPlanTree, outputFilename);

            if (success) {

                this.logInfo(
                        TestPlanGenerator.INFO_TEST_PLAN_GENERATION_SUCCESSFUL);
            } else {

                this.logError(
                    TestPlanGenerator.ERROR_TEST_PLAN_GENERATION_FAILED);
            }
        }

        return testPlanTree;
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
     *     name of the file where the Test Plan shall be stored in.
     * @param testPlanTransformer
     *     builder to be used for building a Test Plan of certain structure.
     * @param filters
     *     (optional) modification filters to be finally applied on the newly
     *     generated Test Plan.
     *
     * @return
     *     the generated Test Plan, or <code>null</code> if any error occurs.
     *
     * @throws IOException
     *     in case any file reading or writing operation failed.
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public ListedHashTree generate (
            final String inputFile,
            final String outputFile,
            final AbstractTestPlanTransformer testPlanTransformer,
            final AbstractFilter[] filters)
                    throws IOException, TransformationException {

        // initialize the model package;
        M4jdslPackageImpl.init();

        // might throw an IOException;
        final WorkloadModel workloadModel = (WorkloadModel)
                XmiEcoreHandler.getInstance().xmiToEcore(inputFile, "xmi");

        return this.generate(
                workloadModel, testPlanTransformer, filters, outputFile);
    }


    /* *************************  protected methods  ************************ */


    /**
     * Writes a given Test Plan into the specified output file.
     *
     * @param testPlanTree    Test Plan to be written into the output file.
     * @param outputFilename  name of the output file.
     */
    protected boolean writeOutput (
            final ListedHashTree testPlanTree, final String outputFilename) {

        boolean success = true;
        FileOutputStream fileOutputStream = null;

        try {

            // might throw a FileNotFoundException or SecurityException;
            fileOutputStream = new FileOutputStream(outputFilename);

            // might throw an IOException;
            SaveService.saveTree(testPlanTree, fileOutputStream);

        } catch (final FileNotFoundException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_OUTPUT_FILE_COULD_NOT_BE_WRITTEN,
                    outputFilename,
                    ex.getMessage());

            this.logError(message);
            success = false;

        } catch (final SecurityException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_OUTPUT_FILE_ACCESS_FAILED,
                    outputFilename,
                    ex.getMessage());

            this.logError(message);
            success = false;

        } catch (final IOException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_TREE_SAVING_FAILED,
                    outputFilename,
                    ex.getMessage());

            this.logError(message);
            success = false;

        } finally {

            if (fileOutputStream != null) {

                try {

                    fileOutputStream.close();

                } catch (final IOException ex) {

                    final String message = String.format(
                            TestPlanGenerator.WARNING_OUTPUT_FILE_CLOSING_FAILED,
                            outputFilename,
                            ex.getMessage());

                    this.logWarning(message);
                    // success remains true, since output file content has been
                    // written, just the file could not be closed;
                }
            }
        }

        return success;
    }


    /* **************************  private methods  ************************* */


    /**
     * Reads all configuration properties from the specified file and gives an
     * error message, if reading fails.
     *
     * @param propertiesFile  properties file to be read.
     *
     * @return
     *     a valid configuration, if the specified properties file could be
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

            this.logError(message);
            configuration = null;  // indicates an error;

        } catch (final IOException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_CONFIGURATION_READING_FAILED,
                    propertiesFile);

            this.logError(message);
            configuration = null;  // indicates an error;

        } catch (final NullPointerException ex) {

            this.logError(TestPlanGenerator.ERROR_CONFIGURATION_UNDEFINED);
            configuration = null;  // indicates an error;
        }

        return configuration;
    }

    /**
     * Creates a Factory which builds Test Plan according to the default
     * properties defined in the specified file.
     *
     * @param propertiesFile
     *     properties file with default properties for Test Plan elements.
     *
     * @return
     *     a valid Test Plan Factory if the properties file could be read
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

            this.logError(message);

        } catch (final IOException ex) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_TEST_PLAN_PROPERTIES_READING_FAILED,
                    propertiesFile);

            this.logError(message);

        } catch (final NullPointerException ex) {

            this.logError(
                    TestPlanGenerator.ERROR_TEST_PLAN_PROPERTIES_UNDEFINED);
        }

        return testPlanElementFactory;
    }

    /**
     * Logs an information to standard output.
     *
     * @param message  information to be logged.
     */
    private void logInfo (final String message) {

        // TODO: remove print-command, solve the issue below;
        System.out.println(message);

        // this command would print in red color, indicating an error:
        //
        //   TestPlanGenerator.LOG.info(message);
        //
        // --> adjust "commons-logging.properties" accordingly;
        // --> even better: use JMeter logging unit;
    }

    /**
     * Logs a warning message to standard output.
     *
     * @param message  warning message to be logged.
     */
    private void logWarning (final String message) {

        // TODO: remove print-command, solve the issue below;
        System.err.println(message);

        // this command would print in non-uniform format:
        //
        //   TestPlanGenerator.LOG.warning(message);
        //
        // --> adjust "commons-logging.properties" accordingly;
        // --> even better: use JMeter logging unit;
    }

    /**
     * Logs an error message to standard output.
     *
     * @param message  error message to be logged.
     */
    private void logError (final String message) {

        // TODO: remove print-command, solve the issue below;
        System.err.println(message);

        // this command would print in non-uniform format:
        //
        //   TestPlanGenerator.LOG.error(message);
        //
        // --> adjust "commons-logging.properties" accordingly;
        // --> even better: use JMeter logging unit;
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
     *     name of the file where the Test Plan shall be stored in.
     * @param generatorPropertiesFile
     *     properties file which provides the core settings for the Test Plan
     *     Generator.
     * @param testPlanPropertiesFile
     *     properties file which provides the default settings for the Test
     *     Plans to be generated.
     * @param requestType
     *     type of requests to be sent; this must be one of the
     *     <code>REQUEST_TYPE</code> constants of this class.
     * @param filterFlags
     *     (optional) modification filters to be finally applied on the newly
     *     generated Test Plan.
     *
     * @return
     *     the generated Test Plan, or <code>null</code> if any error occurs.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    private ListedHashTree generate (
            final String inputFile,
            final String outputFile,
            final String generatorPropertiesFile,
            final String testPlanPropertiesFile,
            final String requestType,
            final String filterFlags) throws TransformationException {

        ListedHashTree testPlan = null;  // to be returned;

        final TestPlanGenerator testPlanGenerator = new TestPlanGenerator();

        // TODO: collect these filters according to the command line flags;
        final AbstractFilter[] filters = new AbstractFilter[]{

                // new ConstantWorkloadIntensityFilter(),

                /* this is just for testing, think times will be taken from the
                 * workload model and managed by the Markov Controller;
                 *
                     new GaussianThinkTimeDistributionFilter(
                        "Think Time",
                        "",
                        true,
                        300.0d,
                        100.0d),
                 */
                new HeaderDefaultsFilter()
        };

        final AbstractRequestTransformer requestTransformer =
                this.createRequestTransformer(requestType);

        if (requestTransformer == null) {

            final String message = String.format(
                    TestPlanGenerator.ERROR_UNSUPPORTED_REQUEST_TYPE,
                    requestType);

            this.logError(message);

        } else {

            testPlanGenerator.init(
                    generatorPropertiesFile,
                    testPlanPropertiesFile);

            if (testPlanGenerator.isInitialized()) {

                try {

                    testPlan = testPlanGenerator.generate(
                            inputFile,
                            outputFile,
                            new SimpleTestPlanTransformer(requestTransformer),
                            filters);

                } catch (final IOException ex) {

                    final String message = String.format(
                            TestPlanGenerator.ERROR_INPUT_FILE_COULD_NOT_BE_READ,
                            inputFile,
                            ex.getMessage());

                    this.logError(message);
                }
            }
        }

        return testPlan;
    }

    /**
     * Creates a Request Transformer of the given type.
     *
     * @param requestType
     *     type of requests to be sent; this must be one of the
     *     <code>REQUEST_TYPE</code> constants of this class.
     *
     * @return
     *     a valid instance of {@link AbstractRequestTransformer}, or
     *     <code>null</code> if the specified type is unknown.
     */
    private AbstractRequestTransformer createRequestTransformer (
            final String requestType) {

        final AbstractRequestTransformer requestTransformer;

        switch (requestType.toLowerCase()) {

            case TestPlanGenerator.REQUEST_TYPE_HTTP :

                requestTransformer = new HTTPRequestTransformer();
                break;

            case TestPlanGenerator.REQUEST_TYPE_JAVA :

                requestTransformer = new JavaRequestTransformer();
                break;

            case TestPlanGenerator.REQUEST_TYPE_BEANSHELL :

                requestTransformer = new BeanShellRequestTransformer();
                break;

            case TestPlanGenerator.REQUEST_TYPE_JUNIT :

                requestTransformer = new JUnitRequestTransformer();
                break;

            case TestPlanGenerator.REQUEST_TYPE_SOAP :

                requestTransformer = new SOAPRequestTransformer();
                break;

            default:

                requestTransformer = null;
        }

        return requestTransformer;
    }


    /* ************************  static main content  *********************** */


    /**
     * Main method which parses the command line parameters and generates a
     * Test Plan afterwards.
     *
     * @param argv arguments vector.
     */
    public static void main (final String[] argv) {

        try {

            // initialize arguments handler for requesting the command line
            // values afterwards via get() methods; might throw a
            // NullPointer-, IllegalArgument- or ParseException;
            CommandLineArgumentsHandler.init(argv);

            TestPlanGenerator.readArgumentsAndGenerate();

        } catch (final NullPointerException
                | IllegalArgumentException
                | ParseException
                | TransformationException ex) {

            System.err.println(ex.getMessage());
            CommandLineArgumentsHandler.printUsage();
        }
    }

    /**
     * Starts the generation process with the arguments which have been passed
     * to command line.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    private static void readArgumentsAndGenerate ()
            throws TransformationException {

        final TestPlanGenerator testPlanGenerator = new TestPlanGenerator();

        final String inputFile =
                CommandLineArgumentsHandler.getInputFile();

        final String outputFile =
                CommandLineArgumentsHandler.getOutputFile();

        final String testPlanPropertiesFile =
                CommandLineArgumentsHandler.getTestPlanPropertiesFile();

        final String requestType =
                CommandLineArgumentsHandler.getRequestType();

        final String filters =
                CommandLineArgumentsHandler.getFilters();

        final boolean runTest =
                CommandLineArgumentsHandler.getRunTest();

        String generatorPropertiesFile =
                CommandLineArgumentsHandler.getGeneratorPropertiesFile();

        if (generatorPropertiesFile == null) {

            generatorPropertiesFile =
                    TestPlanGenerator.GENERATOR_DEFAULT_PROPERTIES;
        }

        // ignore returned Test Plan, since the output file will provide it
        // for being tested in the (possibly) following test run;
        testPlanGenerator.generate(
                inputFile,
                outputFile,
                generatorPropertiesFile,
                testPlanPropertiesFile,
                requestType,
                filters);

        if (runTest) {

            // TODO: libraries need to be added for running tests correctly;
            // otherwise the test fails at runtime (e.g., class HC3CookieHandler
            // is declared to be still unknown);

            try {

                JMeterEngineGateway.getInstance().startJMeterEngine(outputFile);

            } catch (final Exception ex) {

                final String message = String.format(
                        TestPlanGenerator.ERROR_TEST_PLAN_RUN_FAILED,
                        outputFile,
                        ex.getMessage());

                System.err.println(message);
                ex.printStackTrace();
            }
        }
    }
}
