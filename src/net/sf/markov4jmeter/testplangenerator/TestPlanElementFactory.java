package net.sf.markov4jmeter.testplangenerator;

import java.io.IOException;
import java.util.ArrayList;

import net.sf.markov4jmeter.testplangenerator.util.CSVReader;
import net.sf.markov4jmeter.testplangenerator.util.Configuration;
import net.voorn.markov4jmeter.control.ApplicationState;
import net.voorn.markov4jmeter.control.BehaviorMix;
import net.voorn.markov4jmeter.control.BehaviorMixEntry;
import net.voorn.markov4jmeter.control.MarkovController;
import net.voorn.markov4jmeter.control.gui.ApplicationStateGui;
import net.voorn.markov4jmeter.control.gui.MarkovControllerGui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WhileControllerGui;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.gui.CounterConfigGui;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.gui.SetupThreadGroupGui;
import org.apache.jmeter.timers.GaussianRandomTimer;
import org.apache.jmeter.timers.gui.GaussianRandomTimerGui;
import org.apache.jmeter.visualizers.RespTimeGraphVisualizer;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;


/**
 * This factory class provides methods for creating pre-configured JMeter Test
 * Plan elements with default properties.
 *
 * <p>The following elements are supported (in brackets the related names used
 * in the JMeter GUI):
 * <ul>
 *   <li>{@link TestPlan}</li>
 *   <li>{@link Arguments}</li> (User Defined Variables)
 *   <li>{@link SetupThreadGroup}</li> (Thread Group)
 *   <li>{@link LoopController}</li>
 *   <li>{@link WhileController}</li>
 *   <li>{@link GaussianRandomTimer}</li>
 *   <li>{@link CookieManager}</li>
 *   <li>{@link ResponseAssertion}</li>
 *   <li>{@link CounterConfig}</li> (Counter)
 *   <li>{@link RegexExtractor}</li> (Regular Expression Extractor)
 *   <li>{@link HeaderManager}</li>
 *   <li>{@link HTTPSamplerProxy}</li> (HTTP Sampler)
 *   <li>{@link ConfigTestElement}</li> (HTTP Request Defaults)
 *   <li>{@link MarkovController}</li> (Markov Session Controller)
 *   <li>{@link ApplicationState}</li> (Markov State)
 *   <li>{@link ResultCollector}</li> (Response Time Graph | View Results Tree)
 * </ul>
 * <p>The properties of all of these elements have to be defined in a single
 * properties file, which must be passed as a {@link Configuration} instance
 * to the constructor of this class. Additional arguments for certain elements
 * can be defined via CSV-files, whose filenames might be specified in the
 * properties file as well. This allows the instantiation of fully customized
 * Test Plan elements. It is important to note that the CSV-files generally use
 * tabulators as separators between single values.
 *
 * <p>Additionally, a <code>useForcedValues</code> flag (to be passed to the
 * constructor) allows the specification whether property values must be
 * defined, or if they are handled as optional.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 * @since    1.7
 */
public final class TestPlanElementFactory {

    /* IMPLEMENTATION NOTE:
     * --------------------
     * Some of the supported JMeter Test Plan elements provide ambiguous set()-
     * methods for their properties; in particular, parameters of type String
     * and (simple) data types, e.g. int or long, are sometimes possible
     * alternatively; in those cases, the latter mentioned set() methods have
     * been used for setting the values for keeping the type control as strict
     * as possible.
     */

    // property keys (no further comments given here);
    private final static String

    PKEY_TEST_PLAN__NAME                                               = "testPlan_name",
    PKEY_TEST_PLAN__COMMENT                                            = "testPlan_comment",
    PKEY_TEST_PLAN__ENABLED                                            = "testPlan_enabled",
    PKEY_TEST_PLAN__PARAMETERS_FILE                                    = "testPlan_parametersFile",
    PKEY_TEST_PLAN__SERIALIZED                                         = "testPlan_serialized",
    PKEY_TEST_PLAN__TEAR_DOWN_ON_SHUT_DOWN                             = "testPlan_tearDownOnShutDown",
    PKEY_TEST_PLAN__FUNCTIONAL_MODE                                    = "testPlan_functionalMode",
    PKEY_TEST_PLAN__CLASS_PATH_FILES_FILE                              = "testPlan_classPathFilesFile",

    PKEY_ARGUMENTS__NAME                                               = "arguments_name",
    PKEY_ARGUMENTS__COMMENT                                            = "arguments_comment",
    PKEY_ARGUMENTS__ENABLED                                            = "arguments_enabled",
    PKEY_ARGUMENTS__PARAMETERS_FILE                                    = "arguments_parametersFile",

    PKEY_SETUP_THREAD_GROUP__NAME                                      = "setupThreadGroup_name",
    PKEY_SETUP_THREAD_GROUP__COMMENT                                   = "setupThreadGroup_comment",
    PKEY_SETUP_THREAD_GROUP__ENABLED                                   = "setupThreadGroup_enabled",
    PKEY_SETUP_THREAD_GROUP__RAMP_UP                                   = "setupThreadGroup_rampUp",
    PKEY_SETUP_THREAD_GROUP__NUM_THREADS                               = "setupThreadGroup_numThreads",
    PKEY_SETUP_THREAD_GROUP__ON_SAMPLE_ERROR                           = "setupThreadGroup_onSampleError",
    PKEY_SETUP_THREAD_GROUP__SCHEDULER                                 = "setupThreadGroup_scheduler",
    PKEY_SETUP_THREAD_GROUP__SCHEDULER__START_TIME                     = "setupThreadGroup_scheduler_startTime",
    PKEY_SETUP_THREAD_GROUP__SCHEDULER__END_TIME                       = "setupThreadGroup_scheduler_endTime",
    PKEY_SETUP_THREAD_GROUP__SCHEDULER__DURATION                       = "setupThreadGroup_scheduler_duration",
    PKEY_SETUP_THREAD_GROUP__SCHEDULER__DELAY                          = "setupThreadGroup_scheduler_delay",
    PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__NAME                     = "setupThreadGroup_loopController_name",
    PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__COMMENT                  = "setupThreadGroup_loopController_comment",
    PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__ENABLED                  = "setupThreadGroup_loopController_enabled",
    PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__LOOPS                    = "setupThreadGroup_loopController_loops",
    PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__FOREVER                  = "setupThreadGroup_loopController_forever",

    PKEY_LOOP_CONTROLLER__NAME                                         = "loopController_name",
    PKEY_LOOP_CONTROLLER__COMMENT                                      = "loopController_comment",
    PKEY_LOOP_CONTROLLER__ENABLED                                      = "loopController_enabled",
    PKEY_LOOP_CONTROLLER__LOOPS                                        = "loopController_loops",
    PKEY_LOOP_CONTROLLER__FOREVER                                      = "loopController_forever",

    PKEY_WHILE_CONTROLLER__NAME                                        = "whileController_name",
    PKEY_WHILE_CONTROLLER__COMMENT                                     = "whileController_comment",
    PKEY_WHILE_CONTROLLER__ENABLED                                     = "whileController_enabled",
    PKEY_WHILE_CONTROLLER__CONDITION                                   = "whileController_condition",

    PKEY_GAUSSIAN_RANDOM_TIMER__NAME                                   = "gaussianRandomTimer_name",
    PKEY_GAUSSIAN_RANDOM_TIMER__COMMENT                                = "gaussianRandomTimer_comment",
    PKEY_GAUSSIAN_RANDOM_TIMER__ENABLED                                = "gaussianRandomTimer_enabled",
    PKEY_GAUSSIAN_RANDOM_TIMER__DELAY                                  = "gaussianRandomTimer_delay",
    PKEY_GAUSSIAN_RANDOM_TIMER__RANGE                                  = "gaussianRandomTimer_range",

    PKEY_COOKIE_MANAGER__NAME                                          = "cookieManager_name",
    PKEY_COOKIE_MANAGER__COMMENT                                       = "cookieManager_comment",
    PKEY_COOKIE_MANAGER__ENABLED                                       = "cookieManager_enabled",
    PKEY_COOKIE_MANAGER__CLEAR_EACH_ITERATION                          = "cookieManager_clearEachIteration",
    PKEY_COOKIE_MANAGER__COOKIE_POLICY                                 = "cookieManager_cookiePolicy",
    PKEY_COOKIE_MANAGER__IMPLEMENTATION                                = "cookieManager_implementation",

    PKEY_RESPONSE_ASSERTION__NAME                                      = "responseAssertion_name",
    PKEY_RESPONSE_ASSERTION__COMMENT                                   = "responseAssertion_comment",
    PKEY_RESPONSE_ASSERTION__ENABLED                                   = "responseAssertion_enabled",
    PKEY_RESPONSE_ASSERTION__ASSUME_SUCCESS                            = "responseAssertion_assumeSuccess",
    PKEY_RESPONSE_ASSERTION__SCOPE                                     = "responseAssertion_scope",
    PKEY_RESPONSE_ASSERTION__SCOPE_VARIABLE_NAME                       = "responseAssertion_scopeVariableName",
    PKEY_RESPONSE_ASSERTION__TEST_FIELD                                = "responseAssertion_testField",
    PKEY_RESPONSE_ASSERTION__TYPE                                      = "responseAssertion_type",

    PKEY_COUNTER_CONFIG__NAME                                          = "counterConfig_name",
    PKEY_COUNTER_CONFIG__COMMENT                                       = "counterConfig_comment",
    PKEY_COUNTER_CONFIG__ENABLED                                       = "counterConfig_enabled",
    PKEY_COUNTER_CONFIG__START                                         = "counterConfig_start",
    PKEY_COUNTER_CONFIG__INCREMENT                                     = "counterConfig_increment",
    PKEY_COUNTER_CONFIG__END                                           = "counterConfig_end",
    PKEY_COUNTER_CONFIG__FORMAT                                        = "counterConfig_format",
    PKEY_COUNTER_CONFIG__REFERENCE_NAME                                = "counterConfig_referenceName",
    PKEY_COUNTER_CONFIG__IS_PER_USER                                   = "counterConfig_isPerUser",
    PKEY_COUNTER_CONFIG__RESET_ON_THREAD_GROUP_ITERATION               = "counterConfig_resetOnThreadGroupIteration",

