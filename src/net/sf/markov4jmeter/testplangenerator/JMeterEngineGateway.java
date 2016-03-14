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

import java.io.FileInputStream;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * Gateway which provides methods for initializing and using the JMeter engine.
 *
 * <p>This class implements the <i>Singleton Pattern</i>, which means that there
 * is only one instance available, to be requested via {@link #getInstance()}.
 *
 * <p>Furthermore, this class is <u>not</u> intended to be sub-classed.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public final class JMeterEngineGateway {

    /** Separator to be used by JMeter utilities for handling file paths
     *  properly. */
    // the JMeter properties loader, which refers to this separator, uses "/"
    // by default, so File.separatorChar should not be used here; otherwise
    // inconsistent file paths might occur, for example, on Windows systems;
    private final static String JMETER_PROPERTIES_FILE_SEPARATOR = "/";

    /** Error message for the case that the JMeter properties file could not
     *  be read. */
    private final static String ERROR_JMETER_PROPERTIES_READING_FAILED =
            "Could not read JMeter properties file \"%s\": %s";

    /** Log-factory for any warnings or error messages. */
    private final static Log LOG = LogFactory.getLog(JMeterEngineGateway.class);


    /* ***************************  constructors  *************************** */


    /**
     * Constructor, makes the standard constructor <code>private</code>.
     */
    private JMeterEngineGateway () { }


    /* **************************  public methods  ************************** */


    /**
     * Returns a unique instance of this class.
     *
     * @return An instance of {@link JMeterEngineGateway}.
     */
    public static JMeterEngineGateway getInstance() {

        return JMeterEngineGateway.SingletonHolder.INSTANCE;
    }

    /**
     * Initializes the JMeter utilities by setting the required properties
     * in the {@link JMeterUtils} class.
     *
     * @param jMeterHome
     *     home directory of JMeter (= local installation directory of the
     *     JMeter tool).
     * @param jMeterPropertiesFile
     *     file which contains JMeter-specific settings, e.g., the
     *     <code>jmeter.properties</code> file which can be found in the
     *     <code>bin/</code> folder of the JMeter home directory by default.
     * @param locale
     *     the locality to be used.
     *
     * @return
     *     <code>true</code> if and only if the specified properties file could
     *     be read successfully.
     */
    public boolean initJMeter (
            final String jMeterHome,
            final String jMeterPropertiesFile,
            final Locale locale) {

        boolean success = true;  // to be returned;

        final String jMeterPropertiesFilePath =
                jMeterHome +
                JMeterEngineGateway.JMETER_PROPERTIES_FILE_SEPARATOR +
                jMeterPropertiesFile;

        JMeterUtils.setJMeterHome(jMeterHome);
        JMeterUtils.setLocale(locale);

        try {

            // might throw (at least) a RunTimeException;
            JMeterUtils.loadJMeterProperties(jMeterPropertiesFilePath);

        } catch (final Exception ex) {

            final String message = String.format(

                    JMeterEngineGateway.ERROR_JMETER_PROPERTIES_READING_FAILED,
                    jMeterPropertiesFilePath,
                    ex.getMessage());

            JMeterEngineGateway.LOG.error(message);
            success = false;
        }

        return success;
    }

    /**
     * Starts the JMeter engine for processing a given Test Plan.
     *
     * <p><u>Note:</u> this will be an additional feature for running Test Plans
     * directly without starting the JMeter application; the full functionality
     * of this method is not provided yet.
     *
     * @param testPlan  Test Plan to be processed by the JMeter engine.
     */
    public void startJMeterEngine (final HashTree testPlan) {

        // JMeter engine;
        final StandardJMeterEngine jmeterEngine = new StandardJMeterEngine();

        jmeterEngine.configure(testPlan);
        jmeterEngine.run();

        // TODO: capture results and return/print information;
    }

    /**
     * Loads the specified Test Plan and starts the JMeter engine for processing
     * it.
     *
     * @param testPlanFile
     *     location of the Test Plan to be loaded and processed.
     * @throws Exception
     *     if any error while reading the Test Plan occurs.
     */
    public void startJMeterEngine (final String testPlanFile) throws Exception {

        // might throw a FileNotFoundException or SecurityException;
        final FileInputStream fileInputStream =
                new FileInputStream(testPlanFile);

        // might throw an Exception;
        HashTree testPlan = SaveService.loadTree(fileInputStream);
        this.startJMeterEngine(testPlan);
    }


    /* *************************  internal classes  ************************* */


    /**
     * SingletonHolder for singleton pattern; loaded on the first execution of
     * {@link JMeterEngineGateway#getInstance()}.
     */
    private final static class SingletonHolder {

        private final static JMeterEngineGateway INSTANCE =
                new JMeterEngineGateway();
    }
}
