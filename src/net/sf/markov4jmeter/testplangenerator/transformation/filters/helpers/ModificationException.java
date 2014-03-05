package net.sf.markov4jmeter.testplangenerator.transformation.filters.helpers;

/**
 * Exception to be thrown if any Test Plan modification fails.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class ModificationException extends Exception {

    /** Default serial version ID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with a specific error message.
     *
     * @param message  additional information about the error which occurred.
     */
    public ModificationException (final String message) {

        super(message);
    }
}