    PKEY_REGEX_EXTRACTOR__NAME                                         = "regularExpressionExtractor_name",
    PKEY_REGEX_EXTRACTOR__COMMENT                                      = "regularExpressionExtractor_comment",
    PKEY_REGEX_EXTRACTOR__ENABLED                                      = "regularExpressionExtractor_enabled",
    PKEY_REGEX_EXTRACTOR__SCOPE                                        = "regularExpressionExtractor_scope",
    PKEY_REGEX_EXTRACTOR__SCOPE_VARIABLE_NAME                          = "regularExpressionExtractor_scopeVariableName",
    PKEY_REGEX_EXTRACTOR__TEST_FIELD                                   = "regularExpressionExtractor_testField",
    PKEY_REGEX_EXTRACTOR__REFERENCE_NAME                               = "regularExpressionExtractor_referenceName",
    PKEY_REGEX_EXTRACTOR__REGULAR_EXPRESSION                           = "regularExpressionExtractor_regularExpression",
    PKEY_REGEX_EXTRACTOR__TEMPLATE                                     = "regularExpressionExtractor_template",
    PKEY_REGEX_EXTRACTOR__MATCH_NUMBER                                 = "regularExpressionExtractor_matchNumber",
    PKEY_REGEX_EXTRACTOR__DEFAULT_VALUE                                = "regularExpressionExtractor_defaultValue",

    PKEY_HEADER_MANAGER__NAME                                          = "headerManager_name",
    PKEY_HEADER_MANAGER__COMMENT                                       = "headerManager_comment",
    PKEY_HEADER_MANAGER__ENABLED                                       = "headerManager_enabled",
    PKEY_HEADER_MANAGER__HEADERS_FILE                                  = "headerManager_headersFile",

    PKEY_HTTP_SAMPLER_PROXY__NAME                                      = "httpSamplerProxy_name",
    PKEY_HTTP_SAMPLER_PROXY__COMMENT                                   = "httpSamplerProxy_comment",
    PKEY_HTTP_SAMPLER_PROXY__ENABLED                                   = "httpSamplerProxy_enabled",
    PKEY_HTTP_SAMPLER_PROXY__DOMAIN                                    = "httpSamplerProxy_domain",
    PKEY_HTTP_SAMPLER_PROXY__PORT                                      = "httpSamplerProxy_port",
    PKEY_HTTP_SAMPLER_PROXY__CONNECT_TIMEOUT                           = "httpSamplerProxy_connectTimeout",
    PKEY_HTTP_SAMPLER_PROXY__RESPONSE_TIMEOUT                          = "httpSamplerProxy_responseTimeout",
    PKEY_HTTP_SAMPLER_PROXY__IMPLEMENTATION                            = "httpSamplerProxy_implementation",
    PKEY_HTTP_SAMPLER_PROXY__PROTOCOL                                  = "httpSamplerProxy_protocol",
    PKEY_HTTP_SAMPLER_PROXY__METHOD                                    = "httpSamplerProxy_method",
    PKEY_HTTP_SAMPLER_PROXY__CONTENT_ENCODING                          = "httpSamplerProxy_contentEncoding",
    PKEY_HTTP_SAMPLER_PROXY__PATH                                      = "httpSamplerProxy_path",
    PKEY_HTTP_SAMPLER_PROXY__AUTO_REDIRECTS                            = "httpSamplerProxy_autoRedirects",
    PKEY_HTTP_SAMPLER_PROXY__FOLLOW_REDIRECTS                          = "httpSamplerProxy_followRedirects",
    PKEY_HTTP_SAMPLER_PROXY__USE_KEEP_ALIVE                            = "httpSamplerProxy_useKeepAlive",
    PKEY_HTTP_SAMPLER_PROXY__DO_MULTIPART_POST                         = "httpSamplerProxy_doMultipartPost",
    PKEY_HTTP_SAMPLER_PROXY__DO_BROWSER_COMPATIBLE_MULTIPART           = "httpSamplerProxy_doBrowserCompatibleMultipart",
    PKEY_HTTP_SAMPLER_PROXY__ARGUMENTS_FILE                            = "httpSamplerProxy_argumentsFile",
    PKEY_HTTP_SAMPLER_PROXY__HTTP_FILES_FILE                           = "httpSamplerProxy_httpFilesFile",
    PKEY_HTTP_SAMPLER_PROXY__PROXY_HOST                                = "httpSamplerProxy_proxyHost",
    PKEY_HTTP_SAMPLER_PROXY__PROXY_PORT                                = "httpSamplerProxy_proxyPort",
    PKEY_HTTP_SAMPLER_PROXY__PROXY_USER                                = "httpSamplerProxy_proxyUser",
    PKEY_HTTP_SAMPLER_PROXY__PROXY_PASSWORD                            = "httpSamplerProxy_proxyPassword",
    PKEY_HTTP_SAMPLER_PROXY__PARSE_IMAGES                              = "httpSamplerProxy_parseImages",
    PKEY_HTTP_SAMPLER_PROXY__EMBEDDED_URL_REGULAR_EXPRESSION           = "httpSamplerProxy_embeddedUrlRegularExpression",
    PKEY_HTTP_SAMPLER_PROXY__USE_CONCURRENT_POOL_SIZE                  = "httpSamplerProxy_useConcurrentPoolSize",
    PKEY_HTTP_SAMPLER_PROXY__CONCURRENT_POOL_SIZE                      = "httpSamplerProxy_concurrentPoolSize",
    PKEY_HTTP_SAMPLER_PROXY__IP_SOURCE_TYPE                            = "httpSamplerProxy_ipSourceType",
    PKEY_HTTP_SAMPLER_PROXY__IP_SOURCE                                 = "httpSamplerProxy_ipSource",
    PKEY_HTTP_SAMPLER_PROXY__USE_AS_MONITOR                            = "httpSamplerProxy_useAsMonitor",
    PKEY_HTTP_SAMPLER_PROXY__SAVE_RESPONSE_AS_MD5_HASH                 = "httpSamplerProxy_saveResponseAsMD5Hash",

    PKEY_CONFIG_TEST_ELEMENT__NAME                                     = "configTestElement_name",
    PKEY_CONFIG_TEST_ELEMENT__COMMENT                                  = "configTestElement_comment",
    PKEY_CONFIG_TEST_ELEMENT__ENABLED                                  = "configTestElement_enabled",
    PKEY_CONFIG_TEST_ELEMENT__DOMAIN                                   = "configTestElement_domain",
    PKEY_CONFIG_TEST_ELEMENT__PORT                                     = "configTestElement_port",
    PKEY_CONFIG_TEST_ELEMENT__CONNECT_TIMEOUT                          = "configTestElement_connectTimeout",
    PKEY_CONFIG_TEST_ELEMENT__RESPONSE_TIMEOUT                         = "configTestElement_responseTimeout",
    PKEY_CONFIG_TEST_ELEMENT__IMPLEMENTATION                           = "configTestElement_implementation",
    PKEY_CONFIG_TEST_ELEMENT__PROTOCOL                                 = "configTestElement_protocol",
    PKEY_CONFIG_TEST_ELEMENT__CONTENT_ENCODING                         = "configTestElement_contentEncoding",
    PKEY_CONFIG_TEST_ELEMENT__PATH                                     = "configTestElement_path",
    PKEY_CONFIG_TEST_ELEMENT__ARGUMENTS_FILE                           = "configTestElement_argumentsFile",
    PKEY_CONFIG_TEST_ELEMENT__PROXY_HOST                               = "configTestElement_proxyHost",
    PKEY_CONFIG_TEST_ELEMENT__PROXY_PORT                               = "configTestElement_proxyPort",
    PKEY_CONFIG_TEST_ELEMENT__PROXY_USER                               = "configTestElement_proxyUser",
    PKEY_CONFIG_TEST_ELEMENT__PROXY_PASSWORD                           = "configTestElement_proxyPassword",
    PKEY_CONFIG_TEST_ELEMENT__PARSE_IMAGES                             = "configTestElement_parseImages",
    PKEY_CONFIG_TEST_ELEMENT__EMBEDDED_URL_REGULAR_EXPRESSION          = "configTestElement_embeddedUrlRegularExpression",
    PKEY_CONFIG_TEST_ELEMENT__USE_CONCURRENT_POOL_SIZE                 = "configTestElement_useConcurrentPoolSize",
    PKEY_CONFIG_TEST_ELEMENT__CONCURRENT_POOL_SIZE                     = "configTestElement_concurrentPoolSize",

    PKEY_MARKOV_CONTROLLER__NAME                                       = "markovController_name",
    PKEY_MARKOV_CONTROLLER__COMMENT                                    = "markovController_comment",
    PKEY_MARKOV_CONTROLLER__ENABLED                                    = "markovController_enabled",
    PKEY_MARKOV_CONTROLLER__BEHAVIOR_MIX_FILE                          = "markovController_behaviorMixFile",
    PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__ENABLED                = "markovController_sessionArrivalController_enabled",
    PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__MAXIMUM_SESSION_NUMBER = "markovController_sessionArrivalController_maximumSessionNumber",
    PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__LOGGING_ENABLED        = "markovController_sessionArrivalController_loggingEnabled",
    PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__LOG_FILE               = "markovController_sessionArrivalController_logFile",

