package net.sf.markov4jmeter.testplangenerator.util;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a configuration which supports reading of typed
 * values from a properties file. Undefined values will be initialized with
 * default values.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class Configuration extends Properties {

    /** Warning message for the case that a value of type <code>String</code>
     *  could not be found. */
    private final static String WARNING_NO_PROPERTY_STRING =
            "Could not get String value for property \"%s\"; "
            + "will use value \"%s\" instead.";

    /** Warning message for the case that parsing of an <code>boolean</code>
     *  value fails. */
    private final static String WARNING_UNVALID_BOOLEAN =
            "Could not parse boolean value for property \"%s\"; "
            + "will use value \"%s\" instead.";

    /** Warning message for the case that parsing of an <code>int</code>
     *  value fails. */
    private final static String WARNING_UNVALID_INT =
            "Could not parse integer value for property \"%s\"; "
            + "will use value %s instead.";

    /** Warning message for the case that parsing of a <code>long</code>
     *  value fails. */
    private final static String WARNING_UNVALID_LONG =
            "Could not parse long value for property \"%s\"; "
            + "will use value %s instead.";

    /** Warning message for the case that parsing of a <code>double</code>
     *  value fails. */
    private final static String WARNING_UNVALID_DOUBLE =
            "Could not parse double value for property \"%s\"; "
            + "will use value %s instead.";

    /** Default value to be used for invalid <code>String</code> values. */
    private final static String DEFAULT_STRING = "";

    /** Default value to be used for invalid <code>boolean</code> values. */
    private final static boolean DEFAULT_BOOLEAN = false;

    /** Default value to be used for invalid <code>int</code> values. */
    private final static int DEFAULT_INT = 0;

    /** Default value to be used for invalid <code>long</code> values. */
    private final static long DEFAULT_LONG = 0L;

    /** Default value to be used for invalid <code>double</code> values. */
    private final static double DEFAULT_DOUBLE = 0.0D;

    /** Log-factory for any warnings or error messages. */
    private final static Log LOG = LogFactory.getLog(Configuration.class);

    /** Default serial version ID. */
    private final static long serialVersionUID = 1L;

    /** Pattern for a property value which denotes "true". */
    private final static String TRUE_STRING = "true";

    /** Pattern for a property value which denotes "false". */
    private final static String FALSE_STRING = "false";


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for a configuration with no parameters.
     */
    public Configuration () {

        super();
    }

    /**
     * Constructor for a configuration with specific default parameters.
     *
     * @param defaults  Default parameters to be initialized.
     */
    public Configuration (final Configuration defaults) {

        super(defaults);
    }


    /* **************************  public methods  ************************** */


    /**
     * Loads the key/value pairs from a specified properties file.
     *
     * @param filename  Name of the properties file to be loaded.
     * 
     * @throws FileNotFoundException
     *     in case the denoted file does not exist.
     * @throws IOException
     *     if any error while reading occurs.
     * @throws NullPointerException
     *     if <code>null</code> is passed as filename.
     */
    public void load (final String filename)
            throws FileNotFoundException, IOException {

        // might throw a FileNotFoundException;
        final FileInputStream fileInputStream = new FileInputStream(filename);

        final BufferedInputStream bufferedInputStream =
                new BufferedInputStream(fileInputStream);

        try {

            // might throw an IOException;
            this.load(bufferedInputStream);

        } finally {

            bufferedInputStream.close();
        }
    }

    /**
     * Returns a value of type <code>String</code> for the given key.
     *
     * @param propertyKey  Key whose value shall be returned.
     *
     * @return
     *     The value which is assigned to the given key; in case the value
     *     is not available, a warning will be logged, and a default value
     *     (empty <code>String</code>) will be returned.
     */
    public String getString (final String propertyKey) {

        String str = this.getProperty(propertyKey);

        if (str == null) {

            final String message = String.format(
                    Configuration.WARNING_NO_PROPERTY_STRING,
                    propertyKey,
                    Configuration.DEFAULT_STRING);

            Configuration.LOG.warn(message);

            str = Configuration.DEFAULT_STRING;
        }
        return str;
    }

    /**
     * Returns a value of type <code>boolean</code> for the given key.
     *
     * @param propertyKey  Key whose value shall be returned.
     *
     * @return
     *     The value which is assigned to the given key; in case the value
     *     is not available, a warning will be logged, and a default value
     *     (<code>false</code>) will be returned.
     */
    public boolean getBoolean (final String propertyKey) {

        // do not use Boolean.parseBoolean(str), since it gives no warning;

        boolean value;  // to be returned;

        final String str = this.getString(propertyKey);

        if ( Configuration.TRUE_STRING.equalsIgnoreCase(str) ) {

            value = true;

        } else if ( Configuration.FALSE_STRING.equalsIgnoreCase(str) ) {

            value = false;

        } else {

            final String message = String.format(
                    Configuration.WARNING_UNVALID_BOOLEAN,
                    propertyKey,
                    Configuration.DEFAULT_BOOLEAN);
    
            Configuration.LOG.warn(message);
    
            value = Configuration.DEFAULT_BOOLEAN;
        }
        return value;
    }

    /**
     * Returns a value of type <code>int</code> for the given key.
     *
     * @param propertyKey  Key whose value shall be returned.
     *
     * @return
     *     The value which is assigned to the given key; in case the value
     *     is not available, a warning will be logged, and a default value
     *     (<code>0</code>) will be returned.
     */
    public int getInt (final String propertyKey) {

        int value;  // to be returned;

        final String str = this.getString(propertyKey);

        try {
            value = Integer.parseInt(str);

        } catch (final NumberFormatException ex) {

            final String message = String.format(
                    Configuration.WARNING_UNVALID_INT,
                    propertyKey,
                    Configuration.DEFAULT_INT);

            Configuration.LOG.warn(message);

            value = Configuration.DEFAULT_INT;
        }
        return value;
    }

    /**
     * Returns a value of type <code>long</code> for the given key.
     *
     * @param propertyKey  Key whose value shall be returned.
     *
     * @return
     *     The value which is assigned to the given key; in case the value
     *     is not available, a warning will be logged, and a default value
     *     (<code>0L</code>) will be returned.
     */
    public long getLong (final String propertyKey) {

        long value;  // to be returned;

        final String str = this.getString(propertyKey);

        try {
            value = Long.parseLong(str);

        } catch (final NumberFormatException ex) {

            final String message = String.format(
                    Configuration.WARNING_UNVALID_LONG,
                    propertyKey,
                    Configuration.DEFAULT_LONG);

            Configuration.LOG.warn(message);

            value = Configuration.DEFAULT_LONG;
        }
        return value;
    }

    /**
     * Returns a value of type <code>double</code> for the given key.
     *
     * @param propertyKey  Key whose value shall be returned.
     *
     * @return
     *     The value which is assigned to the given key; in case the value
     *     is not available, a warning will be logged, and a default value
     *     (<code>0.0D</code>) will be returned.
     */
    public double getDouble (final String propertyKey) {

        double value;  // to be returned;

        final String str = this.getString(propertyKey);

        try {
            value = Double.parseDouble(str);

        } catch (final NumberFormatException ex) {

            final String message = String.format(
                    Configuration.WARNING_UNVALID_DOUBLE,
                    propertyKey,
                    Configuration.DEFAULT_DOUBLE);

            Configuration.LOG.warn(message);

            value = Configuration.DEFAULT_DOUBLE;
        }
        return value;
    }
}