    PKEY_APPLICATION_STATE__NAME                                       = "applicationState_name",
    PKEY_APPLICATION_STATE__COMMENT                                    = "applicationState_comment",
    PKEY_APPLICATION_STATE__ENABLED                                    = "applicationState_enabled",

    PKEY_RESPONSE_TIME_GRAPH__NAME                                     = "responseTimeGraph_name",
    PKEY_RESPONSE_TIME_GRAPH__COMMENT                                  = "responseTimeGraph_comment",
    PKEY_RESPONSE_TIME_GRAPH__ENABLED                                  = "responseTimeGraph_enabled",

    PKEY_VIEW_RESULTS_TREE__NAME                                       = "viewResultsTree_name",
    PKEY_VIEW_RESULTS_TREE__COMMENT                                    = "viewResultsTree_comment",
    PKEY_VIEW_RESULTS_TREE__ENABLED                                    = "viewResultsTree_enabled";

    /** Scope-property of an {@link AbstractScopedTestElement} instance
     *  ("main sample and sub-samples"); */
    private final static String SCOPE_ALL = "scopeAll";

    /** Scope-property of an {@link AbstractScopedTestElement} instance
     *  ("sub-samples only"); */
    private final static String SCOPE_CHILDREN = "scopeChildren";

    /** Scope-property of an {@link AbstractScopedTestElement} instance
     *  ("main sample only", generally used by default); */
    private final static String SCOPE_PARENT = "scopeParent";

    // miscellaneous type constants for ResponseAssertions;

    private final static String TEST_FIELD_RESPONSE_CODE             = "responseCode";
    private final static String TEST_FIELD_RESPONSE_DATA             = "responseData";
    private final static String TEST_FIELD_RESPONSE_DATA_AS_DOCUMENT = "responseDataAsDocument";
    private final static String TEST_FIELD_RESPONSE_HEADERS          = "responseHeaders";
    private final static String TEST_FIELD_RESPONSE_MESSAGE          = "responseMessage";
    private final static String TEST_FIELD_URL                       = "URL";

    private final static String RESPONSE_ASSERTION_EQUALS_TYPE       = "equalsType";
    private final static String RESPONSE_ASSERTION_MATCH_TYPE        = "matchType";
    private final static String RESPONSE_ASSERTION_NOT_TYPE          = "notType";
    private final static String RESPONSE_ASSERTION_SUBSTRING_TYPE    = "substringType";
    private final static String RESPONSE_ASSERTION_CONTAINS_TYPE     = "containsType";

    /** Pattern for a property value which denotes "true". */
    private final static String TRUE_STRING = "true";

    /** Default separator for tokens in CSV-files. */
    // note: the '+' is important for reading -all- successive tabulators;
    private final static String DEFAULT_CSV_SEPARATOR = "\\t+";

    /** Warning message for the case that a CSV-file cannot be read. */
    private final static String WARNING_INVALID_CSV_FILE =
            "Could not read CSV-data from file \"%s\" for element %s: %s";

    /** Warning message for the case that a line of a CSV-file has too few
     *  tokens. */
    private final static String WARNING_INVALID_CSV_FILE_ENTRY =
            "Could not extract information for element %s: "
            + "line %d in file \"%s\" has too few tokens.";

    /** Warning message for the case that a property value of type
     *  <code>double</code> is invalid. */
    private final static String WARNING_UNVALID_CSV_DOUBLE =
            "Could not parse double value for \"%s\"; "
            + "String \"%s\" invalid, will use value %s instead.";

    /** Warning message for the case that a parameters filename is invalid. */
    private final static String WARNING_PARAMETERS_FILENAME_INVALID =
            "Could not parse parameters for \"%s\"; filename \"%s\" invalid.";

    /** Log-factory for any warnings or error messages. */
    private final static Log LOG =
            LogFactory.getLog(TestPlanElementFactory.class);


    /* *********************  global (non-final) fields  ******************** */


    /** Configuration with default properties to be used. */
    private final Configuration configuration;

    /**
     * Flag which indicates whether properties must be defined in the used
     * configuration.
     * <ul>
     *   <li> If the flag is set <code>true</code>, a warning will be given
     *   for each undefined property, and a default value will be set, e.g., 0
     *   for an undefined <code>int</code> value.
     *
     *   <li> If the flag is set <code>false</code>, property definitions are
     *   optional; in that case, an undefined property will be ignored, and the
     *   default setting of the related Test Plan element will be used. In
     *   particular, nothing will be set in that case.
     * </ul>
     */
    private boolean useForcedValues = true;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for a Test Plan Element Factory with a specific
     * configuration of the elements to be created.
     *
     * @param configuration
     *     Configuration of the elements to be created.
     * @param useForcedValues
     *     Flag which indicates whether properties must be defined in the used
     *     configuration.
     *     <ul>
     *       <li> If the flag is set <code>true</code>, a warning will be given
     *       for each undefined property, and a default value will be set, e.g.,
     *       0 for an undefined <code>int</code>-typed property.
     *
     *       <li> If the flag is set <code>false</code>, property definitions
     *       are optional; in that case, an undefined property will be ignored,
     *       and the default setting of the related Test Plan element will be
     *       used. In particular, nothing will be set in that case.
     *     </ul>
     */
    public TestPlanElementFactory (
            final Configuration configuration,
            final boolean useForcedValues) {

        this.configuration   = configuration;
        this.useForcedValues = useForcedValues;
    }


    /* **************************  public methods  ************************** */


    /**
     * Returns the configuration of the elements to be created.
     *
     * @return
     *     A valid instance of {@link Configuration} which has been passed to
     *     the constructor of this class.
     *
     * @see TestPlanElementFactory#TestPlanElementFactory(Configuration, boolean)
     */
    public Configuration getConfiguration () {

        return this.configuration;
    }

    /**
     * Returns the information whether properties must be defined in the used
     * configuration. This is specified through the <code>useForcedValues</code>
     * flag which is passed to the constructor of this class.
     *
     * @return
     *     <code>true</code> if and only if properties must be defined in the
     *     used configuration.
     *
     * @see TestPlanElementFactory#TestPlanElementFactory(Configuration, boolean)
     */
    public boolean usesForcedValues () {

        return this.useForcedValues;
    }

    /**
     * Creates a {@link TestPlan} instance with default properties.
     *
     * @return  A valid instance of {@link TestPlan}.
     */
    public TestPlan createTestPlan () {

        final TestPlanGui gui = new TestPlanGui();
        final TestPlan testPlan = (TestPlan) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                testPlan,
                TestPlanElementFactory.PKEY_TEST_PLAN__NAME,
                TestPlanElementFactory.PKEY_TEST_PLAN__COMMENT,
                TestPlanElementFactory.PKEY_TEST_PLAN__ENABLED);

        key = TestPlanElementFactory.PKEY_TEST_PLAN__SERIALIZED;

        if (forced || c.containsKey(key)) {

            // true if and only if Thread Groups shall be run consecutively;
            final boolean value = c.getBoolean(key);

            testPlan.setSerialized(value);
        }

        key = TestPlanElementFactory.PKEY_TEST_PLAN__TEAR_DOWN_ON_SHUT_DOWN;

        if (forced || c.containsKey(key)) {

            // true if and only if TearDown Thread Groups shall be run after
            // shutdown of main threads;
            final boolean value = c.getBoolean(key);

            testPlan.setTearDownOnShutdown(value);
        }

        key = TestPlanElementFactory.PKEY_TEST_PLAN__FUNCTIONAL_MODE;

        if (forced || c.containsKey(key)) {

            // true if and only if the functional mode shall be enabled; should
            // be false for not adversely affecting the performance;
            final boolean functionalMode = c.getBoolean(key);

            testPlan.setFunctionalMode(functionalMode);
        }

        key = TestPlanElementFactory.PKEY_TEST_PLAN__PARAMETERS_FILE;

        if (forced || c.containsKey(key)) {

            final String parametersFile = c.getString(key);

            if ( this.isValidValue(parametersFile) ) {

                // user-defined set of arguments;
                // this must be an array of name/value pairs;
                final String[][] parameters =
                        this.readParameters(parametersFile, TestPlan.class, 2);

                for (String[] parameter : parameters) {

                    final String name  = parameter[0];
                    final String value = parameter[1];

                    testPlan.addParameter(name, value);
                }
            }
        }

        key = TestPlanElementFactory.PKEY_TEST_PLAN__CLASS_PATH_FILES_FILE;

        if (forced || c.containsKey(key)) {

            final String classPathFilesFile = c.getString(key);

            if ( this.isValidValue(classPathFilesFile) ) {

                // default directories or JARs to be added to the class path;
                // this must be a simple array of paths.
                final String[] classPathArray =
                        this.readSimpleList(classPathFilesFile, TestPlan.class);

                testPlan.setTestPlanClasspathArray(classPathArray);
            }
        }

        return testPlan;
    }

    /**
     * Creates an {@link Arguments} instance with default properties.
     *
     * @return  A valid instance of {@link Arguments}.
     */
    public Arguments createArguments () {

        final ArgumentsPanel gui = new ArgumentsPanel();
        final Arguments arguments = (Arguments) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                arguments,
                TestPlanElementFactory.PKEY_ARGUMENTS__NAME,
                TestPlanElementFactory.PKEY_ARGUMENTS__COMMENT,
                TestPlanElementFactory.PKEY_ARGUMENTS__ENABLED);

        key = TestPlanElementFactory.PKEY_ARGUMENTS__PARAMETERS_FILE;

        if (forced || c.containsKey(key)) {

            final String parametersFile = c.getString(key);

            if ( this.isValidValue(parametersFile) ) {

                // user-defined set of arguments;
                // this must be an array of name/value pairs;
                final String[][] parameters =
                        this.readParameters(parametersFile, Arguments.class, 3);

                for (String[] parameter : parameters) {

                    final String name        = parameter[0];
                    final String value       = parameter[1];
                    final String description = parameter[2];

                    final Argument argument =
                            new Argument(name, value, "=", description);

                    arguments.addArgument(argument);
                }
            }
        }

        return arguments;
    }

    /**
     * Creates a {@link SetupThreadGroup} instance with default properties.
     *
     * @return  A valid instance of {@link SetupThreadGroup}.
     */
    public SetupThreadGroup createSetupThreadGroup () {

        // JMeter uses the current time for start- and end-time by default;
        // final long currentTimeMillis = System.currentTimeMillis();

        final SetupThreadGroupGui gui = new SetupThreadGroupGui();

        final SetupThreadGroup setupThreadGroup =
                (SetupThreadGroup) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                setupThreadGroup,
                TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__NAME,
                TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__COMMENT,
                TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__ENABLED);

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__RAMP_UP;

        if (forced || c.containsKey(key)) {

            // ramp-up time in milliseconds;
            final int value = c.getInt(key);

            setupThreadGroup.setRampUp(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__NUM_THREADS;

        if (forced || c.containsKey(key)) {

            // number of threads;
            final int value = c.getInt(key);

            setupThreadGroup.setNumThreads(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__ON_SAMPLE_ERROR;

        if (forced || c.containsKey(key)) {

            // action to be taken after a Sampler error occurs; this must be one
            // of the ON_SAMPLE_ERROR-constants which are defined in class
            // AbstractThreadGroup;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            setupThreadGroup.setProperty(
                    SetupThreadGroup.ON_SAMPLE_ERROR, value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__SCHEDULER;

        if (forced || c.containsKey(key)) {

            // true if and only if the embedded Scheduler shall be used;
            final boolean value = c.getBoolean(key);

            setupThreadGroup.setScheduler(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__SCHEDULER__START_TIME;

        if (forced || c.containsKey(key)) {

            // start time of the embedded Scheduler;
            final long value = c.getLong(key);

            setupThreadGroup.setStartTime(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__SCHEDULER__END_TIME;

        if (forced || c.containsKey(key)) {

            // end time of the embedded Scheduler;
            final long value = c.getLong(key);

            setupThreadGroup.setEndTime(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__SCHEDULER__DURATION;

        if (forced || c.containsKey(key)) {

            // duration of the embedded Scheduler in seconds;
            final long value = c.getLong(key);

            setupThreadGroup.setDuration(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__SCHEDULER__DELAY;

        if (forced || c.containsKey(key)) {

            // delay of the embedded Scheduler in seconds;
            final long value = c.getLong(key);

            setupThreadGroup.setDelay(value);
        }

        // embedded Loop Controller ("Sampler Controller");
        final LoopController loopController = this.createLoopController();

        this.setTestElementProperties(
                loopController,
                TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__NAME,
                TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__COMMENT,
                TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__ENABLED);

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__LOOPS;

        if (forced || c.containsKey(key)) {

            // number of loops to be run by the embedded Loop Controller;
            final int value = c.getInt(key);

            loopController.setLoops(value);
        }

        key = TestPlanElementFactory.PKEY_SETUP_THREAD_GROUP__LOOP_CONTROLLER__FOREVER;

        if (forced || c.containsKey(key)) {

            // true if and only if the Loop Controller shall run continually;
            // number of loops will be ignored in that case;
            final boolean value = c.getBoolean(key);

            loopController.setContinueForever(value);
        }

        setupThreadGroup.setSamplerController(loopController);

        return setupThreadGroup;
    }

    /**
     * Creates a {@link LoopController} instance with default values.
     *
     * @return  A valid instance of {@link LoopController}.
     */
    public LoopController createLoopController () {

        final LoopControlPanel gui = new LoopControlPanel();

        final LoopController loopController =
                (LoopController) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                loopController,
                TestPlanElementFactory.PKEY_LOOP_CONTROLLER__NAME,
                TestPlanElementFactory.PKEY_LOOP_CONTROLLER__COMMENT,
                TestPlanElementFactory.PKEY_LOOP_CONTROLLER__ENABLED);

        key = TestPlanElementFactory.PKEY_LOOP_CONTROLLER__LOOPS;

        if (forced || c.containsKey(key)) {

            // number of loops to be run;
            final int value = c.getInt(key);

            loopController.setLoops(value);
        }

        key = TestPlanElementFactory.PKEY_LOOP_CONTROLLER__FOREVER;

        if (forced || c.containsKey(key)) {

            // true if and only if the Loop Controller shall run continually;
            // number of loops will be ignored in that case;
            final boolean value = c.getBoolean(key);

            loopController.setContinueForever(value);
        }

        return loopController;
    }

    /**
     * Creates a {@link WhileController} instance with default values.
     *
     * @return  A valid instance of {@link WhileController}.
     */
    public WhileController createWhileController () {

        final WhileControllerGui gui = new WhileControllerGui();

        final WhileController whileController =
                (WhileController) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                whileController,
                TestPlanElementFactory.PKEY_WHILE_CONTROLLER__NAME,
                TestPlanElementFactory.PKEY_WHILE_CONTROLLER__COMMENT,
                TestPlanElementFactory.PKEY_WHILE_CONTROLLER__ENABLED);

        key = TestPlanElementFactory.PKEY_WHILE_CONTROLLER__CONDITION;

        if (forced || c.containsKey(key)) {

            // condition to be met for executing the While controller's child
            // elements;
            final String value = c.getString(key);

            whileController.setCondition(value);
        }

        return whileController;
    }

    /**
     * Creates a {@link GaussianRandomTimer} instance with default values.
     *
     * @return  A valid instance of {@link GaussianRandomTimer}.
     */
    public GaussianRandomTimer createGaussianRandomTimer () {

        final GaussianRandomTimerGui gui = new GaussianRandomTimerGui();

        final GaussianRandomTimer gaussianRandomTimer =
                (GaussianRandomTimer) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                gaussianRandomTimer,
                TestPlanElementFactory.PKEY_GAUSSIAN_RANDOM_TIMER__NAME,
                TestPlanElementFactory.PKEY_GAUSSIAN_RANDOM_TIMER__COMMENT,
                TestPlanElementFactory.PKEY_GAUSSIAN_RANDOM_TIMER__ENABLED);

        key = TestPlanElementFactory.PKEY_GAUSSIAN_RANDOM_TIMER__DELAY;

        if (forced || c.containsKey(key)) {

            // constant delay offset in milliseconds;
            final String value = c.getString(key);

            gaussianRandomTimer.setDelay(value);
        }

        key = TestPlanElementFactory.PKEY_GAUSSIAN_RANDOM_TIMER__RANGE;

        if (forced || c.containsKey(key)) {

            // deviation in milliseconds;
            final double value = c.getDouble(key);

            gaussianRandomTimer.setRange(value);  // double | String;
        }

        return gaussianRandomTimer;
    }

    /**
     * Creates a {@link CookieManager} instance with default values.
     *
     * @return  A valid instance of {@link CookieManager}.
     */
    public CookieManager createCookieManager () {

        final CookieManager cookieManager = new CookieManager();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                cookieManager,
                TestPlanElementFactory.PKEY_COOKIE_MANAGER__NAME,
                TestPlanElementFactory.PKEY_COOKIE_MANAGER__COMMENT,
                TestPlanElementFactory.PKEY_COOKIE_MANAGER__ENABLED);

        key = TestPlanElementFactory.PKEY_COOKIE_MANAGER__CLEAR_EACH_ITERATION;

        if (forced || c.containsKey(key)) {

            // true if and only if cookies shall be cleared in each iteration;
            final boolean value = c.getBoolean(key);

            cookieManager.setClearEachIteration(value);
        }

        key = TestPlanElementFactory.PKEY_COOKIE_MANAGER__COOKIE_POLICY;

        if (forced || c.containsKey(key)) {

            // the cookie policy must be one of the following values:
            //   - "default"
            //   - "compatibility" (default value)
            //   - "rfc2109"
            //   - "rfc2965"
            //   - "ignorecookies"
            //   - "netscape"
            final String value = c.getString(key);

            cookieManager.setCookiePolicy(value);
        }

        key = TestPlanElementFactory.PKEY_COOKIE_MANAGER__IMPLEMENTATION;

        if (forced || c.containsKey(key)) {

            // the cookie implementation must be one of the following values:
            //   - "HC3CookieHandler" (default value)
            //   - "HC4CookieHandler"
            final String value = c.getString(key);

            cookieManager.setImplementation(value);
        }

        // derivation from GUI gives lib-error output on console:
        //     final CookiePanel gui = new CookiePanel();
        //     ... (CookieManager) gui.createTestElement();

        this.setGUIandTestProperties(
                cookieManager,
                CookiePanel.class,
                CookieManager.class);

        return cookieManager;
    }

    /**
     * Creates a {@link ResponseAssertion} instance with default values.
     *
     * @return  A valid instance of {@link ResponseAssertion}.
     */
    public ResponseAssertion createResponseAssertion () {

        final AssertionGui gui = new AssertionGui();

        final ResponseAssertion responseAssertion =
                (ResponseAssertion) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                responseAssertion,
                TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__NAME,
                TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__COMMENT,
                TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__ENABLED);

        key = TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__ASSUME_SUCCESS;

        if (forced || c.containsKey(key)) {

            // true if and only if the status of the applied assertion shall be
            // ignored;
            final boolean value = c.getBoolean(key);

            responseAssertion.setAssumeSuccess(value);
        }

        key = TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__SCOPE;

        if (forced || c.containsKey(key)) {

            // scope on which the assertion shall be applied to; this must be
            // one of the following values:
            //   - "scopeAll"       --  main sample and sub-samples
            //   - "scopeParent"    --  main sample only (default)
            //   - "scopeChildren"  --  sub-samples only
            final String value = c.getString(key);

            this.setScope(responseAssertion, value);
        }

        key = TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__SCOPE_VARIABLE_NAME;

        if (forced || c.containsKey(key)) {

            // scope JMeter-variable; if this variable is defined, any other
            // scope-related settings will be ignored;
            final String value = c.getString(key);

            if ( this.isValidValue(value) ) {

                responseAssertion.setScopeVariable(value);
            }

        }

        key = TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__TEST_FIELD;

        if (forced || c.containsKey(key)) {

            // response-field to be tested; this must be one of the following
            // values:
            //   - "responseCode"            --  response code
            //   - "responseData"            --  text response (default)
            //   - "responseDataAsDocument"  --  document (text)
            //   - "responseHeaders"         --  response headers
            //   - "responseMessage"         --  response message
            //   - "URL"                     --  URL sampled
            final String value = c.getString(key);

            this.setTestField(responseAssertion, value);
        }

        key = TestPlanElementFactory.PKEY_RESPONSE_ASSERTION__TYPE;

        if (forced || c.containsKey(key)) {

            // pattern matching type of assertion; this must be one of the
            // following values:
            //   - "equalsType"
            //   - "matchType"
            //   - "notType"
            //   - "substringType"
            //   - "containsType" (default value)
            final String value = c.getString(key);

            this.setType(responseAssertion, value);
        }

        return responseAssertion;
    }

    /**
     * Creates a {@link CounterConfig} instance with default values.
     *
     * @return  A valid instance of {@link CounterConfig}.
     */
    public CounterConfig createCounterConfig () {

        final CounterConfigGui gui = new CounterConfigGui();

        final CounterConfig counterConfig =
                (CounterConfig) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                counterConfig,
                TestPlanElementFactory.PKEY_COUNTER_CONFIG__NAME,
                TestPlanElementFactory.PKEY_COUNTER_CONFIG__COMMENT,
                TestPlanElementFactory.PKEY_COUNTER_CONFIG__ENABLED);

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__START;

        if (forced || c.containsKey(key)) {

            // counter start value;
            final long value = c.getLong(key);

            counterConfig.setStart(value);  // long | String;
        }

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__INCREMENT;

        if (forced || c.containsKey(key)) {

            // counter increment value;
            final long value = c.getLong(key);

            counterConfig.setIncrement(value);  // long | String;
        }

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__END;

        if (forced || c.containsKey(key)) {

            // counter end value;
            final long value = c.getLong(key);

            counterConfig.setEnd(value);  // long | String;
        }

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__FORMAT;

        if (forced || c.containsKey(key)) {

            // optional counter format; defined as DecimalFormat, e.g. 000 for
            // 001, 002, 003, ...;
            final String value = c.getString(key);

            counterConfig.setFormat(value);
        }

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__REFERENCE_NAME;

        if (forced || c.containsKey(key)) {

            // name of the counter value, for being referenced by other elements
            // via syntax ${referenceName};
            final String value = c.getString(key);

            counterConfig.setVarName(value);
        }

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__IS_PER_USER;

        if (forced || c.containsKey(key)) {

            // true if and only if the counter shall be tracked independently
            // for each user;
            final boolean value = c.getBoolean(key);

            counterConfig.setIsPerUser(value);
        }

        key = TestPlanElementFactory.PKEY_COUNTER_CONFIG__RESET_ON_THREAD_GROUP_ITERATION;

        if (forced || c.containsKey(key)) {

            // true if and only if the counter shall be reset on each Thread
            // Group iteration; this flag is ignored, if the isPerUser property
            // is set to false;
            final boolean value = c.getBoolean(key);

            counterConfig.setResetOnThreadGroupIteration(value);
        }

        return counterConfig;
    }

    /**
     * Creates a {@link RegexExtractor} instance with default values.
     *
     * @return  A valid instance of {@link RegexExtractor}.
     */
    public RegexExtractor createRegexExtractor () {

        final RegexExtractorGui gui = new RegexExtractorGui();

        final RegexExtractor regexExtractor =
                (RegexExtractor) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                regexExtractor,
                TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__NAME,
                TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__COMMENT,
                TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__ENABLED);

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__SCOPE;

        if (forced || c.containsKey(key)) {

            // scope on which the extractor shall be applied to; this must be
            // one of the following values:
            //   - "scopeAll"       --  main sample and sub-samples
            //   - "scopeParent"    --  main sample only (default)
            //   - "scopeChildren"  --  sub-samples only
            final String value = c.getString(key);

            this.setScope(regexExtractor, value);
        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__SCOPE_VARIABLE_NAME;

        if (forced || c.containsKey(key)) {

            // scope JMeter-variable; if this variable is defined, any other
            // scope-related settings will be ignored;
            final String value = c.getString(key);

            if ( this.isValidValue(value) ) {

                regexExtractor.setScopeVariable(value);
            }

        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__TEST_FIELD;

        if (forced || c.containsKey(key)) {

            // response-field to be tested; this must be one of the following
            // values:
            //   - "responseCode"            --  response code
            //   - "responseDataUnescaped"   --  non-escaped text response
            //   - "responseData"            --  text response (default)
            //   - "responseDataAsDocument"  --  document (text)
            //   - "responseHeaders"         --  response headers
            //   - "responseMessage"         --  response message
            //   - "URL"                     --  URL sampled
            final String value = c.getString(key);

            regexExtractor.setUseField(value);
        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__REFERENCE_NAME;

        if (forced || c.containsKey(key)) {

            // name of the result variable, for being referenced by other
            // elements via syntax ${referenceName};
            final String value = c.getString(key);

            regexExtractor.setRefName(value);
        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__REGULAR_EXPRESSION;

        if (forced || c.containsKey(key)) {

            // regular expression to be used for parsing the response data;
            final String value = c.getString(key);

            regexExtractor.setRegex(value);
        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__TEMPLATE;

        if (forced || c.containsKey(key)) {

            // template to be used for creating a string from the found matches;
            final String value = c.getString(key);

            regexExtractor.setTemplate(value);
        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__MATCH_NUMBER;

        if (forced || c.containsKey(key)) {

            // number of match to be used for the result;
            final int value = c.getInt(key);

            regexExtractor.setMatchNumber(value);  // int | String;
        }

        key = TestPlanElementFactory.PKEY_REGEX_EXTRACTOR__DEFAULT_VALUE;

        if (forced || c.containsKey(key)) {

            // default value of the referenced variable, in case the regular
            // expression does not match (for debugging purposes in particular,
            // undefined by default).
            final String value = c.getString(key);

            regexExtractor.setDefaultValue(value);
        }

        return regexExtractor;
    }

    /**
     * Creates a {@link HeaderManager} instance with default values.
     *
     * @return  A valid instance of {@link HeaderManager}.
     */
    public HeaderManager createHeaderManager () {

        final HeaderPanel gui = new HeaderPanel();

        final HeaderManager headerManager =
                (HeaderManager) gui.createTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                headerManager,
                TestPlanElementFactory.PKEY_HEADER_MANAGER__NAME,
                TestPlanElementFactory.PKEY_HEADER_MANAGER__COMMENT,
                TestPlanElementFactory.PKEY_HEADER_MANAGER__ENABLED);

        key = TestPlanElementFactory.PKEY_HEADER_MANAGER__HEADERS_FILE;

        if (forced || c.containsKey(key)) {

            final String headersFile = c.getString(key);

            if ( this.isValidValue(headersFile) ) {

                // user-defined set of header parameters;
                // this must be an array of name/value pairs;
                final String[][] headerParameters = this.readParameters(
                        headersFile, HeaderManager.class, 2);

                for (String[] parameter : headerParameters) {

                    final String name  = parameter[0];
                    final String value = parameter[1];

                    final Header header = new Header(name, value);

                    headerManager.add(header);
                }
            }
        }

        return headerManager;
    }

    /**
     * Creates a {@link HTTPSamplerProxy} instance with default values.
     *
     * @return  A valid instance of {@link HTTPSamplerProxy}.
     */
    public HTTPSamplerProxy createHTTPSamplerProxy () {

        final HTTPSamplerProxy httpSamplerProxy = new HTTPSamplerProxy();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                httpSamplerProxy,
                TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__NAME,
                TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__COMMENT,
                TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__ENABLED);

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__DOMAIN;

        if (forced || c.containsKey(key)) {

            // server name or IP address;
            final String value = c.getString(key);

            httpSamplerProxy.setDomain(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PORT;

        if (forced || c.containsKey(key)) {

            // port number;
            final int value = c.getInt(key);

            httpSamplerProxy.setPort(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__CONNECT_TIMEOUT;

        if (forced || c.containsKey(key)) {

            // connect timeout in milliseconds:
            final String value = c.getString(key);

            httpSamplerProxy.setConnectTimeout(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__RESPONSE_TIMEOUT;

        if (forced || c.containsKey(key)) {

            // response timeout in milliseconds;
            final String value = c.getString(key);

            httpSamplerProxy.setResponseTimeout(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__IMPLEMENTATION;

        if (forced || c.containsKey(key)) {

            // HTTP request-implementation (optionally,  undefined by default);
            // this might be one of the following values:
            //   - "HttpClient4"
            //   - "HttpClient3.1"
            //   - "Java"
            //   - "" (blank value, default)
            final String value = c.getString(key);

            httpSamplerProxy.setImplementation(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PROTOCOL;

        if (forced || c.containsKey(key)) {

            // HTTP request-protocol;
            final String value = c.getString(key);

            httpSamplerProxy.setProtocol(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__METHOD;

        if (forced || c.containsKey(key)) {

            // HTTP request-method; this might be one of the following values:
            //   - "GET" (default value)
            //   - "POST"
            //   - "HEAD"
            //   - "PUT"
            //   - "OPTIONS"
            //   - "TRACE"
            //   - "DELETE"
            //   - "PATCH"
            final String value = c.getString(key);

            httpSamplerProxy.setMethod(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__CONTENT_ENCODING;

        if (forced || c.containsKey(key)) {

            // content encoding to be used for HTTP request;
            final String value = c.getString(key);

            httpSamplerProxy.setContentEncoding(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PATH;

        if (forced || c.containsKey(key)) {

            // path to the resource which is to be requested;
            final String value = c.getString(key);

            httpSamplerProxy.setPath(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__AUTO_REDIRECTS;

        if (forced || c.containsKey(key)) {

            // flag for auto-redirects, to be used alternatively with the
            // "follow-redirects" flag (they must not both be set true);
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setAutoRedirects(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__FOLLOW_REDIRECTS;

        if (forced || c.containsKey(key)) {

            // flag for follow-redirects, to be used alternatively with the
            // "auto-redirects" flag (they must not both be set true);
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setFollowRedirects(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__USE_KEEP_ALIVE;

        if (forced || c.containsKey(key)) {

            // true if and only if "Connection: keep-alive" shall be sent;
            // otherwise "Connection: close" will be sent;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setUseKeepAlive(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__DO_MULTIPART_POST;

        if (forced || c.containsKey(key)) {

            // true if and only if "multipart/form-data" for POST shall be used;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setDoMultipartPost(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__DO_BROWSER_COMPATIBLE_MULTIPART;

        if (forced || c.containsKey(key)) {

            // true if and only if browser-compatible headers shall be used;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setDoBrowserCompatibleMultipart(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__ARGUMENTS_FILE;

        if (forced || c.containsKey(key)) {

            final String parametersFile = c.getString(key);

            if ( this.isValidValue(parametersFile) ) {

                final Arguments arguments = this.readParametersForHTTPRequest(
                        parametersFile,
                        HTTPSamplerProxy.class);

                httpSamplerProxy.setArguments(arguments);
            }
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__HTTP_FILES_FILE;

        if (forced || c.containsKey(key)) {

            final String httpFilesFile = c.getString(key);

            if ( this.isValidValue(httpFilesFile) ) {

                final ArrayList<HTTPFileArg> httpFileArgsList =
                        new ArrayList<HTTPFileArg>();

                // read files to be sent with a request optionally;
                final String[][] httpFiles = this.readParameters(
                        httpFilesFile, HTTPSamplerProxy.class, 3);

                for (final String[] file : httpFiles) {

                    final String name     = file[0];
                    final String value    = file[1];
                    final String mimeType = file[2];

                    final HTTPFileArg httpFileArg =
                            new HTTPFileArg(name, value, mimeType);

                    httpFileArgsList.add(httpFileArg);
                }

                httpSamplerProxy.setHTTPFiles(
                        httpFileArgsList.toArray( new HTTPFileArg[]{} ));
            }
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PARSE_IMAGES;

        if (forced || c.containsKey(key)) {

            // true if and only if all embedded resources shall be retrieved;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setImageParser(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__EMBEDDED_URL_REGULAR_EXPRESSION;

        if (forced || c.containsKey(key)) {

            // regular expression which must be matched by any resource-URL for
            // its resource being parsed; this flag is irrelevant, if flag
            // "parseImages" is set to false;
            final String value = c.getString(key);

            httpSamplerProxy.setEmbeddedUrlRE(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__USE_CONCURRENT_POOL_SIZE;

        if (forced || c.containsKey(key)) {

            // true if and only if concurrent pool size shall be used;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setConcurrentDwn(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__CONCURRENT_POOL_SIZE;

        if (forced || c.containsKey(key)) {

            // integer value which defines the concurrent pool size; this value
            // is irrelevant, in case flag "parseImages" is set to false;
            final String value = c.getString(key);

            httpSamplerProxy.setConcurrentPool(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PROXY_HOST;

        if (forced || c.containsKey(key)) {

            // proxy server name or IP address;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            httpSamplerProxy.setProperty(HTTPSampler.PROXYHOST, value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PROXY_PORT;

        if (forced || c.containsKey(key)) {

            // proxy server port;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            httpSamplerProxy.setProperty(HTTPSampler.PROXYPORT, value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PROXY_USER;

        if (forced || c.containsKey(key)) {

            // proxy server user name;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            httpSamplerProxy.setProperty(HTTPSampler.PROXYUSER, value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__PROXY_PASSWORD;

        if (forced || c.containsKey(key)) {

            // proxy server password;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            httpSamplerProxy.setProperty(HTTPSampler.PROXYPASS, value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__IP_SOURCE_TYPE;

        if (forced || c.containsKey(key)) {

            // this must be a value between 0 and 3, interpreted as follows:
            //   - 0  --  IP/Hostname (default value)
            //   - 1  --  Device
            //   - 2  --  Device IPv4
            //   - 3  --  Device IPv6
            final int value = c.getInt(key);

            httpSamplerProxy.setIpSourceType(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__IP_SOURCE;

        if (forced || c.containsKey(key)) {

            // source address as String;
            final String value = c.getString(key);

            httpSamplerProxy.setIpSource(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__USE_AS_MONITOR;

        if (forced || c.containsKey(key)) {

            // true if and only if a Monitor Results Listener shall be used;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setMonitor(value);
        }

        key = TestPlanElementFactory.PKEY_HTTP_SAMPLER_PROXY__SAVE_RESPONSE_AS_MD5_HASH;

        if (forced || c.containsKey(key)) {

            // true if and only if response shall be saved as MD5 hash;
            final boolean value = c.getBoolean(key);

            httpSamplerProxy.setMD5(value);
        }

        // derivation from GUI results in GUI-related Exception:
        //     final HttpTestSampleGui gui = new HttpTestSampleGui();
        //     ... (HTTPSamplerProxy) gui.createTestElement();

        this.setGUIandTestProperties(
                httpSamplerProxy,
                HttpTestSampleGui.class,
                HTTPSamplerProxy.class);

        return httpSamplerProxy;
    }

    /**
     * Creates a {@link ConfigTestElement} instance with default values.
     *
     * @return  A valid instance of {@link ConfigTestElement}.
     */
    public ConfigTestElement createConfigTestElement () {

        final ConfigTestElement configTestElement = new ConfigTestElement();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                configTestElement,
                TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__NAME,
                TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__COMMENT,
                TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__ENABLED);

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__DOMAIN;

        if (forced || c.containsKey(key)) {

            // server name or IP address;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.DOMAIN, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PORT;

        if (forced || c.containsKey(key)) {

            // port number;
            final int value = c.getInt(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PORT, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__CONNECT_TIMEOUT;

        if (forced || c.containsKey(key)) {

            // connect timeout in milliseconds:
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.CONNECT_TIMEOUT, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__RESPONSE_TIMEOUT;

        if (forced || c.containsKey(key)) {

            // response timeout in milliseconds;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.RESPONSE_TIMEOUT, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__IMPLEMENTATION;

        if (forced || c.containsKey(key)) {

            // HTTP request-implementation (optionally,  undefined by default);
            // this might be one of the following values:
            //   - "HttpClient4"
            //   - "HttpClient3.1"
            //   - "Java"
            //   - "" (blank value, default)
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.IMPLEMENTATION, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PROTOCOL;

        if (forced || c.containsKey(key)) {

            // HTTP request-protocol;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PROTOCOL, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__CONTENT_ENCODING;

        if (forced || c.containsKey(key)) {

            // content encoding to be used for HTTP request;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.CONTENT_ENCODING, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PATH;

        if (forced || c.containsKey(key)) {

            // path to the resource which is to be requested;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PATH, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__ARGUMENTS_FILE;

        // arguments must be always set in a ConfigTestElement instance,
        // otherwise the JMeter GUI will end up in an error message;
        final ArgumentsPanel gui = new ArgumentsPanel();
        Arguments arguments = (Arguments) gui.createTestElement();

        if (forced || c.containsKey(key)) {

            final String parametersFile = c.getString(key);

            if ( this.isValidValue(parametersFile) ) {

                arguments = this.readParametersForHTTPRequest(
                        parametersFile,
                        ConfigTestElement.class);
            }
        }

        final TestElementProperty property = new TestElementProperty(
                HTTPSampler.ARGUMENTS,
                arguments);

        // no dedicated set() method for this property available;
        configTestElement.setProperty(property);

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PROXY_HOST;

        if (forced || c.containsKey(key)) {

            // proxy server name or IP address;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PROXYHOST, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PROXY_PORT;

        if (forced || c.containsKey(key)) {

            // proxy server port;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PROXYPORT, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PROXY_USER;

        if (forced || c.containsKey(key)) {

            // proxy server user name;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PROXYUSER, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PROXY_PASSWORD;

        if (forced || c.containsKey(key)) {

            // proxy server password;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.PROXYPASS, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__PARSE_IMAGES;

        if (forced || c.containsKey(key)) {

            // true if and only if all embedded resources shall be retrieved;
            final boolean value = c.getBoolean(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.IMAGE_PARSER, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__EMBEDDED_URL_REGULAR_EXPRESSION;

        if (forced || c.containsKey(key)) {

            // regular expression which must be matched by any resource-URL for
            // its resource being parsed; this flag is irrelevant, if flag
            // "parseImages" is set to false;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.EMBEDDED_URL_RE, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__USE_CONCURRENT_POOL_SIZE;

        if (forced || c.containsKey(key)) {

            // true if and only if concurrent pool size shall be used;
            final boolean value = c.getBoolean(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.CONCURRENT_DWN, value);
        }

        key = TestPlanElementFactory.PKEY_CONFIG_TEST_ELEMENT__CONCURRENT_POOL_SIZE;

        if (forced || c.containsKey(key)) {

            // integer value which defines the concurrent pool size; this value
            // is irrelevant, in case flag "parseImages" is set to false;
            final String value = c.getString(key);

            // no dedicated set() method for this property available;
            configTestElement.setProperty(HTTPSampler.CONCURRENT_POOL, value);
        }

        // derivation from GUI results in GUI-related Exception:
        //     final HttpDefaultsGui gui = new HttpDefaultsGui();
        //     ... (ConfigTestElement) gui.createTestElement();

        this.setGUIandTestProperties(
                configTestElement,
                HttpDefaultsGui.class,
                ConfigTestElement.class);

        return configTestElement;
    }

    /**
     * Creates a {@link MarkovController} instance with default values.
     *
     * @return  A valid instance of {@link MarkovController}.
     */
    public MarkovController createMarkovController () {

        final MarkovController markovController = new MarkovController();

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        this.setTestElementProperties(
                markovController,
                TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__NAME,
                TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__COMMENT,
                TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__ENABLED);

        key = TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__BEHAVIOR_MIX_FILE;

        if (forced || c.containsKey(key)) {

            final String behaviorMixFile = c.getString(key);

            if ( this.isValidValue(behaviorMixFile) ) {

                // user-defined set of behavior mix entries;
                // this must be an array of array of triples, with each line
                // containing the name of the related Behavior Model, relative
                // frequency and filename;
                final String[][] behaviorMixEntries = this.readParameters(
                        behaviorMixFile, MarkovController.class, 3);

                // name = "User Defined Variables", comment = "", enabled = true;
                final BehaviorMix behaviorMix = new BehaviorMix();

                for (final String[] line : behaviorMixEntries) {

                    final String bName             = line[0];
                    final String bRelativeFreqency = line[1];
                    final String bFilename         = line[2];

                    final BehaviorMixEntry behaviorMixEntry =
                            new BehaviorMixEntry();

                    final double bRelativeFreqencyDouble = this.stringToDouble(
                            bRelativeFreqency,
                            MarkovController.class);

                    behaviorMixEntry.setBName(bName);
                    behaviorMixEntry.setRFreq(bRelativeFreqencyDouble);
                    behaviorMixEntry.setFilename(bFilename);

                    behaviorMix.addBehaviorEntry(behaviorMixEntry);
                }
                markovController.setBehaviorMix(behaviorMix);
            }
        }

        key = TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__ENABLED;

        if (forced || c.containsKey(key)) {

            // true if and only if the embedded Arrival Controller shall be
            // enabled;
            final boolean value = c.getBoolean(key);

            markovController.setArrivalCtrlEnabled(value);
        }

        key = TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__MAXIMUM_SESSION_NUMBER;

        if (forced || c.containsKey(key)) {

            // maximum number of sessions, defined as a formula;
            final String value = c.getString(key);

            markovController.setArrivalCtrlNumSessions(value);
        }

        key = TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__LOGGING_ENABLED;

        if (forced || c.containsKey(key)) {

            // true if and only if logging for the Arrival Controller shall be
            // enabled;
            final boolean value = c.getBoolean(key);

            markovController.setArrivalCtrlLoggingEnabled(value);
        }

        key = TestPlanElementFactory.PKEY_MARKOV_CONTROLLER__ARRIVAL_CONTROLLER__LOG_FILE;

        if (forced || c.containsKey(key)) {

            // log-file to be used by the Arrival Controller, in case logging
            // is enabled;
            final String value = c.getString(key);

            markovController.setArrivalCtrlLogfile(value);
        }

        // IDs and state names are not provided by GUI -> ignore them here;
        // markovController.setId(id);
        // markovController.setStateNames(stateNames);

        // derivation from GUI results in NullPointerException:
        //     final MarkovControllerGui gui = new MarkovControllerGui();
        //     ... (MarkovController) gui.createTestElement();

        this.setGUIandTestProperties(
                markovController,
                MarkovControllerGui.class,
                MarkovController.class);

        return markovController;
    }

    /**
     * Creates an {@link ApplicationState} instance with default values.
     *
     * @return  A valid instance of {@link ApplicationState}.
     */
    public ApplicationState createApplicationState () {

        final ApplicationStateGui gui = new ApplicationStateGui();

        // sets ID as well as GUI- and test-class;
        final ApplicationState applicationState =
                gui.createTestElement();

        this.setTestElementProperties(
                applicationState,
                TestPlanElementFactory.PKEY_APPLICATION_STATE__NAME,
                TestPlanElementFactory.PKEY_APPLICATION_STATE__COMMENT,
                TestPlanElementFactory.PKEY_APPLICATION_STATE__ENABLED);

        // IDs are not provided by GUI -> ignore them here;

        // transitions are added automatically when being created as a child
        // of a MarkovController element -> do not set them explicitly;

        return applicationState;
    }

    /**
     * Creates a {@link ResultCollector} instance with default values,
     * represented in JMeter as a <i>Response Time Graph</i>.
     *
     * @return  A valid instance of {@link ResultCollector}.
     */
    public ResultCollector createResponseTimeGraph () {

        final ResultCollector resultCollector = new ResultCollector();

        this.setTestElementProperties(
                resultCollector,
                TestPlanElementFactory.PKEY_RESPONSE_TIME_GRAPH__NAME,
                TestPlanElementFactory.PKEY_RESPONSE_TIME_GRAPH__COMMENT,
                TestPlanElementFactory.PKEY_RESPONSE_TIME_GRAPH__ENABLED);

        // further properties do not need be set here, since each Result
        // Collector is completely configured by default and does not provide
        // any additional functionality for the measurement process itself;
        //
        // TODO: the option for setting the properties explicitly might be
        //       implemented in future;

        // derivation from GUI results in GUI-related Exception:
        //    final RespTimeGraphVisualizer gui = new RespTimeGraphVisualizer();
        //     ... (ResultCollector) gui.createTestElement();

        this.setGUIandTestProperties(
                resultCollector,
                RespTimeGraphVisualizer.class,
                ResultCollector.class);

        return resultCollector;
    }

    /**
     * Creates a {@link ResultCollector} instance with default values,
     * represented in JMeter as a <i>View Results Tree</i>.
     *
     * @return  A valid instance of {@link ResultCollector}.
     */
    public ResultCollector createViewResultsTrue () {

        final ResultCollector resultCollector = new ResultCollector();

        this.setTestElementProperties(
                resultCollector,
                TestPlanElementFactory.PKEY_VIEW_RESULTS_TREE__NAME,
                TestPlanElementFactory.PKEY_VIEW_RESULTS_TREE__COMMENT,
                TestPlanElementFactory.PKEY_VIEW_RESULTS_TREE__ENABLED);

        // further properties do not need be set here, since each Result
        // Collector is completely configured by default and does not provide
        // any additional functionality for the measurement process itself;
        //
        // TODO: the option for setting the properties explicitly might be
        //       implemented in future;

        // derivation from GUI results in GUI-related Exception:
        //     final ViewResultsFullVisualizer gui = new ViewResultsFullVisualizer();
        //     ... (ResultCollector) gui.createTestElement();

        this.setGUIandTestProperties(
                resultCollector,
                ViewResultsFullVisualizer.class,
                ResultCollector.class);

        return resultCollector;
    }


    /* ****************  private methods (general purposes)  **************** */


    /**
     * Sets the "name"-, "comment"- and "enabled"-properties of a given Test
     * Plan element.
     *
     * @param testElement
     *     Test Plan element whose properties shall be set.
     * @param propertyKeyForName
     *     Key for the "name"-property.
     * @param propertyKeyForComment
     *     Key for the "comment"-property.
     * @param propertyKeyForEnabled
     *     Key for the "enabled"-property.
     */
    private void setTestElementProperties (
            final TestElement testElement,
            final String propertyKeyForName,
            final String propertyKeyForComment,
            final String propertyKeyForEnabled) {

        final Configuration c = this.configuration;
        final boolean forced  = this.useForcedValues;

        String key;

        key = propertyKeyForName;

        if (forced || c.containsKey(key)) {

            // name of the instance;
            final String value = c.getString(key);

            testElement.setName(value);
        }

        key = propertyKeyForComment;

        if (forced || c.containsKey(key)) {

            // additional information to be stored;
            final String value = c.getString(key);

            testElement.setComment(value);
        }

        key = propertyKeyForEnabled;

        if (forced || c.containsKey(key)) {

            // true if and only if the created instance shall be enabled;
            final boolean value = c.getBoolean(key);

            testElement.setEnabled(value);
        }
    }

    /**
     * Parses a <code>double</code> value from a given <code>String</code> and
     * gives a warning message in case parsing fails. Since the parsing is done
     * for a certain type of Test Plan element, the type must be specified
     * additionally.
     *
     * @param str     <code>String</code> to be parsed.
     * @param parent  The type of element for which the parsing shall be done.
     *
     * @return  The parsed value, or 0.0 by default if parsing fails.
     */
    private double stringToDouble (final String str, final Class<?> parent) {

        double value;

        try {

            value = Double.parseDouble(str);

        } catch (final NumberFormatException ex) {

            final String message = String.format(
                    TestPlanElementFactory.WARNING_UNVALID_CSV_DOUBLE,
                    parent.getSimpleName(),
                    str);

            TestPlanElementFactory.LOG.warn(message);
            value = 0.0;
        }

        return value;
    }

    /**
     * Checks whether the given <code>String</code> equals <code>"true"</code>,
     * with non-case-sensitivity and wrapping white spaces being ignored.
     *
     * @param str  <code>String</code> to be checked.
     *
     * @return
     *    <code>true</code> if and only if the given <code>String</code>
     *    indicates a "true" value.
     */
    private boolean isTrue (final String str) {

        return str != null &&
                TestPlanElementFactory.TRUE_STRING.equalsIgnoreCase(str.trim());
    }

    /**
     * Checks whether the given value of type <code>String</code> is valid;
     * that is, it does not equal <code>null</code> and does neither denote the
     * empty <code>String</code> (<code>""</code>) nor a whitespace sequence.
     *
     * @param value  Value to be checked.
     *
     * @return  <code>true</code> if and only if the given value is valid.
     */
    private boolean isValidValue (final String value) {

        return (value != null) && !"".equals(value.trim());
    }

    /**
     * Reads a simple list of strings from a file. Each line-break in the file
     * content indicates a new <code>String</code> instance, for example, the
     * content
     * <pre>
     *   first
     *   second
     *   third</pre>
     * results in the array <code>{"first", "second", "third"}</code>. Since the
     * reading is done for a certain type of Test Plan element, the type must
     * be specified additionally.
     *
     * @param filename  Name of the file to be read.
     * @param parent    The type of element for which the reading shall be done.
     *
     * @return  An array of strings which have been read.
     */
    private final String[] readSimpleList (
            final String filename, final Class<?> parent) {

        final ArrayList<String> rValues = new ArrayList<String>();

        final String[][] values =
                this.readParameters(filename, TestPlan.class, 1, null);

        for (final String[] line : values) {

            rValues.add(line[0]);
        }

        return rValues.toArray( new String[]{} );
    }

    /**
     * Reads a list of parameter values from a CSV-file. Each line-break in the
     * file content indicates a new parameter. Values need to be separated by
     * tabulators, and the number of values per line (columns) must be
     * passed. In case a line has too few values, a warning will be given and
     * the line will not be taken into the result array.
     *
     * <p>Since the reading is done for a certain type of Test Plan element,
     * the type must be specified additionally.
     *
     * @param filename  Name of the file to be read.
     * @param parent    The type of element for which the reading shall be done.
     * @param columns   Number of parameter values.
     *
     * @return  An array of parameters which have been read.
     */
    private String[][] readParameters (
            final String filename, final Class<?> parent, final int columns) {

        return this.readParameters(
                filename,
                parent,
                columns,
                TestPlanElementFactory.DEFAULT_CSV_SEPARATOR);
    }

    /**
     * Reads a list of parameters from a CSV-file. Each line of the file content
     * indicates a new parameter. Values need to be separated by the specified
     * separator, and the number of values per line (columns) must be passed.
     * If a line has too few values, it will not be taken into the result array;
     * instead, a warning will be given.
     *
     * <p>Since the reading is done for a certain type of Test Plan element,
     * the type must be specified additionally.
     *
     * @param filename
     *     Name of the file to be read.
     * @param parent
     *     The type of element for which the reading shall be done.
     * @param columns
     *     Number of parameter values.
     * @param separator
     *     Regular expression which describes the separator to be used.
     *
     * @return  An array of parameters which have been read.
     */
    private String[][] readParameters (
            final String filename,
            final Class<?> parent,
            final int columns,
            final String separator) {

        final ArrayList<String[]> rValues = new ArrayList<String[]>();
        final CSVReader csvReader = new CSVReader(separator);

        try {

            final String[][] values = csvReader.readValues(filename);

            for (int i = 0, n = values.length; i < n; i++) {

                final String[] line = values[i];

                if (line.length < columns) {

                    final String message = String.format(
                        TestPlanElementFactory.WARNING_INVALID_CSV_FILE_ENTRY,
                        parent.getSimpleName(),
                        (i + 1),  // current line number;
                        filename);

                    TestPlanElementFactory.LOG.warn(message);

                } else {

                    rValues.add(line);
                }
            }

        } catch (final IOException | NullPointerException ex) {

            final String message = String.format(
                    TestPlanElementFactory.WARNING_INVALID_CSV_FILE,
                    filename,
                    parent.getSimpleName(),
                    ex.getMessage());

            TestPlanElementFactory.LOG.warn(message);
        }

        return rValues.toArray( new String[][]{} );
    }

    /**
     * Sets the GUI- and Test-class properties of a Test Plan element.
     *
     * <p><u>Note:</u> The GUI-class property needs to be set for displaying
     * Test Plans in the JMeter GUI; otherwise the loading of Test Plans into
     * the GUI fails with an "empty test plan" message.
     *
     * @param testElement  Test Plan element to be initialized.
     * @param guiClass     GUI-class to be set.
     * @param testClass    Test-class to be set.
     */
    private void setGUIandTestProperties (
            final TestElement testElement,
            final Class<?> guiClass,
            final Class<?> testClass) {

        testElement.setProperty(new StringProperty(
                TestElement.GUI_CLASS, guiClass.getName()));

        testElement.setProperty(new StringProperty(
                TestElement.TEST_CLASS, testClass.getName()));
    }


    /* ****************  private methods (specific purposes)  *************** */


    /**
     * Sets the "type" property of a Response Assertion.
     *
     * @param responseAssertion  Response Assertion whose type shall be set.
     * @param type               Type to be set.
     */
    private void setType (
            final ResponseAssertion responseAssertion,
            final String type) {

        if (TestPlanElementFactory.
                RESPONSE_ASSERTION_EQUALS_TYPE.equals(type)) {

            responseAssertion.setToEqualsType();

        } else if (TestPlanElementFactory.
                RESPONSE_ASSERTION_MATCH_TYPE.equals(type)) {

            responseAssertion.setToMatchType();

        } else if (TestPlanElementFactory.
                RESPONSE_ASSERTION_NOT_TYPE.equals(type)) {

            responseAssertion.setToNotType();

        } else if (TestPlanElementFactory.
                RESPONSE_ASSERTION_SUBSTRING_TYPE.equals(type)) {

            responseAssertion.setToSubstringType();

        } else if (TestPlanElementFactory.
                RESPONSE_ASSERTION_CONTAINS_TYPE.equals(type)) {

            responseAssertion.setToContainsType();

        } else {

            responseAssertion.setToContainsType();  // default setting;
        }
    }

    /**
     * Sets the "test field" property of a Response Assertion.
     *
     * @param responseAssertion
     *     Response Assertion whose test field shall be set.
     * @param testField
     *     Test field to be set.
     */
    private void setTestField (
            final ResponseAssertion responseAssertion,
            final String testField) {

        if (TestPlanElementFactory.TEST_FIELD_RESPONSE_CODE.
                equals(testField)) {

            responseAssertion.setTestFieldResponseCode();

        } else if (TestPlanElementFactory.TEST_FIELD_RESPONSE_DATA.
                equals(testField)) {

            responseAssertion.setTestFieldResponseData();

        } else if (TestPlanElementFactory.TEST_FIELD_RESPONSE_DATA_AS_DOCUMENT.
                equals(testField)) {

            responseAssertion.setTestFieldResponseDataAsDocument();

        } else if (TestPlanElementFactory.TEST_FIELD_RESPONSE_HEADERS.
                equals(testField)) {

            responseAssertion.setTestFieldResponseHeaders();

        } else if (TestPlanElementFactory.TEST_FIELD_RESPONSE_MESSAGE.
                equals(testField)) {

            responseAssertion.setTestFieldResponseMessage();

        } else if (TestPlanElementFactory.TEST_FIELD_URL.
                equals(testField)) {

            responseAssertion.setTestFieldURL();

        } else {

            responseAssertion.setTestFieldResponseData();  // default setting;
        }
    }

    /**
     * Sets the "scope" property of an "Abstract Scoped Test Element".
     *
     * @param abstractScopedTestElement  Element whose scope shall be set.
     * @param scope                      Scope to be set.
     */
    private void setScope (
            final AbstractScopedTestElement abstractScopedTestElement,
            final String scope) {

        if (TestPlanElementFactory.SCOPE_ALL.equals(scope)) {

            abstractScopedTestElement.setScopeAll();

        } else if (TestPlanElementFactory.SCOPE_CHILDREN.equals(scope)) {

            abstractScopedTestElement.setScopeChildren();

        } else if (TestPlanElementFactory.SCOPE_PARENT.equals(scope)) {

            abstractScopedTestElement.setScopeParent();

        } else {

            abstractScopedTestElement.setScopeParent();  // default setting;
        }
    }

    /**
     * Reads a set of HTTPRequest-related parameters from a given CSV-file; if
     * the passed CSV-filename is invalid, no parameters will be read, and a
     * warning message will be given instead.
     *
     * @param parametersFile
     *     CSV-file which provides the parameters information.
     * @param parent
     *     Type of element for which the parameters shall be read.
     *
     * @return
     *     A valid instance of {@link Arguments}, which might contain no
     *     elements in case reading fails.
     */
    private Arguments readParametersForHTTPRequest (
            final String parametersFile,
            final Class<?> parent) {

        final ArgumentsPanel gui = new ArgumentsPanel();
        final Arguments arguments = (Arguments) gui.createTestElement();

        if ( this.isValidValue(parametersFile) ) {

            // read user-defined set of arguments;
            // this must be an array of quadruples including name/value and
            // "alwaysEncoded"/"useEquals" flags;
            final String[][] parameters =
                    this.readParameters(parametersFile, parent, 4);

            for (String[] parameter : parameters) {

                final String  name          = parameter[0];
                final String  value         = parameter[1];
                final boolean alwaysEncoded = this.isTrue(parameter[2]);
                final boolean useEquals     = this.isTrue(parameter[3]);

                // addArgument() methods in HTTPArgument do not allow to set
                // the "useEquals" flag explicitly, so build an own instance;
                final HTTPArgument httpArgument = new HTTPArgument(name, value);

                httpArgument.setAlwaysEncoded(alwaysEncoded);
                httpArgument.setUseEquals(useEquals);

                arguments.addArgument(httpArgument);
            }

        } else {

            final String message = String.format(
                    WARNING_PARAMETERS_FILENAME_INVALID,
                    parent,
                    parametersFile);

            TestPlanElementFactory.LOG.warn(message);
        }

        return arguments;
    }
}
